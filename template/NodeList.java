package template;

import java.util.ArrayList;

class NodeList extends Node {
    ArrayList<Node> nodes;

    protected NodeList() {
        this.nodes = new ArrayList<Node>(16);
    }

    public void add(Node node) {
        this.nodes.add(node);
    }

    public int size() {
        return this.nodes.size();
    }

    public Node nodeAt(int index) {
        return this.nodes.get(index);
    }

    protected Node orSingleNode() {
        return (this.nodes.size() == 1) ? this.nodes.get(0) : this;
    }

    protected Object value(Driver driver, Object view) {
        if(this.nodes.size() == 1) {
            return this.nodes.get(0).value(driver, view);
        } else {
            return null;
        }
    }

    protected Iterable<Object> valueIterableViewList(Driver driver, Object view) {
        if(this.nodes.size() == 1) {
            return this.nodes.get(0).valueIterableViewList(driver, view);
        } else {
            return null;
        }
    }

    public void render(StringBuilder builder, Driver driver, Object view, Context context) {
        if(context == Context.ATTRIBUTE_VALUE) {
            // Lists need to be space separated inside attributes
            int listStart = builder.length();
            for(Node node : this.nodes) {
                // If something has been added already, add a space
                if(listStart != builder.length()) {
                    builder.append(' ');
                }
                // Record the start of the value, then render it
                int valueStart = builder.length();
                node.render(builder, driver, view, context);
                // If nothing was output, and it's not the first attribute, then remove the space
                if(valueStart == builder.length()) {
                    if(listStart != valueStart) {
                        builder.setLength(valueStart - 1);
                    }
                }
            }
            
        } else {
            // For all other contexts, output values with nothing between them
            for(Node node : this.nodes) {
                node.render(builder, driver, view, context);
            }
        }
    }

    public void dumpToBuilder(StringBuilder builder, String linePrefix) {
        builder.append(linePrefix).append("LIST ("+this.nodes.size()+" nodes)\n");
        for(Node node : this.nodes) {
            node.dumpToBuilder(builder, linePrefix+"  ");
        }
    }
}
