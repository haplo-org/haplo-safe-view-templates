package template;

import java.util.ArrayList;

class NodeScriptTag extends Node {
    private NodeURL url;

    protected NodeScriptTag(NodeURL url) {
        this.url = url;
    }

    public void render(StringBuilder builder, Driver driver, Object view, Context context) {
        int tagStart = builder.length();
        builder.append("<script src=\"");
        int attrStart = builder.length();
        this.url.render(builder, driver, view, Context.ATTRIBUTE_VALUE);
        if(builder.length() == attrStart) {
            // Script tags with an empty src are a bad idea
            builder.setLength(tagStart);
            return;
        }
        builder.append("\"></script>");
    }

    public void dumpToBuilder(StringBuilder builder, String linePrefix) {
        builder.append(linePrefix).append("SCRIPT TAG src=\n");
        this.url.dumpToBuilder(builder, linePrefix+"  ");
    }
}
