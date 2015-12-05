package template;

class NodeFunctionCase extends NodeFunction.ExactlyOneArgument {
    NodeFunctionCase() {
    }

    public String getFunctionName() {
        return "case";
    }

    public void render(StringBuilder builder, Driver driver, Object view, Context context) {
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

    public String getDumpName() {
        return "FUNCTION case()";
    }
}
