package org.haplo.template.driver.nestedjava;

import java.util.Map;

import org.haplo.template.html.Driver;
import org.haplo.template.html.Template;
import org.haplo.template.html.Context;
import org.haplo.template.html.RenderException;

class NestedJavaDriver extends Driver {
    private Object rootView;

    public NestedJavaDriver(Object view) {
        this.rootView = view;
    }

    public Driver driverWithNewRoot(Object rootView) {
        return new NestedJavaDriver(rootView);
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
}
