package org.haplo.template.driver.nestedjava;

import java.util.Map;

import org.haplo.template.html.Driver;
import org.haplo.template.html.Template;
import org.haplo.template.html.Context;
import org.haplo.template.html.RenderException;

class NestedJavaDriver extends Driver {
    private Object rootView;
    private Map<String,Template> inclusions;

    public NestedJavaDriver(Object view, Map<String,Template> inclusions) {
        this.rootView = view;
        this.inclusions = inclusions;
    }

    public Driver driverWithNewRoot(Object rootView) {
        return new NestedJavaDriver(rootView, this.inclusions);
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

    public void iterateOverValueAsArray(Object value, ArrayIterator iterator) throws RenderException {
        if(!(value instanceof Object[])) { return; }
        for(Object entry : ((Object[])value)) {
            iterator.entry(entry);
        }
    }

    @SuppressWarnings("unchecked")
    public void iterateOverValueAsDictionary(Object value, DictionaryIterator iterator) throws RenderException {
        if(!(value instanceof Map)) { return; }
        for(Map.Entry<String,Object> entry : ((Map<String,Object>)value).entrySet()) {
            iterator.entry(entry.getKey(), entry.getValue());
        }
    }

    // Uses default valueIsTruthy() implementation

    public void renderIncludedTemplate(String inclusionName, StringBuilder builder, Context context) throws RenderException {
        // TODO: Error if inclusion not found?
        if(this.inclusions == null) { return; }
        Template template = this.inclusions.get(inclusionName);
        if(template != null) {
            template.renderAsInclusion(builder, this, this.getRootView(), context);
        }
    }
}
