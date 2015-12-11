package template;

abstract public class Driver {
    abstract public Object getRootView();
    abstract public Driver driverWithNewRoot(Object rootView);
    abstract public Object getValueFromView(Object view, String[] path);
    abstract public String valueToStringRepresentation(Object value);
    abstract public Iterable<Object> valueToIterableViewList(Object value);
    abstract public void renderInclusion(String inclusionName, StringBuilder builder, Context context);

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

    // ----------------------------------------------------------------------
    // Because the driver is passed to all rendering functions, it is a
    // useful place to store state while rendering. Nodes themselves can't
    // store any state because they need to be thread-safe.
    private boolean driverSetup = false;
    private int numberOfRememberedViews = 0;
    private Object[] rememberedViews;

    public void setupForRender(int numberOfRememberedViews) {
        if(this.driverSetup) {
            throw new RuntimeException("Can't use same Driver twice");
        }
        this.numberOfRememberedViews = numberOfRememberedViews;
        this.driverSetup = true;
    }

    public void rememberView(int index, Object view) {
        if(this.rememberedViews == null) {
            // Allocate remembered views on demand
            if(this.numberOfRememberedViews <= 0) {
                throw new RuntimeException("Unexpected rememberView(), logic error");
            }
            this.rememberedViews = new Object[this.numberOfRememberedViews];
        }
        this.rememberedViews[index] = view;
    }

    public Object recallView(int index) {
        return (this.rememberedViews == null) ? null : this.rememberedViews[index];
    }
}
