package template;

public class Node {
    protected final static String BLOCK_ANONYMOUS = ""; // used as key & object identity comparison

    public void render(StringBuilder builder, Driver driver, Object view, Context context) {
        // TODO: Make Node's render() function abstract
    }

    protected Object value(Driver driver, Object view) {
        return null;
    }

    protected Iterable<Object> valueIterableViewList(Driver driver, Object view) {
        return null;
    }

    public void dumpToBuilder(StringBuilder builder, String linePrefix) {
        builder.append(linePrefix);
        builder.append("UNKNOWN\n");
    }
}
