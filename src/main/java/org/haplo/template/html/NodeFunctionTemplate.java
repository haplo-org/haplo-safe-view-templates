package org.haplo.template.html;

class NodeFunctionTemplate extends NodeFunction {
    private String templateName;

    NodeFunctionTemplate(String templateName) {
        this.templateName = templateName;
    }

    public String getFunctionName() {
        return "template:"+this.templateName;
    }

    public void postParse(Parser parser, int functionStartPos) throws ParseException {
        super.postParse(parser, functionStartPos);
        if(parser.getCurrentParseContext() != Context.TEXT) {
            parser.error("template: functions can only be used in document text");
        }
        if(getArgumentsHead() != null) {
            parser.error("template: functions may not take any arguments");
        }
    }

    public void render(StringBuilder builder, Driver driver, Object view, Context context) throws RenderException {
        // Clone the driver with a new root, so all state is reset for included template
        // and store a binding to this function so yield() works.
        Driver nestedDriver = driver.driverWithNewRoot(view);
        nestedDriver.setIncludedFromBinding(new FunctionBinding(this, driver, view));
        // Delegate the rendering of the included template to the driver, so it can
        // implement driver specific templates, templates written in other languages, etc.
        nestedDriver.renderIncludedTemplate(this.templateName, builder, context);
    }

    public String getDumpName() {
        return "TEMPLATE "+this.templateName;
    }
}
