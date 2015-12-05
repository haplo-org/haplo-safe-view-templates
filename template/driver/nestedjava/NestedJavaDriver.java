package template.driver.nestedjava;

import java.util.Map;
import java.util.Arrays;

import template.Driver;
import template.Template;
import template.Context;

class NestedJavaDriver extends Driver {
    private Object rootView;
    private Map<String,Template> inclusions;

    public NestedJavaDriver(Object view, Map<String,Template> inclusions) {
        this.rootView = view;
        this.inclusions = inclusions;
    }

    public Object getRootView() {
        return this.rootView;
    }

    @SuppressWarnings("unchecked")
    public Object getValueFromView(Object view, String[] path) {
        Object o = view;
        for(String key : path) {
            if(o == null) { return null; }
            if(o instanceof Map) {
                o = ((Map<String,Object>)o).get(key);
            } else {
                o = null; // can't traverse
            }
        }
        return o;
    }

    public String valueToStringRepresentation(Object value) {
        return (value == null) ? null : value.toString();
    }

    public Iterable<Object> valueToIterableViewList(Object value) {
        if(!(value instanceof Object[])) { return null; }
        return Arrays.asList((Object[])value);
    }

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

    public void renderInclusion(String inclusionName, StringBuilder builder, Object view, Context context) {
        // TODO: Error if inclusion not found?
        if(this.inclusions == null) { return; }
        Template template = this.inclusions.get(inclusionName);
        if(template != null) {
            template.renderAsInclusion(builder, this, view, context);
        }
    }
}
