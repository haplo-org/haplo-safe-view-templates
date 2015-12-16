package template;

// NodeList is final because other implementations might not override
// whitelistForLiteralStringOnly() and create a security bug.
final class NodeList extends NodeListBase {
    protected Node orSimplifiedNode() {
        return (hasOneMember()) ? getListHeadMaybe() : this;
    }

    protected Object value(Driver driver, Object view) {
        if(hasOneMember()) {
            return getListHeadMaybe().value(driver, view);
        } else {
            return null;
        }
    }

    protected Iterable<Object> valueIterableViewList(Driver driver, Object view) {
        if(hasOneMember()) {
            return getListHeadMaybe().valueIterableViewList(driver, view);
        } else {
            return null;
        }
    }

    public void render(StringBuilder builder, Driver driver, Object view, Context context) {
        if(context == Context.ATTRIBUTE_VALUE) {
            // Lists need to be space separated inside attributes
            int listStart = builder.length();
            Node node = getListHeadMaybe();
            while(node != null) {
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
                node = node.getNextNode();
            }
            
        } else {
            // For all other contexts, output values with nothing between them
            Node node = getListHeadMaybe();
            while(node != null) {
                node.render(builder, driver, view, context);
                node = node.getNextNode();
            }
        }
    }

    protected boolean whitelistForLiteralStringOnly() {
        Node node = getListHeadMaybe();
        while(node != null) {
            if(!node.whitelistForLiteralStringOnly()) {
                return false;
            }
            node = node.getNextNode();
        }
        return true;
    }

    protected String dumpName() {
        return "LIST";
    }
}
