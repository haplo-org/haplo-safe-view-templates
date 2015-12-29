package org.haplo.template.driver.rhinojs;

import org.mozilla.javascript.Scriptable;

import org.haplo.template.html.ParserConfiguration;
import org.haplo.template.html.Driver;

public class JSPlatformIntegration {
    // Parser configuration for JS templates
    public static ParserConfiguration parserConfiguration;

    // Implementation of default platform functions
    public static Driver.FunctionRenderer platformFunctionRenderer;
}
