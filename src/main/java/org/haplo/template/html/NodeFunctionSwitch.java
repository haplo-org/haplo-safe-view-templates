package org.haplo.template.html;

final class NodeFunctionSwitch extends NodeFunction.ExactlyOneArgument {
    NodeFunctionSwitch() {
    }

    public String getFunctionName() {
        return "switch";
    }

    public void render(StringBuilder builder, Driver driver, Object view, Context context) throws RenderException {
        StringBuilder blockName = new StringBuilder(240);
        getSingleArgument().render(blockName, driver, view, Context.UNSAFE);
        Node block = getBlock(blockName.toString());
        if(block == null) {
            block = getBlock(Node.BLOCK_ANONYMOUS);
        }
        if(block != null) {
            block.render(builder, driver, view, context);
        }
    }

    protected boolean whitelistForLiteralStringOnly() {
        return checkBlocksWhitelistForLiteralStringOnly();
    }

    public String getDumpName() {
        return "FUNCTION switch()";
    }
}
