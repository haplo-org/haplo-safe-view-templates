package template;

import java.util.List;
import java.util.ArrayList;

// NodeList is final because other implementations might not override
// whitelistForLiteralStringOnly() and create a security bug.
final class NodeList extends NodeListBase {
    protected Node orSimplifiedNode() {
        return (this.nodes.size() == 1) ? this.nodes.get(0) : this;
    }

    protected Object value(Driver driver, Object view) {
        if(this.nodes.size() == 1) {
            return this.nodes.get(0).value(driver, view);
        } else {
            return null;
        }
    }

    protected Iterable<Object> valueIterableViewList(Driver driver, Object view) {
        if(this.nodes.size() == 1) {
            return this.nodes.get(0).valueIterableViewList(driver, view);
        } else {
            return null;
        }
    }

    public void render(StringBuilder builder, Driver driver, Object view, Context context) {
        if(context == Context.ATTRIBUTE_VALUE) {
            // Lists need to be space separated inside attributes
            int listStart = builder.length();
            for(Node node : this.nodes) {
                // If something has been added already, add a space
                if(listStart != builder.length()) {
                    builder.append(' ');
                }
                // Record the start of the value, then render it
                int valueStart = builder.length();
                node.render(builder, driver, view, context);
                // If nothing was output, and it's not the first attribute, then remove the space
                if(valueStart == builder.length()) {
                    if(listStart != valueStart) {
                        builder.setLength(valueStart - 1);
                    }
                }
            }
            
        } else {
            // For all other contexts, output values with nothing between them
            for(Node node : this.nodes) {
                node.render(builder, driver, view, context);
            }
        }
    }

    protected boolean whitelistForLiteralStringOnly() {
        for(Node node : this.nodes) {
            if(!node.whitelistForLiteralStringOnly()) {
                return false;
            }
        }
        return true;
    }

    protected String dumpName() {
        return "LIST";
    }
}
