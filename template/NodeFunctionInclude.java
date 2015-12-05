package template;

class NodeFunctionInclude extends NodeFunction.ExactlyOneArgument {

    static private final String[] PERMITTED_BLOCK_NAMES = {};

    NodeFunctionInclude() {
    }

    public String getFunctionName() {
        return "include";
    }

    protected String[] getPermittedBlockNames() {
        return PERMITTED_BLOCK_NAMES;
    }

    public void postParse(Parser parser, int functionStartPos) throws ParseException {
        super.postParse(parser, functionStartPos);
        if(!(getSingleArgument() instanceof NodeLiteral)) {
            parser.error("include() must have a literal string as the argument", functionStartPos);
        }
    }

    public String getInclusionName() {
        return ((NodeLiteral)getSingleArgument()).getLiteralHTML();
    }

    public void render(StringBuilder builder, Driver driver, Object view, Context context) {
        String inclusionName = getInclusionName();
        // Move the view to the property named after the inclusion, if it exists in the view
        Object inclusionView = driver.getValueFromView(view, new String[] {inclusionName});
        if(inclusionView == null) { inclusionView = view; }
        // Delegate the rendering of the included template to the driver, so it can
        // implement driver specific templates, templates written in other languages, etc.
        driver.renderInclusion(inclusionName, builder, inclusionView, context);
    }

    public String getDumpName() {
        return "INCLUDE";
    }

    public void dumpToBuilder(StringBuilder builder, String linePrefix) {
        builder.append(linePrefix).append("INCLUDE \"").
                append(getInclusionName()).append("\"\n");
    }
}
