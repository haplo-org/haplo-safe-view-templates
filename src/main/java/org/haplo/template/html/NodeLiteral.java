package org.haplo.template.html;

final class NodeLiteral extends Node {
    private String literal;

    public NodeLiteral(String literal) {
        this.literal = literal;
    }

    protected String getLiteralString() {
        return this.literal;
    }

    protected boolean tryToMergeWith(Node otherNode) {
        if(otherNode instanceof NodeLiteral) {
            this.literal += ((NodeLiteral)otherNode).literal;
            return true;
        }
        return false;
    }

    public void render(StringBuilder builder, Driver driver, Object view, Context context) throws RenderException {
        builder.append(this.literal);
    }

    protected Object valueForFunctionArgument(Driver driver, Object view) {
        return this.literal;
    }

    protected boolean whitelistForLiteralStringOnly() {
        return true;
    }

    public void dumpToBuilder(StringBuilder builder, String linePrefix) {
        builder.append(linePrefix).append("LITERAL ").append(this.literal).append("\n");
    }
}
