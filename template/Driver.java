package template;

abstract public class Driver {
    abstract public Object getRootView();
    abstract public Driver driverWithNewRoot(Object rootView);
    abstract public Object getValueFromView(Object view, String[] path);
    abstract public String valueToStringRepresentation(Object value);
    abstract public Iterable<Object> valueToIterableViewList(Object value);
    abstract public void renderInclusion(String inclusionName, StringBuilder builder, Object view, Context context);

    public boolean valueIsTruthy(Object value) {
        if(value == null) {
            return false;
        } else if(value instanceof CharSequence) {
            return ((CharSequence)value).length() > 0;
        } else if(value instanceof Boolean) {
            return ((Boolean)value).booleanValue();
        } else if(value instanceof Object[]) {
            return ((Object[])value).length > 0;
        }
        return false;
    }
}
