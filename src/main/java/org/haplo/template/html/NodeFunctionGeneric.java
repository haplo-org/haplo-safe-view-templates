package org.haplo.template.html;

class NodeFunctionGeneric extends NodeFunction {
    private String name;

    NodeFunctionGeneric(String name) {
        this.name = name;
    }

    public String getFunctionName() {
        return this.name;
    }

    public void render(StringBuilder builder, Driver driver, Object view, Context context) throws RenderException {
        driver.renderFunction(builder, new FunctionBinding(this, driver, view));
    }

    public String getDumpName() {
        return "FUNCTION "+this.name+"()";
    }
}
