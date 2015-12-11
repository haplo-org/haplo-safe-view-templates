package template;

class NodeEnclosingView extends Node implements ValueNode {
    private int rememberedViewIndex;
    private Node block;

    NodeEnclosingView(int rememberedViewIndex, Node block) {
        this.rememberedViewIndex = rememberedViewIndex;
        this.block = block;
    }

    public void render(StringBuilder builder, Driver driver, Object view, Context context) {
        this.block.render(builder, driver, driver.recallView(this.rememberedViewIndex), context);
    }

    protected Object value(Driver driver, Object view) {
        return this.block.value(driver, driver.recallView(this.rememberedViewIndex));
    }

    protected Iterable<Object> valueIterableViewList(Driver driver, Object view) {
        return this.block.valueIterableViewList(driver, driver.recallView(this.rememberedViewIndex));
    }

    public void dumpToBuilder(StringBuilder builder, String linePrefix) {
        builder.append(linePrefix).
                append("ENCLOSING VIEW index=").
                append(this.rememberedViewIndex).
                append('\n');
        this.block.dumpToBuilder(builder, linePrefix+"  ");
    }
}
