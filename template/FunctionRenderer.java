package template;

public interface FunctionRenderer {
    boolean renderFunction(StringBuilder builder, FunctionBinding binding) throws RenderException;
}
