package org.haplo.template.html;

public enum Context {
    UNSAFE,     // no escaping
    FUNCTION_ARGUMENTS,
    TEXT,
    TAG,
    ATTRIBUTE_VALUE,
    URL,
    URL_PATH
}
