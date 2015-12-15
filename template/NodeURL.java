package template;

import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

final class NodeURL extends NodeListBase {
    private ArrayList<ParamInst> parameters;

    public NodeURL() {
    }

    // ----------------------------------------------------------------------

    public void addParameterInstructionAddKeyValue(String key, Node value) {
        ParamInst inst = addParameterInstruction();
        inst.key = key;
        inst.value = value;
    }

    public void addParameterInstructionRemoveKey(String key) {
        ParamInst inst = addParameterInstruction();
        inst.key = key;
        inst.remove = true;
    }

    public void addParameterInstructionAllFromDictionary(Node value) {
        ParamInst inst = addParameterInstruction();
        inst.value = value;
    }

    // ----------------------------------------------------------------------

    private ParamInst addParameterInstruction() {
        if(this.parameters == null) {
            this.parameters = new ArrayList<ParamInst>(16);
        }
        ParamInst inst = new ParamInst();
        this.parameters.add(inst);
        return inst;
    }

    private static class ParamInst {
        public String key;
        public boolean remove;
        public Node value;
    }

    // ----------------------------------------------------------------------

    public boolean allowedInURLContext() {
        return false;   // can't nest URLs
    }

    public void render(StringBuilder builder, Driver driver, Object view, Context context) {
        Context urlContext = Context.URL_PATH;
        for(Node node : this.nodes) {
            node.render(builder, driver, view, urlContext);
            urlContext = Context.URL;
        }
        if(this.parameters != null) {
            // Use LinkedHashMap to preserve order of parameters
            LinkedHashMap<String,String> params = new LinkedHashMap<String,String>(16);
            for(ParamInst inst : this.parameters) {
                if(inst.remove) {
                    params.remove(inst.key);
                } else if(inst.key != null) {
                    StringBuilder valueBuilder = new StringBuilder();
                    inst.value.render(valueBuilder, driver, view, Context.UNSAFE);
                    if(valueBuilder.length() > 0) {
                        params.put(inst.key, valueBuilder.toString());
                    }
                } else {
                    driver.iterateOverValueAsDictionary(inst.value.value(driver, view), (key, value) -> {
                        String valueString = driver.valueToStringRepresentation(value);
                        if((valueString != null) && (valueString.length() > 0)) {
                            params.put(key, valueString);
                        }
                    });
                }
            }
            char separator = '?';
            for(Map.Entry<String,String> p : params.entrySet()) {
                builder.append(separator);
                Escape.escape(p.getKey(), builder, Context.URL);
                builder.append('=');
                Escape.escape(p.getValue(), builder, Context.URL);
                separator = '&';
            }
        }
    }

    public void dumpToBuilder(StringBuilder builder, String linePrefix) {
        super.dumpToBuilder(builder, linePrefix);
        if(this.parameters != null) {
            builder.append(linePrefix).append("  PARAMETERS\n");
            for(ParamInst inst : this.parameters) {
                if(inst.remove) {
                    builder.append(linePrefix).append("    REMOVE '").append(inst.key).append("'\n");
                } else if(inst.key != null) {
                    builder.append(linePrefix).append("    SET '").append(inst.key).append("' to\n");
                } else {
                    builder.append(linePrefix).append("    ADD ALL KEYS IN\n");
                }
                if(inst.value != null) {
                    inst.value.dumpToBuilder(builder, linePrefix+"      ");
                }
            }
        }
    }

    protected String dumpName() {
        return "URL";
    }
}
