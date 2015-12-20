package org.haplo.template.html;

class NodeEnclosingView extends Node {
    private int rememberedViewIndex;
    private Node blockHead;

    NodeEnclosingView(int rememberedViewIndex, Node blockHead) {
        this.rememberedViewIndex = rememberedViewIndex;
        this.blockHead = blockHead;
    }

    public void render(StringBuilder builder, Driver driver, Object view, Context context) throws RenderException {
        this.blockHead.renderWithNextNodes(builder, driver, driver.recallView(this.rememberedViewIndex), context);
    }

    protected boolean nodeRepresentsValueFromView() {
        return true;
    }

    protected Object value(Driver driver, Object view) {
        if(this.blockHead.getNextNode() != null) { return null; }
        return this.blockHead.value(driver, driver.recallView(this.rememberedViewIndex));
    }

    protected void iterateOverValueAsArray(Driver driver, Object view, Driver.ArrayIterator iterator) throws RenderException {
        if(this.blockHead.getNextNode() != null) { return; }
        this.blockHead.iterateOverValueAsArray(driver, driver.recallView(this.rememberedViewIndex), iterator);
    }

    public void dumpToBuilder(StringBuilder builder, String linePrefix) {
        builder.append(linePrefix).
                append("ENCLOSING VIEW index=").
                append(this.rememberedViewIndex).
                append('\n');
        this.blockHead.dumpToBuilderWithNextNodes(builder, linePrefix+"  ");
    }
}
