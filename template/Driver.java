package template;

abstract public class Driver {
    abstract public Object getRootView();
    abstract public Object getValueFromView(Object view, String[] path);
    abstract public String valueToStringRepresentation(Object value);
    abstract public Iterable<Object> valueToIterableViewList(Object value);
    abstract public boolean valueIsTruthy(Object value);
    abstract public void renderInclusion(String inclusionName, StringBuilder builder, Object view, Context context);
}
