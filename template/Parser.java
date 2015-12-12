package template;

import java.util.Stack;

public class Parser {
    private CharSequence source;
    private int pos = 0;
    private Context context = Context.TEXT;
    private Stack<Node> nesting;
    private boolean inEnclosingViewBlock = false;
    private int nextRememberIndex = 0;

    final static java.util.regex.Pattern VALID_TAG_NAME_REGEX = java.util.regex.Pattern.compile("\\A[a-z0-9]+\\Z");
    // TODO: Is this overly restrictive regex valid? Is it worth writing a scanner function for it?
    final static java.util.regex.Pattern VALID_ATTRIBUTE_NAME_REGEX = java.util.regex.Pattern.compile("\\A[a-z0-9_A-Z-]+\\Z");

    public Parser(CharSequence source) {
        this.source = source;
        this.nesting = new Stack<Node>();
    }

    public Template parse() throws ParseException {
        Template template = new Template(parseList(-1, "template"), this.nextRememberIndex);
        if(!this.nesting.empty()) {
            error("Bad nesting in template.");
        }
        return template;
    }

    protected Context getCurrentParseContext() {
        return this.context;
    }

    protected NodeList parseList(int endOfListCharacter, String what) throws ParseException {
        int listStart = this.pos;
        NodeList nodes = new NodeList();
        this.nesting.push(nodes);
        boolean seenEndOfList = false;
        Node node;
        while((node = parseOneValue(endOfListCharacter)) != null) {
            if(node == END_OF_LIST) {
                seenEndOfList = true;
                break;
            }
            nodes.add(node);
        }
        if((endOfListCharacter != -1) && !seenEndOfList) {
            error("Did not find end of "+what, listStart);
        }
        popNestingAndCheckNodeWas(nodes, listStart);
        return nodes;
    }

    protected Node parseOneValue(int endOfListCharacter) throws ParseException {
        CharSequence s = symbol();
        if(s == null) { return null; }
        char firstChar = s.charAt(0);
        int singleChar = (s.length() == 1) ? firstChar : -999;
        if(singleChar == '"') {
            return new NodeLiteral(quotedString());
        } else if(singleChar == '<') {
            return parseTag().orSimplifiedNode();
        } else if(singleChar == '[') {
            return parseList(']', "list").orSimplifiedNode();
        } else if(singleChar == endOfListCharacter) {
            return END_OF_LIST;
        } else if((singleChar == ']') || (singleChar == '}') ||
                  (singleChar == '(') || (singleChar == ')') || // both () directions
                  (singleChar == '>')) {
            error("Unexpected "+s);
        }
        // Special case enclosing view block
        if(firstChar == '^') {
            return parseEnclosingViewBlock(s);
        }
        // Special case for '.'
        if(firstChar == '.') {
            if(s.length() == 1) {
                return new NodeValueThis();
            } else {
                error("Value names cannot be prefixed with . (single dot is used for 'this')");
            }
        }
        // Is it a value, or a function? Get next symbol to check.
        int savedPos = this.pos;
        CharSequence nextSymbol = symbol();
        if(symbolIsSingleChar(nextSymbol, '(')) {
            return parseFunction(s.toString());
        } else {
            // Simple value, restore position then return
            this.pos = savedPos;
            return new NodeValue(s.toString());
        }
    }

    private static final Node END_OF_LIST = new Node();

    protected Node parseEnclosingViewBlock(CharSequence name) throws ParseException {
        if(this.inEnclosingViewBlock) {
            error("Enclosing view blocks may not contain other enclosing view blocks");
        }
        this.inEnclosingViewBlock = true;
        for(int n = 0; n < name.length(); ++n) {
            if(name.charAt(n) != '^') {
                error("Enclosing view block names may not contain other characters");
            }
        }
        if(!symbolIsSingleChar(symbol(), '{')) {
            error("Enclosing view block names must be followed by a block");
        }
        NodeFunction.ChangesView nodeWhichChangesView = null;
        int depth = name.length();
        for(int n = this.nesting.size() - 1; n >= 0 && depth > 0; --n) {
            Node s = this.nesting.get(n);
            if(s instanceof NodeFunction.ChangesView) {
                depth--;
                if(depth == 0) {
                    nodeWhichChangesView = (NodeFunction.ChangesView)s;
                    break;
                }
            }
        }
        if(nodeWhichChangesView == null) {
            error("There are not "+name.length()+" enclosing views");
        }
        // Ask the function node to remember the value when the view is remembered
        nodeWhichChangesView.shouldRemember(this);
        Node block = parseList('}', "enclosing view block").orSimplifiedNode();
        this.inEnclosingViewBlock = false;
        return new NodeEnclosingView(nodeWhichChangesView.getRememberedViewIndex(), block);
    }

    // Called after the first ( has been returned from symbol()
    protected Node parseFunction(String functionName) throws ParseException {
        int functionStartPos = this.pos;
        NodeList arguments = parseList(')', "arguments");
        NodeFunction fn = null;
        switch(functionName) {
            case "include":     fn = new NodeFunctionInclude(); break;
            case "within":      fn = new NodeFunctionWithin(); break;
            case "if":          fn = new NodeFunctionConditional(false); break;
            case "unless":      fn = new NodeFunctionConditional(true); break;
            case "each":        fn = new NodeFunctionEach(); break;
            case "case":        fn = new NodeFunctionCase(); break;
            case "unsafeHTML":  fn = new NodeFunctionUnsafeHTML(); break;
            default:            fn = new NodeFunctionGeneric(functionName); break;
        }
        fn.setArguments(this, arguments);
        this.nesting.push(fn);
        // Are there any blocks?
        CharSequence possibleBlockName = Node.BLOCK_ANONYMOUS;
        while(true) {
            int savedPos = this.pos, blockNameEndPos = this.pos;
            if(possibleBlockName == null) {
                // The first block doesn't have a name, but further blocks do.
                possibleBlockName = symbol();
                blockNameEndPos = this.pos;
                // The name may be a quoted string.
                if(symbolIsSingleChar(possibleBlockName, '"')) {
                    possibleBlockName = quotedString();
                }
            }
            CharSequence possibleStartBlock = symbol();
            if(symbolIsSingleChar(possibleStartBlock, '{')) {
                String blockName = possibleBlockName.toString();
                // Work out a readable block name for the error message
                String listBlockName = (possibleBlockName == Node.BLOCK_ANONYMOUS) ?
                    "block" : blockName+" block";
                Node block = parseList('}', listBlockName).orSimplifiedNode();
                fn.addBlock(this, blockName, block, blockNameEndPos);
            } else {
                this.pos = savedPos;
                break;
            }
            possibleBlockName = null;
        }
        fn.postParse(this, functionStartPos);
        popNestingAndCheckNodeWas(fn, functionStartPos);
        return fn;
    }

    protected Node parseTag() throws ParseException {
        if(this.context != Context.TEXT) {
            error("Tags are not valid in this context");
        }
        int tagStartPos = this.pos;
        CharSequence name = symbol();
        if(symbolIsSingleChar(name, '/')) {
            return parseCloseTag(tagStartPos);
        }
        this.context = Context.TAG;
        checkTagName(name, false, tagStartPos);
        String tagName = name.toString();
        NodeTag tag = new NodeTag(tagName);
        CharSequence attributeName = null;
        while(true) {
            CharSequence s = symbol();
            if(s == null) { error("Unexpected end of template in tag"); }
            if(symbolIsSingleChar(s, '>')) {
                break;
            } else if(symbolIsSingleChar(s, '=')) {
                if(attributeName == null) {
                    error("Unexpected = in tag");
                }
                this.context = Context.ATTRIBUTE_VALUE;
                tag.addAttribute(attributeName.toString(), parseOneValue(-1));
                this.context = Context.TAG;
                attributeName = null;
            } else if(symbolIsSingleChar(s, '/')) {
                error("Self closing tags are not allowed", tagStartPos);
            } else {
                if(attributeName != null) {
                    error("Expected = after attribute name");
                }
                if(!(VALID_ATTRIBUTE_NAME_REGEX.matcher(s).matches())) {
                    error("Invalid attribute name: "+s);
                }
                attributeName = s;
            }
        }
        if(attributeName != null) {
            error("No attribute value in tag");
        }
        if(!HTML.isVoidTag(tagName)) {
            // Void tags are not closed, so aren't part of the nested structure
            this.nesting.push(tag);
        }
        this.context = Context.TEXT;
        return tag;
    }

    protected Node parseCloseTag(int tagStartPos) throws ParseException {
        CharSequence name = symbol();
        checkTagName(name, true, tagStartPos);
        CharSequence terminator = symbol();
        if(symbolIsSingleChar(terminator, '/')) {
            error("Self closing tags are not allowed", tagStartPos);
        } else if(!symbolIsSingleChar(terminator, '>')) {
            error("A closing tag may not have attributes");
        }
        String tagName = name.toString();
        if(HTML.isVoidTag(tagName)) {
            error("Void tags may not have close tags", tagStartPos);
        }
        Node openingTag = this.nesting.pop();
        if(!((openingTag instanceof NodeTag) && ((NodeTag)openingTag).getName().equals(tagName))) {
            error("Unexpected tag </"+name+">, tags must be balanced", tagStartPos);
        }
        return new NodeLiteral("</"+name+">");
    }

    protected void checkTagName(CharSequence name, boolean isCloseTag, int tagStartPos) throws ParseException {
        if(name == null) { error("Unexpected end of template after <"); }
        // TODO: Validate tag name a bit better?
        if(!(VALID_TAG_NAME_REGEX.matcher(name).matches())) {
            error("Invalid tag name <"+(isCloseTag?"/":"")+name+"> (must be lower case, a-z0-9 only)", tagStartPos);
        }
    }

    // ----------------------------------------------------------------------

    // Views need to be rememebered during rendering so mechanisms like the
    // enclosing view can recall them. To make it as close to zero cost as
    // possible when the feature isn't used, only store views that are known
    // to be needed later when rendering. The parser keeps track of indicies
    // and tells the Driver, via the Template, how many are needed.
    protected int allocateRememberIndex() {
        return this.nextRememberIndex++;
    }

    // ----------------------------------------------------------------------

    protected int read() {
        if(pos >= this.source.length()) {
            return -1;
        }
        return source.charAt(this.pos++);
    }

    protected void move(int by) {
        this.pos += by;
        if(this.pos < 0 || this.pos >= this.source.length()) {
            throw new RuntimeException("move() out of bounds "+by+" to "+this.pos);
        }
    }

    protected boolean isWhitespace(int c) {
        return (c == ' ') || (c == '\n') || (c == '\r');
    }

    protected boolean isSingleCharSymbol(int c) {
        return (c == '(') || (c == ')') ||
                (c == '{') || (c == '}') ||
                (c == '<') || (c == '>') ||
                (c == '[') || (c == ']') ||
                (c == '/') ||
                (c == '"') ||
                (c == '=');
    }

    protected boolean isReservedCharacter(int c) {
        // NOTE: Don't remove ' from the reserved characters as the escaper assumes it can't be used
        return (c == ',') || (c == '\'') || (c == ';');
    }

    protected void popNestingAndCheckNodeWas(Node node, int errorPosition) throws ParseException {
        if(this.nesting.empty() || (this.nesting.pop() != node)) {
            error("Improperly nested block, check tags are balanced", errorPosition);
        }
    }

    protected void error(String error) throws ParseException {
        // Default error position is the last character consumed
        error(error, this.pos);
    }

    protected void error(String error, int errorPosition) throws ParseException {
        // TODO: Report errors nicely
        int p = errorPosition - 1;  // back one so the relevant character is found
        while((p > 0) && (this.source.charAt(p) == '\n')) {
            // If the position is on a newline, then move back another character,
            // errors at char 0 were actually at the end of the previous line
            --p;
        }
        int charPos = 0;
        for(; p >= 0; p--) {
            if(this.source.charAt(p) == '\n') {
                break;
            }
            charPos++;
        }
        int line = 1;
        for(; p >= 0; p--) {
            if(this.source.charAt(p) == '\n') {
                line++;
            }
        }
        throw new ParseException("Error at line "+line+" character "+charPos+": "+error);
    }

    protected CharSequence symbol() throws ParseException {
        // Skip whitespace
        int c = -1;
        do {
            if((c = read()) == -1) { return null; }
        } while(isWhitespace(c));
        move(-1);
        c = read();
        // Skip comments: // until end of line
        // Since isSingleCharSymbol() returns true for this character, it must be checked
        // separately, and / chars can only be found at this point in this function.
        if(c == '/') {
            int symbolEnd = this.pos - 1;
            if(read() != '/') {
                this.pos = symbolEnd + 1;   // not actually a comment
            } else {
                while(((c = read()) != -1) && (c != '\n')) { /* empty */ }
                return symbol();
            }
        }
        // Single character symbol? (never EOF at this point)
        if(isSingleCharSymbol(c)) {
            return this.source.subSequence(this.pos - 1, this.pos);
        }
        move(-1);
        // Multi-char symbol
        int startPos = this.pos;
        while(true) {
            c = read();
            if(c == -1) {
                return this.source.subSequence(startPos, this.pos);
            } else if(isWhitespace(c)) {
                return this.source.subSequence(startPos, this.pos - 1);
            } else if(isSingleCharSymbol(c)) {
                move(-1);
                return this.source.subSequence(startPos, this.pos);
            } else if(isReservedCharacter(c)) {
                error("Reserved character: \""+((char)c)+"\"");
            } else if(c == '\t') {
                error("Tab character in source, indent with 4 spaces");
            }
        }
    }

    protected String quotedString() throws ParseException {
        int c, startPos = this.pos;
        StringBuilder builder = new StringBuilder();
        while(true) {
            c = read();
            if(c == -1) {
                break;
            } else if(c == '\\') {
                int n = read();
                if(n == -1) {
                    break;
                }
                builder.append((n == 'n') ? '\n' : (char)n);
            } else if(c == '"') {
                return builder.toString();
            } else {
                builder.append((char)c);
            }
        }
        // Report error message from the beginning of the string so it's useful
        error((c == -1) ?
            "Unexpected end of template in quoted string" :
            "Unexpected end of template in quoted character", startPos);
        return null;
    }

    protected boolean symbolIsSingleChar(CharSequence s, char ch) {
        return (s != null) && (s.length() == 1) && (s.charAt(0) == ch);
    }
}
