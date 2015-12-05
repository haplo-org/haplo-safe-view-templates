package template;

class NodeFunctionGeneric extends NodeFunction {
    private String name;

    NodeFunctionGeneric(String name) {
        this.name = name;
    }

    public String getFunctionName() {
        return this.name;
    }

    public String getDumpName() {
        return "FUNCTION "+this.name+"()";
    }
}
