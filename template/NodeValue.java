package template;

class NodeValue extends Node {
    private String[] path;

    final static java.util.regex.Pattern SPLIT_REGEX = java.util.regex.Pattern.compile("\\.");

    public NodeValue(String pathString) {
        // TODO: Split more efficiently than a regex?
        this.path = SPLIT_REGEX.split(pathString);
    }

    protected NodeValue(String[] path) {
        this.path = path;
    }

    // Used for validation
    protected String _getFirstPathComponent() {
        return this.path[0];
    }

    public void render(StringBuilder builder, Driver driver, Object view, Context context) {
        Object value = driver.getValueFromView(view, this.path);
        if(value != null) {
            String string = driver.valueToStringRepresentation(value);
            if(string != null) {
                Escape.escape(string, builder, context);
            }
        }
    }

    protected boolean nodeRepresentsValueFromView() {
        return true;
    }

    protected Object value(Driver driver, Object view) {
        return driver.getValueFromView(view, this.path);
    }

    protected void iterateOverValueAsArray(Driver driver, Object view, Driver.ArrayIterator iterator) {
        Object value = driver.getValueFromView(view, this.path);
        if(value != null) {
            driver.iterateOverValueAsArray(value, iterator);
        }
    }

    public void dumpToBuilder(StringBuilder builder, String linePrefix) {
        builder.append(linePrefix).append("VALUE ");
        boolean first = true;
        for(String s : this.path) {
            if(first) { first = false; } else { builder.append('.'); }
            builder.append(s);
        }
        builder.append("\n");
    }
}
