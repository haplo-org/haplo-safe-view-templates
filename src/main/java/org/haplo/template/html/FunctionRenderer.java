package org.haplo.template.html;

public interface FunctionRenderer {
    boolean renderFunction(StringBuilder builder, FunctionBinding binding) throws RenderException;
}
