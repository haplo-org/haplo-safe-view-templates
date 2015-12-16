package template;

abstract class NodeListBase extends Node {
    protected Node listHead;

    protected NodeListBase() {
    }

    public void add(Node node, Context context) {
        boolean tryMerge = (context == Context.TEXT);
        this.listHead = Node.appendToNodeList(this.listHead, node, tryMerge);
    }

    public boolean isEmpty() {
        return (this.listHead == null);
    }

    public boolean hasOneMember() {
        return (this.listHead != null) && (this.listHead.getNextNode() == null);
    }

    public Node getListHeadMaybe() {
        return this.listHead;
    }

    public void dumpToBuilder(StringBuilder builder, String linePrefix) {
        builder.append(linePrefix).append(this.dumpName()).append(" ("+Node.nodeListLength(this.listHead)).append(" nodes)\n");
        Node node = this.listHead;
        while(node != null) {
            node.dumpToBuilder(builder, linePrefix+"  ");
            node = node.getNextNode();
        }
    }

    abstract protected String dumpName();
}
