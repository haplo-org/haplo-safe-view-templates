package org.haplo.template.html;

abstract public class Driver {
    abstract public Object getRootView();
    abstract public Driver driverWithNewRoot(Object rootView);
    abstract public Object getValueFromView(Object view, String[] path);
    abstract public String valueToStringRepresentation(Object value);
    abstract public void iterateOverValueAsArray(Object value, ArrayIterator iterator) throws RenderException;
    abstract public void iterateOverValueAsDictionary(Object value, DictionaryIterator iterator) throws RenderException;

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

    public interface IncludedTemplateRenderer {
        void renderIncludedTemplate(String templateName, StringBuilder builder, Driver driver, Context context) throws RenderException;
    }

    private IncludedTemplateRenderer includedTemplateRenderer;

    public void setIncludedTemplateRenderer(IncludedTemplateRenderer includedTemplateRenderer) {
        this.includedTemplateRenderer = includedTemplateRenderer;
    }

    public void renderIncludedTemplate(String templateName, StringBuilder builder, Context context) throws RenderException {
        if(this.includedTemplateRenderer == null) {
            throw new RenderException("No IncludedTemplateRenderer available for rendering included templates");
        }
        this.includedTemplateRenderer.renderIncludedTemplate(templateName, builder, this, context);
    }

    public void renderYield(String blockName, StringBuilder builder, Object view, Context context) throws RenderException {
        // TODO: Is it OK to silently ignore yield() calls when nothing was included with a template() ?
        if(this.includedFromBinding == null) { return; }
        this.includedFromBinding.renderBlock(blockName, builder, view, context);
    }

    // ----------------------------------------------------------------------

    public interface FunctionRenderer {
        boolean renderFunction(StringBuilder builder, FunctionBinding binding) throws RenderException;
    }

    private FunctionRenderer functionRenderer;

    public void setFunctionRenderer(FunctionRenderer renderer) {
        this.functionRenderer = renderer;
    }

    protected void renderFunction(StringBuilder builder, FunctionBinding binding) throws RenderException {
        if(     (this.functionRenderer == null) ||
                !this.functionRenderer.renderFunction(builder, binding) ) {
            throw new RenderException("No renderable implementation for function "+binding.getFunctionName()+"()");
        }
    }

    // ----------------------------------------------------------------------

    public static interface ArrayIterator {
        void entry(Object value) throws RenderException;
    }

    public static interface DictionaryIterator {
        void entry(String key, Object value) throws RenderException;
    }

    // ----------------------------------------------------------------------

    final public Driver driverWithNewRootAndCopyOfConfig(Object rootView) {
        Driver driver = this.driverWithNewRoot(rootView);
        driver.includedTemplateRenderer = this.includedTemplateRenderer;
        driver.functionRenderer = this.functionRenderer;
        return driver;
    }

    // ----------------------------------------------------------------------
    // Because the driver is passed to all rendering functions, it is a
    // useful place to store state while rendering. Nodes themselves can't
    // store any state because they need to be thread-safe.
    private boolean driverSetup = false;
    private int numberOfRememberedViews = 0;
    private Object[] rememberedViews;
    private FunctionBinding includedFromBinding;

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

    public void setIncludedFromBinding(FunctionBinding includedFromBinding) {
        if(this.includedFromBinding != null) {
            throw new RuntimeException("Unexpected setIncludedFromBinding(), logic error");
        }
        this.includedFromBinding = includedFromBinding;
    }
}
