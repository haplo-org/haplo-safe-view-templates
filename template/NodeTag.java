package template;

import java.util.ArrayList;

class NodeTag extends Node {
    private String name;
    private String start;
    private ArrayList<Attribute> attributes;

    public NodeTag(String name) {
        // TODO: store both name and 'start'?
        this.name = name;
        this.start = "<"+name;
    }

    public boolean allowedInURLContext() {
        return false;   // caught by Context.TEXT check as well
    }

    public String getName() {
        return this.name;
    }

    public void addAttribute(String name, Node value, Context valueContext) {
        if(value instanceof NodeLiteral) {
            // Value is just a literal string, so can be optimised
            // Literal values should not be escaped, because the author is trusted
            // TODO: Linter should check literal values don't contain bad things
            String attributeValue = ((NodeLiteral)value).getLiteralHTML();
            if(canOmitQuotesForValue(attributeValue)) {
                this.start += " "+name+"="+attributeValue;
            } else {
                this.start += " "+name+"=\""+attributeValue+'"';
            }
            return;
        }
        if(this.attributes == null) {
            this.attributes = new ArrayList<Attribute>(6);
        }
        Attribute attribute = new Attribute();
        attribute.name = name;
        attribute.preparedNameEquals = " "+name+"=\"";
        attribute.value = value;
        // If a URL, and the value is a single NodeValue element, it has to output as a URL path
        if((valueContext == Context.URL) && (value instanceof NodeValue)) {
            valueContext = Context.URL_PATH;
        }
        attribute.valueContext = valueContext;
        this.attributes.add(attribute);
    }

    private boolean canOmitQuotesForValue(CharSequence value) {
        int len = value.length();
        for(int i = 0; i < len; ++i) {
            char c = value.charAt(i);
            if(!(
                ((c >= 'a') && (c <= 'z')) ||
                ((c >= 'A') && (c <= 'Z')) ||
                ((c >= '0' && (c <= '9')))
            )) { return false; }
        }
        return true;
    }

    private static class Attribute {
        public String name;
        public String preparedNameEquals; // " name=\"" for rendering
        public Node value;
        public Context valueContext;
    }

    protected Node orSimplifiedNode() {
        if(this.attributes == null) {
            return new NodeLiteral(this.start+">");
        }
        return this;
    }

    public void render(StringBuilder builder, Driver driver, Object view, Context context) {
        builder.append(this.start);
        if(this.attributes != null) {
            for(Attribute attribute : this.attributes) {
                int attributeStart = builder.length();
                builder.append(attribute.preparedNameEquals);
                int valueStart = builder.length();
                attribute.value.render(builder, driver, view, attribute.valueContext);
                // If nothing was rendered, remove the attribute
                if(valueStart == builder.length()) {
                    builder.setLength(attributeStart);
                } else {
                    builder.append('"');
                }
            }
        }
        builder.append('>');
    }

    public void dumpToBuilder(StringBuilder builder, String linePrefix) {
        builder.append(linePrefix).append("TAG ").append(this.start);
        if(this.attributes == null) {
            builder.append(">\n");
        } else {
            builder.append("> with "+this.attributes.size()+" attributes:\n");
            for(Attribute attribute : this.attributes) {
                builder.append(linePrefix+"  ").append(attribute.name).append("\n");
                attribute.value.dumpToBuilder(builder, linePrefix+"  = ");
            }
        }
    }
}
