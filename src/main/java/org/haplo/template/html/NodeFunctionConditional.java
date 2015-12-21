package org.haplo.template.html;

final class NodeFunctionConditional extends NodeFunction.ExactlyOneValueArgument {
    private boolean inverse;

    static private final String[] PERMITTED_BLOCK_NAMES = {NodeFunction.BLOCK_ANONYMOUS, "else"};

    NodeFunctionConditional(boolean inverse) {
        this.inverse = inverse;
    }

    public String getFunctionName() {
        return this.inverse ? "unless" : "if";
    }

    protected String[] getPermittedBlockNames() {
        return PERMITTED_BLOCK_NAMES;
    }

    protected boolean requiresAnonymousBlock() {
        return true;
    }

    public void render(StringBuilder builder, Driver driver, Object view, Context context) throws RenderException {
        Object testValue = getSingleArgument().value(driver, view);
        boolean renderAnonBlock = driver.valueIsTruthy(testValue);
        if(this.inverse) { renderAnonBlock = !renderAnonBlock; }
        Node block = getBlock(renderAnonBlock ? Node.BLOCK_ANONYMOUS : "else");
        if(block != null) {
            block.render(builder, driver, view, context);
        }
    }

    protected boolean whitelistForLiteralStringOnly() {
        return checkBlocksWhitelistForLiteralStringOnly();
    }

    public String getDumpName() {
        return this.inverse ? "UNLESS" : "IF";
    }
}
