package template;

public enum Context {
    UNSAFE,     // no escaping
    TEXT,
    TAG,
    ATTRIBUTE_VALUE,
    URL,
    URL_PATH
}
