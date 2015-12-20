package org.haplo.template.driver.rhinojs;

import java.util.Map;

import org.haplo.template.html.Driver;
import org.haplo.template.html.Template;
import org.haplo.template.html.Context;
import org.haplo.template.html.RenderException;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.UniqueTag;
import org.mozilla.javascript.ScriptRuntime;

class RhinoJavaScriptDriver extends Driver {
    private Scriptable rootView;
    private Map<String,Template> inclusions;

    public RhinoJavaScriptDriver(Scriptable view, Map<String,Template> inclusions) {
        this.rootView = view;
        this.inclusions = inclusions;
    }

    public Object getRootView() {
        return this.rootView;
    }

    public Driver driverWithNewRoot(Object rootView) {
        if((rootView != null) && !(rootView instanceof Scriptable)) {
            throw new RuntimeException("Unexpected view object when creating driver for new root");
        }
        return new RhinoJavaScriptDriver((Scriptable)rootView, this.inclusions);
    }

    public Object getValueFromView(Object view, String[] path) {
        if(view instanceof Scriptable) {
            Scriptable o = (Scriptable)view;
            for(int i = 0; i < path.length; ++i) {
                if(o == null || (o instanceof Undefined)) { return null; }
                Object value = o.get(path[i], o);
                if(value instanceof Scriptable) {
                    o = (Scriptable)value;
                } else {
                    // Scriptable values can be any Object, allow this in last entry in path
                    if(i == (path.length - 1)) {
                        return hasValue(value) ? value : null;
                    }
                    o = null;
                }
            }
            return hasValue(o) ? o : null;
        } else if(path.length == 0) {
            return view;    // to allow . value to work
        }
        return null;
    }

    public String valueToStringRepresentation(Object value) {
        return hasValue(value) ? value.toString() : null;
    }

    public void iterateOverValueAsArray(Object value, ArrayIterator iterator) throws RenderException {
        // Works on any Array-like JS Object
        int length = isArrayLikeScriptableObject(value);
        if(length == -1) { return; }
        Scriptable scriptable = (Scriptable)value;
        for(int i = 0; i < length; ++i) {
            Object entry = scriptable.get(i, scriptable);
            iterator.entry(hasValue(entry) ? entry : null);
        }
    }

    @SuppressWarnings("unchecked")
    public void iterateOverValueAsDictionary(Object value, DictionaryIterator iterator) throws RenderException {
        if(!(value instanceof Map)) { return; }
        for(Map.Entry<Object,Object> entry : ((Map<Object,Object>)value).entrySet()) {
            iterator.entry(entry.getKey().toString(), entry.getValue());
        }
    }

    public void renderIncludedTemplate(String inclusionName, StringBuilder builder, Context context) throws RenderException {
        // TODO: Error if inclusion not found?
        if(this.inclusions == null) { return; }
        Template template = this.inclusions.get(inclusionName);
        if(template != null) {
            template.renderAsInclusion(builder, this, this.getRootView(), context);
        }
    }

    public boolean valueIsTruthy(Object value) {
        if(value instanceof Scriptable) {
            if(isArrayLikeScriptableObject(value) == 0) {
                return false;   // empty array
            }
            return ScriptRuntime.toBoolean(value);
        } else {
            return super.valueIsTruthy(value);
        }
    }

    // ----------------------------------------------------------------------

    private static boolean hasValue(Object object) {
        return !(
            (object == null) ||
            (object instanceof Undefined) ||
            (object instanceof UniqueTag)
        );
    }

    // Returns -1 if not a JS object which looks like an array, otherwise the length
    private static int isArrayLikeScriptableObject(Object object) {
        if(!(object instanceof Scriptable)) { return -1; }
        Scriptable scriptable = (Scriptable)object;
        Object lengthProperty = scriptable.get("length", scriptable);
        if(!(hasValue(lengthProperty) && (lengthProperty instanceof Number))) { return -1; }
        long lengthL = ((Number)lengthProperty).longValue();
        // JS max array length is actually (2^53)-1, but Rhino uses int
        if(lengthL < 0 || lengthL > Integer.MAX_VALUE) { return -1; }
        return (int)lengthL;
    }
}
