package template;

public enum Context {
    UNSAFE,     // no escaping
    TEXT,
    ELEMENT,
    ATTRIBUTE_VALUE
}
