package org.haplo.template.html;

public interface DeferredRender {
    void renderDeferred(StringBuilder builder, Context context) throws RenderException;
}
