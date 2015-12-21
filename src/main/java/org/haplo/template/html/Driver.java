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

    final public void setParentDriver(Driver parentDriver) {
        this.parentDriver = parentDriver;
    }

    final public Driver getParentDriver() {
        return this.parentDriver;
    }

    final public Template getTemplate() {
        return this.template;
    }

    // Some RenderExceptions are thrown when there isn't a template. This is usually
    // the fault of the last template which was rendered, so this function is used
    // to blame something which can be used for tracking down the problem.
    final public Template getLastTemplate() {
        Driver search = this;
        while(search != null) {
            if(search.template != null) { return search.template; }
            search = search.parentDriver;
        }
        return null;
    }

    // ----------------------------------------------------------------------

    public interface IncludedTemplateRenderer {
        void renderIncludedTemplate(String templateName, StringBuilder builder, Driver driver, Context context) throws RenderException;
    }

    private IncludedTemplateRenderer includedTemplateRenderer;

    final public void setIncludedTemplateRenderer(IncludedTemplateRenderer includedTemplateRenderer) {
        this.includedTemplateRenderer = includedTemplateRenderer;
    }

    final public void renderIncludedTemplate(String templateName, StringBuilder builder, Context context) throws RenderException {
        if(this.includedTemplateRenderer == null) {
            throw new RenderException(this, "No IncludedTemplateRenderer available for rendering included templates");
        }
        this.includedTemplateRenderer.renderIncludedTemplate(templateName, builder, this, context);
    }

    final public void renderYield(String blockName, StringBuilder builder, Object view, Context context) throws RenderException {
        if(this.includedFromBinding == null) {
            throw new RenderException(this, "yield() used in a template which isn't being included in another template");
        }
        this.includedFromBinding.renderBlock(blockName, builder, view, context);
    }

    // ----------------------------------------------------------------------

    public interface FunctionRenderer {
        boolean renderFunction(StringBuilder builder, FunctionBinding binding) throws RenderException;
    }

    private FunctionRenderer functionRenderer;

    final public void setFunctionRenderer(FunctionRenderer renderer) {
        this.functionRenderer = renderer;
    }

    final protected void renderFunction(StringBuilder builder, FunctionBinding binding) throws RenderException {
        if(     (this.functionRenderer == null) ||
                !this.functionRenderer.renderFunction(builder, binding) ) {
            throw new RenderException(this, "No renderable implementation for function "+binding.getFunctionName()+"()");
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
    private Template template;
    private Driver parentDriver;
    private Object[] rememberedViews;
    private FunctionBinding includedFromBinding;

    final public void setupForRender(Template template) {
        if(this.template != null) {
            throw new RuntimeException("Can't use same Driver twice");
        }
        this.template = template;
    }

    final public void rememberView(int index, Object view) {
        if(this.rememberedViews == null) {
            // Allocate remembered views on demand
            int numberOfRememberedViews = (this.template == null) ? -1 : this.template.getNumberOfRememberedViews();
            if(numberOfRememberedViews <= 0) {
                throw new RuntimeException("Unexpected rememberView(), logic error");
            }
            this.rememberedViews = new Object[numberOfRememberedViews];
        }
        this.rememberedViews[index] = view;
    }

    final public Object recallView(int index) {
        return (this.rememberedViews == null) ? null : this.rememberedViews[index];
    }

    final public void setIncludedFromBinding(FunctionBinding includedFromBinding) {
        if(this.includedFromBinding != null) {
            throw new RuntimeException("Unexpected setIncludedFromBinding(), logic error");
        }
        this.includedFromBinding = includedFromBinding;
    }
}
