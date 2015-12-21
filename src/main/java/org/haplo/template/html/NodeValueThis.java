package org.haplo.template.html;

final class NodeValueThis extends NodeValue {
    static private final String[] THIS = new String[] {};

    public NodeValueThis() {
        super(THIS);
    }

    public void dumpToBuilder(StringBuilder builder, String linePrefix) {
        builder.append(linePrefix).append("VALUE .\n");
    }
}
