package org.haplo.template.driver.rhinojs;

import org.mozilla.javascript.Scriptable;

import org.haplo.template.html.ParserConfiguration;
import org.haplo.template.html.Driver;
import org.haplo.template.html.Context;
import org.haplo.template.html.FunctionBinding;
import org.haplo.template.html.RenderException;

public class JSPlatformIntegration {
    // Parser configuration for JS templates
    public static ParserConfiguration parserConfiguration;

    // Implementation of platform included template rendering
    public static JSIncludedTemplateRenderer includedTemplateRenderer;

    public interface JSIncludedTemplateRenderer {
        void renderIncludedTemplate(Scriptable owner, String templateName, StringBuilder builder, Driver driver, Context context) throws RenderException;
    }

    // Implementation of default platform functions
    public static JSFunctionRenderer platformFunctionRenderer;

    public interface JSFunctionRenderer {
        boolean renderFunction(Scriptable owner, StringBuilder builder, FunctionBinding binding) throws RenderException;
    }
}
