package template;

class NodeLiteral extends Node {
    private String html;

    public NodeLiteral(String html) {
        this.html = html;
    }

    protected String getLiteralHTML() {
        return this.html;
    }

    public void render(StringBuilder builder, Driver driver, Object view, Context context) {
        builder.append(this.html);
    }

    public void dumpToBuilder(StringBuilder builder, String linePrefix) {
        builder.append(linePrefix).append("LITERAL ").append(this.html).append("\n");
    }
}
