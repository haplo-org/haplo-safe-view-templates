package template;

class NodeFunctionUnsafeHTML extends NodeFunction.ExactlyOneValueArgument {

    static private final String[] PERMITTED_BLOCK_NAMES = {};

    NodeFunctionUnsafeHTML() {
    }

    public String getFunctionName() {
        return "unsafeHTML";
    }

    protected String[] getPermittedBlockNames() {
        return PERMITTED_BLOCK_NAMES;
    }

    // Only allowed in TEXT context, as it would be too dangerous to allow it anywhere else
    public void postParse(Parser parser, int functionStartPos) throws ParseException {
        super.postParse(parser, functionStartPos);
        if(parser.getCurrentParseContext() != Context.TEXT) {
            parser.error("unsafeHTML() cannot be used in this context", functionStartPos);
        }
    }

    public void render(StringBuilder builder, Driver driver, Object view, Context context) {
        // Render argument without any escaping
        getSingleArgument().render(builder, driver, view, Context.UNSAFE);
    }

    public String getDumpName() {
        return "UNSAFEHTML";
    }
}
