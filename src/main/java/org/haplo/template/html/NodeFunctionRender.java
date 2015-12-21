package org.haplo.template.html;

final class NodeFunctionRender extends NodeFunction.ExactlyOneValueArgument {

    static private final String[] PERMITTED_BLOCK_NAMES = {};

    NodeFunctionRender() {
    }

    public String getFunctionName() {
        return "render";
    }

    protected String[] getPermittedBlockNames() {
        return PERMITTED_BLOCK_NAMES;
    }

    public void postParse(Parser parser, int functionStartPos) throws ParseException {
        super.postParse(parser, functionStartPos);
        if(parser.getCurrentParseContext() != Context.TEXT) {
            parser.error("render() cannot be used in this context", functionStartPos);
        }
    }

    public void render(StringBuilder builder, Driver driver, Object view, Context context) throws RenderException {
        Object value = getSingleArgument().value(driver, view);
        if(value instanceof DeferredRender) {
            ((DeferredRender)value).render(builder, context);
        } else if(value != null) {
            throw new RenderException(driver, "Can't use render() on the value found in the view");
        }
    }

    public String getDumpName() {
        return "RENDER";
    }
}
