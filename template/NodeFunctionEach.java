package template;

class NodeFunctionEach extends NodeFunction.ChangesView {

    static private final String[] PERMITTED_BLOCK_NAMES = {NodeFunction.BLOCK_ANONYMOUS};

    NodeFunctionEach() {
    }

    public String getFunctionName() {
        return "each";
    }

    protected String[] getPermittedBlockNames() {
        return PERMITTED_BLOCK_NAMES;
    }

    protected boolean requiresAnonymousBlock() {
        return true;
    }

    public void render(StringBuilder builder, Driver driver, Object view, Context context) {
        rememberUnchangedViewIfNecessary(driver, view);
        Node block = getBlock(Node.BLOCK_ANONYMOUS);
        Node arg0 = getSingleArgument();
        Iterable<Object> viewList = arg0.valueIterableViewList(driver, view);
        if(viewList == null) { return; }
        for(Object nestedView : viewList) {
            block.render(builder, driver, nestedView, context);
        }
    }

    public String getDumpName() {
        return "EACH";
    }
}
