package org.haplo.template.html;

final public class FunctionBinding {
    private NodeFunction function;
    private Driver driver;
    private Object view;
    private Context context;
    private Node nextArgument;
    private int argumentCount;

    FunctionBinding(NodeFunction function, Driver driver, Object view, Context context) {
        this.function = function;
        this.driver = driver;
        this.view = view;
        this.context = context;
        this.restartArguments();
    }

    public String getFunctionName() {
        return this.function.getFunctionName();
    }

    public Driver getDriver() {
        return this.driver;
    }

    public Object getView() {
        return this.view;
    }

    public Context getContext() {
        return this.context;
    }

    // ----------------------------------------------------------------------

    public Object[] allValueArguments() {
        int count = 0;
        Node argument = this.function.getArgumentsHead();
        while(argument != null) {
            count++;
            argument = argument.getNextNode();
        }
        Object[] arguments = new Object[count];
        argument = this.function.getArgumentsHead();
        int i = 0;
        while(argument != null) {
            arguments[i] = argument.valueForFunctionArgument(this.driver, this.view);
            i++;
            argument = argument.getNextNode();
        }
        return arguments;
    }

    // ----------------------------------------------------------------------

    public enum ArgumentRequirement {
        OPTIONAL,
        REQUIRED
    }

    public boolean hasArguments() {
        return (this.function.getArgumentsHead() != null);
    }

    public void restartArguments() {
        this.nextArgument = this.function.getArgumentsHead();
        this.argumentCount = 0;
    }

    public String nextUnescapedStringArgument(ArgumentRequirement requirement) throws RenderException {
        Node arg = getNextArg(requirement);
        if(arg == null) { return null; }
        StringBuilder builder = new StringBuilder(128);
        arg.render(builder, this.driver, this.view, Context.UNSAFE);
        return builder.toString();
    }

    public Object nextViewObjectArgument(ArgumentRequirement requirement) throws RenderException {
        Node arg = getNextArg(requirement);
        if(arg == null) { return null; }
        return arg.valueForFunctionArgument(this.driver, this.view);
    }

    public void skipArgument(ArgumentRequirement requirement) throws RenderException {
        getNextArg(requirement);
    }

    public void noMoreArgumentsExpected() throws RenderException {
        if(this.nextArgument != null) {
            throw new RenderException(driver, "Too many arguments for "+this.getFunctionName()+"()");
        }
    }

    protected Node getNextArg(ArgumentRequirement requirement) throws RenderException {
        Node arg = this.nextArgument;
        if(arg == null) {
            if(requirement == ArgumentRequirement.REQUIRED) {
                throw new RenderException(driver, "Argument "+(this.argumentCount+1)+" expected for "+this.getFunctionName()+"()");
            }
        } else {
            this.argumentCount++;
            this.nextArgument = arg.getNextNode();
        }
        return arg;
    }

    // ----------------------------------------------------------------------

    public boolean hasBlock(String blockName) {
        return null != this.function.getBlock(blockName);
    }

    public void renderBlock(String blockName, StringBuilder builder, Object view, Context context) throws RenderException {
        Node block = this.function.getBlock(blockName);
        if(block != null) {
            block.render(builder, this.driver, this.view, context);
        }
    }
}
