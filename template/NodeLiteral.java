package template;

final class NodeLiteral extends Node {
    private String html;

    public NodeLiteral(String html) {
        this.html = html;
    }

    protected String getLiteralHTML() {
        return this.html;
    }

    protected boolean tryToMergeWith(Node otherNode) {
        if(otherNode instanceof NodeLiteral) {
            this.html += ((NodeLiteral)otherNode).html;
            return true;
        }
        return false;
    }

    public void render(StringBuilder builder, Driver driver, Object view, Context context) {
        builder.append(this.html);
    }

    protected boolean whitelistForLiteralStringOnly() {
        return true;
    }

    public void dumpToBuilder(StringBuilder builder, String linePrefix) {
        builder.append(linePrefix).append("LITERAL ").append(this.html).append("\n");
    }
}
