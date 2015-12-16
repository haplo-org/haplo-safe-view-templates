package template;

public class Node {
    protected final static String BLOCK_ANONYMOUS = ""; // used as key & object identity comparison

    // Used during parsing, not rendering
    public boolean allowedInURLContext() {
        return true;
    }

    public void render(StringBuilder builder, Driver driver, Object view, Context context) {
        // TODO: Make Node's render() function abstract
    }

    protected boolean nodeRepresentsValueFromView() {
        return false;
    }

    protected Object value(Driver driver, Object view) {
        return null;
    }

    protected Iterable<Object> valueIterableViewList(Driver driver, Object view) {
        return null;
    }

    protected Node orSimplifiedNode() {
        return this;
    }

    protected boolean tryToMergeWith(Node otherNode) {
        return false;
    }

    protected boolean whitelistForLiteralStringOnly() {
        return false;
    }

    public void dumpToBuilder(StringBuilder builder, String linePrefix) {
        builder.append(linePrefix);
        builder.append("UNKNOWN\n");
    }

    // ----------------------------------------------------------------------
    // Node list support
    private Node nextNode;

    final public Node getNextNode() {
        return this.nextNode;
    }

    final public void setNextNode(Node nextNode) {
        this.nextNode = nextNode;
    }

    protected static Node lastNodeInNodeList(Node maybeListHead) {
        if(maybeListHead == null) { return null; }
        Node scan = maybeListHead;
        while(true) {
            Node next = scan.getNextNode();
            if(next == null) { return scan; }
            scan = next;
        }
    }

    // Usage: this.listHead = Node.appendToNodeList(this.listHead, node, false|true);
    protected static Node appendToNodeList(Node maybeListHead, Node node, boolean tryMerge) {
        Node lastNode = lastNodeInNodeList(maybeListHead);
        if(lastNode == null) {
            return node; // list was empty, node is head of list
        } else {
            if(!(tryMerge && lastNode.tryToMergeWith(node))) {
                lastNode.setNextNode(node);
            }
            return maybeListHead; // head of list unchanged
        }
    }

    protected static int nodeListLength(Node maybeListHead) {
        int count = 0;
        Node scan = maybeListHead;
        while(scan != null) {
            count++;
            scan = scan.getNextNode();
        }
        return count;
    }
}
