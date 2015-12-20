package org.haplo.template.html;

class NodeFunctionWithin extends NodeFunction.ChangesView {

    static private final String[] PERMITTED_BLOCK_NAMES = {NodeFunction.BLOCK_ANONYMOUS};

    NodeFunctionWithin() {
    }

    public String getFunctionName() {
        return "within";
    }

    protected String[] getPermittedBlockNames() {
        return PERMITTED_BLOCK_NAMES;
    }

    protected boolean requiresAnonymousBlock() {
        return true;
    }

    public void render(StringBuilder builder, Driver driver, Object view, Context context) throws RenderException {
        rememberUnchangedViewIfNecessary(driver, view);
        Object nestedView = getSingleArgument().value(driver, view);
        if(nestedView == null) { return; }
        Node block = getBlock(Node.BLOCK_ANONYMOUS);
        block.render(builder, driver, nestedView, context);
    }

    public String getDumpName() {
        return "WITHIN";
    }
}
