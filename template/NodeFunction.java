package template;

import java.util.HashMap;

abstract class NodeFunction extends Node {
    private NodeList arguments;
    private HashMap<String,Node> blocks;

    public NodeFunction() {
    }

    abstract public String getFunctionName();

    public void setArguments(Parser parser, NodeList arguments) throws ParseException {
        this.arguments = arguments;
    }

    protected NodeList getArguments() {
        return this.arguments;
    }

    protected String[] getPermittedBlockNames() {
        return null;    // no restrictions on block names
    }

    protected boolean requiresAnonymousBlock() {
        return false;
    }

    public void addBlock(Parser parser, String name, Node block, int startPos) throws ParseException {
        // Check block is permitted
        String[] permittedNames = this.getPermittedBlockNames();
        if(permittedNames != null) {
            boolean permitted = false;
            for(String n : permittedNames) { if(n.equals(name)) { permitted = true; break; } }
            if(!permitted) {
                parser.error(this.getFunctionName()+"() may not take "+
                    ((name == Node.BLOCK_ANONYMOUS) ? "an anonymous" : ("a "+name))+" block",
                    startPos);
            }
        }
        if(this.blocks == null) {
            this.blocks = new HashMap<String,Node>(4);
        }
        if(this.blocks.containsKey(name)) {
            parser.error("Repeated block for function: "+name);
        }
        this.blocks.put(name, block);
    }

    public void postParse(Parser parser, int functionStartPos) throws ParseException {
        if(this.requiresAnonymousBlock()) {
            if((this.blocks == null) || !(this.blocks.containsKey(Node.BLOCK_ANONYMOUS))) {
                parser.error(this.getFunctionName()+"() requires an anonymous block", functionStartPos);
            }
        }
    }

    protected Node getBlock(String name) {
        if(this.blocks == null) { return null; }
        return this.blocks.get(name);
    }

    public void dumpToBuilder(StringBuilder builder, String linePrefix) {
        builder.append(linePrefix).append(getDumpName()).
                append(" with "+this.arguments.size()+" arguments:\n");
        arguments.dumpToBuilder(builder, linePrefix+"  ");
        if(this.blocks != null) {
            for(String blockName : this.blocks.keySet()) {
                builder.append(linePrefix+"  BLOCK "+blockName+"\n");
                this.blocks.get(blockName).dumpToBuilder(builder, linePrefix+"    ");
            }
        }
    }

    abstract public String getDumpName();

    // ----------------------------------------------------------------------

    public abstract static class ExactlyOneArgument extends NodeFunction {
        public void setArguments(Parser parser, NodeList arguments) throws ParseException {
            if((arguments == null) || (arguments.size() != 1)) {
                parser.error(this.getFunctionName()+"() must take exactly one argument");
            }
            super.setArguments(parser, arguments);
        }
        public Node getSingleArgument() {
            return this.getArguments().nodeAt(0);
        }
    }

    public abstract static class ExactlyOneValueArgument extends ExactlyOneArgument {
        public void postParse(Parser parser, int functionStartPos) throws ParseException {
            super.postParse(parser, functionStartPos);
            if(!(getSingleArgument() instanceof NodeValue)) {
                parser.error(this.getFunctionName()+"() must have a value as the argument", functionStartPos);
            }
        }
    }
}
