package org.haplo.template.html;

final class NodeFunctionYield extends NodeFunction {
    private String blockName;

    NodeFunctionYield(String blockName) {
        this.blockName = blockName;
    }

    public String getFunctionName() {
        return (this.blockName == Node.BLOCK_ANONYMOUS) ? "yield" : "yield:"+this.blockName;
    }

    public void postParse(Parser parser, int functionStartPos) throws ParseException {
        super.postParse(parser, functionStartPos);
        if(parser.getCurrentParseContext() != Context.TEXT) {
            parser.error("yield: functions can only be used in document text");
        }
        if(getArgumentsHead() != null) {
            parser.error("yield: functions may not take any arguments");
        }
    }

    public void render(StringBuilder builder, Driver driver, Object view, Context context) throws RenderException {
        driver.renderYield(this.blockName, builder, view, context);
    }

    public String getDumpName() {
        return (this.blockName == Node.BLOCK_ANONYMOUS) ? "YIELD ANONYMOUS" : "YIELD "+this.blockName;
    }
}
