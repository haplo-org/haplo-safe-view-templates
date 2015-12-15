package template;

import java.util.List;
import java.util.ArrayList;

abstract class NodeListBase extends Node {
    protected ArrayList<Node> nodes;

    protected NodeListBase() {
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

    public void dumpToBuilder(StringBuilder builder, String linePrefix) {
        builder.append(linePrefix).append(this.dumpName()).append(" ("+this.nodes.size()).append(" nodes)\n");
        for(Node node : this.nodes) {
            node.dumpToBuilder(builder, linePrefix+"  ");
        }
    }

    abstract protected String dumpName();
}
