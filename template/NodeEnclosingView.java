package template;

class NodeEnclosingView extends Node {
    private int rememberedViewIndex;
    private Node block;

    NodeEnclosingView(int rememberedViewIndex, Node block) {
        this.rememberedViewIndex = rememberedViewIndex;
        this.block = block;
    }

    public void render(StringBuilder builder, Driver driver, Object view, Context context) {
        this.block.render(builder, driver, driver.recallView(this.rememberedViewIndex), context);
    }

    protected boolean nodeRepresentsValueFromView() {
        return true;
    }

    protected Object value(Driver driver, Object view) {
        return this.block.value(driver, driver.recallView(this.rememberedViewIndex));
    }

    protected void iterateOverValueAsArray(Driver driver, Object view, Driver.ArrayIterator iterator) {
        this.block.iterateOverValueAsArray(driver, driver.recallView(this.rememberedViewIndex), iterator);
    }

    public void dumpToBuilder(StringBuilder builder, String linePrefix) {
        builder.append(linePrefix).
                append("ENCLOSING VIEW index=").
                append(this.rememberedViewIndex).
                append('\n');
        this.block.dumpToBuilder(builder, linePrefix+"  ");
    }
}
