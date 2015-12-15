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

    protected Object value(Driver driver, Object view) {
        return null;
    }

    protected Iterable<Object> valueIterableViewList(Driver driver, Object view) {
        return null;
    }

    protected Node orSimplifiedNode() {
        return this;
    }

    protected boolean whitelistForLiteralStringOnly() {
        return false;
    }

    public void dumpToBuilder(StringBuilder builder, String linePrefix) {
        builder.append(linePrefix);
        builder.append("UNKNOWN\n");
    }
}
