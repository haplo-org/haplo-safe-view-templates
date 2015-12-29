package org.haplo.template.driver.rhinojs;

import org.haplo.template.html.Parser;
import org.haplo.template.html.Template;
import org.haplo.template.html.Driver;
import org.haplo.template.html.ParseException;
import org.haplo.template.html.RenderException;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrappedException;

public class HaploTemplate extends ScriptableObject implements Callable {
    private Template template;
    private Driver.IncludedTemplateRenderer includedTemplateRenderer;

    public String getClassName() {
        return "$HaploTemplate";
    }

    public void jsConstructor(String source, String name) throws ParseException {
        this.template = new Parser(source, name, JSPlatformIntegration.parserConfiguration).parse();
    }

    public Template getTemplate() {
        return this.template;
    }

    public void setIncludedTemplateRenderer(Driver.IncludedTemplateRenderer renderer) {
        this.includedTemplateRenderer = renderer;
    }

    public String jsFunction_render(Object view) throws RenderException {
        RhinoJavaScriptDriver driver = createDriver(view);
        if(this.template == null) { throw new RenderException(driver, "No template"); }
        return this.template.renderString(driver);
    }

    // Can also call the template as a function
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, java.lang.Object[] args) {
        try {
            return jsFunction_render((args.length == 0) ? null : args[0]);
        } catch(RenderException e) {
            throw new WrappedException(e);
        }
    }

    public Scriptable jsFunction_deferredRender(Object view) throws RenderException {
        RhinoJavaScriptDriver driver = createDriver(view);
        if(this.template == null) { throw new RenderException(driver, "No template"); }
        HaploTemplateDeferredRender deferred =
            (HaploTemplateDeferredRender)Context.getCurrentContext().
                newObject(this.getParentScope(), "$HaploTemplateDeferredRender");
        deferred.setDeferredRender(this.template.deferredRender(driver));
        return deferred;
    }

    protected RhinoJavaScriptDriver createDriver(Object view) {
        RhinoJavaScriptDriver driver = new RhinoJavaScriptDriver(view);
        if(this.includedTemplateRenderer != null) {
            driver.setIncludedTemplateRenderer(this.includedTemplateRenderer);
        }
        driver.setFunctionRenderer(new JSFunctionRenderer(this));
        return driver;
    }
}
