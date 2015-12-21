package org.haplo.template.html;

public interface DeferredRender {
    void render(StringBuilder builder, Context context) throws RenderException;
}
