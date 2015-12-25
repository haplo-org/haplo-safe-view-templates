package org.haplo.template.html;

final class NodeFunctionDo extends NodeFunction {
    NodeFunctionDo() {
    }

    public String getFunctionName() {
        return "do";
    }

    public void postParse(Parser parser, int functionStartPos) throws ParseException {
        super.postParse(parser, functionStartPos);
        if(getArgumentsHead() != null) {
            parser.error("do() must not take any arguments");
        }
        if(getBlock(Node.BLOCK_ANONYMOUS) == null) {
            parser.error("do() requires an anonymous block");
        }
    }

    public void render(StringBuilder builder, Driver driver, Object view, Context context) throws RenderException {
        Node block = getBlock(Node.BLOCK_ANONYMOUS);
        Driver nestedDriver = driver.newNestedDriverWithView(view);
        nestedDriver.setBindingForYield(new FunctionBinding(this, driver, view, context));
        block.render(builder, nestedDriver, view, context);
    }

    public String getDumpName() {
        return "DO";
    }
}
