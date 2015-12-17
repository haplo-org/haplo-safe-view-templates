package template;

public class FunctionBinding {
    private NodeFunction function;
    private Driver driver;
    private Object view;

    FunctionBinding(NodeFunction function, Driver driver, Object view) {
        this.function = function;
        this.driver = driver;
        this.view = view;
    }

    protected void renderBlock(String blockName, StringBuilder builder, Object view, Context context) {
        Node block = this.function.getBlock(blockName);
        if(block != null) {
            block.render(builder, this.driver, this.view, context);
        }
    }
}
