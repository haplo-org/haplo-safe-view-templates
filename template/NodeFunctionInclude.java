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
        // Clone the driver with a new root, so all state is reset for included template
        Driver nestedDriver = driver.driverWithNewRoot(view);
        // Delegate the rendering of the included template to the driver, so it can
        // implement driver specific templates, templates written in other languages, etc.
        nestedDriver.renderInclusion(getInclusionName(), builder, context);
    }

    public String getDumpName() {
        return "INCLUDE";
    }

    public void dumpToBuilder(StringBuilder builder, String linePrefix) {
        builder.append(linePrefix).append("INCLUDE \"").
                append(getInclusionName()).append("\"\n");
    }
}
