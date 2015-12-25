package org.haplo.template.driver.rhinojs;

import org.mozilla.javascript.Scriptable;

import org.haplo.template.html.Driver;

public class JSPlatformIntegration {

    // What scope should new JS objects be created in?
    public static Scope scope = (referenceObject) -> referenceObject.getParentScope();

    public interface Scope {
        Scriptable rootScope(Scriptable referenceObject);
    }

    // Implementation of default platform functions
    public static Driver.FunctionRenderer platformFunctionRenderer;
}
