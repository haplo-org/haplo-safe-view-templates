package org.haplo.template.driver.rhinojs;

import org.haplo.template.html.Context;
import org.haplo.template.html.DeferredRender;
import org.haplo.template.html.RenderException;

import org.mozilla.javascript.ScriptableObject;

public class HaploTemplateDeferredRender extends ScriptableObject implements DeferredRender {
    private DeferredRender deferredRender;

    public String getClassName() {
        return "$HaploTemplateDeferredRender";
    }

    protected void setDeferredRender(DeferredRender deferredRender) {
        this.deferredRender = deferredRender;
    }

    public void renderDeferred(StringBuilder builder, Context context) throws RenderException {
        if(this.deferredRender != null) {
            this.deferredRender.renderDeferred(builder, context);
        }
    }
}
