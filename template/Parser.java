package template;

public class Parser {
    private CharSequence source;
    private int pos;
    private Context context;

    // TODO: Is this overly restrictive regex valid? Is it worth writing a scanner function for it?
    final static java.util.regex.Pattern VALID_ATTRIBUTE_NAME_REGEX = java.util.regex.Pattern.compile("\\A[a-z0-9_A-Z-]+\\Z");

    public Parser(CharSequence source) {
        this.source = source;
        this.pos = 0;
        this.context = Context.TEXT;
    }

    public Template parse() throws ParseException {
        return new Template(parseList(-1, "template"));
    }

    protected Context getCurrentParseContext() {
        return this.context;
    }

    protected NodeList parseList(int endOfListCharacter, String what) throws ParseException {
        int listStart = this.pos;
        NodeList nodes = new NodeList();
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
        return nodes;
    }

    protected Node parseOneValue(int endOfListCharacter) throws ParseException {
        CharSequence s = symbol();
        if(s == null) { return null; }
        int singleChar = (s.length() == 1) ? s.charAt(0) : -999;
        if(singleChar == '"') {
            return new NodeLiteral(quotedString());
        } else if(singleChar == '<') {
            return parseElement().orLiteral();
        } else if(singleChar == '[') {
            return parseList(']', "list").orSingleNode();
        } else if(singleChar == endOfListCharacter) {
            return END_OF_LIST;
        } else if((singleChar == ']') || (singleChar == '}') ||
                  (singleChar == ')') || (singleChar == '>')) {
            error("Unexpected "+s);
        }
        // Special case for '.'
        if(s.charAt(0) == '.') {
            if(s.length() == 1) {
                return new NodeValueThis();
            } else {
                error("Value names cannot be prefixed with . (single dot is used for 'this')",
                    this.pos - 1);  // reasonable position for error
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

    // Called after the first ( has been returned from symbol()
    protected Node parseFunction(String functionName) throws ParseException {
        int functionStartPos = this.pos;
        NodeList arguments = parseList(')', "arguments");
        NodeFunction fn = null;
        switch(functionName) {
            case "include":     fn = new NodeFunctionInclude(); break;
            case "with":        fn = new NodeFunctionWith(); break;
            case "if":          fn = new NodeFunctionConditional(false); break;
            case "unless":      fn = new NodeFunctionConditional(true); break;
            case "each":        fn = new NodeFunctionEach(); break;
            case "case":        fn = new NodeFunctionCase(); break;
            case "unsafeHTML":  fn = new NodeFunctionUnsafeHTML(); break;
            default:            fn = new NodeFunctionGeneric(functionName); break;
        }
        fn.setArguments(this, arguments);
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
                Node block = parseList('}', listBlockName).orSingleNode();
                fn.addBlock(this, blockName, block, blockNameEndPos);
            } else {
                this.pos = savedPos;
                break;
            }
            possibleBlockName = null;
        }
        fn.postParse(this, functionStartPos);
        return fn;
    }

    protected NodeElement parseElement() throws ParseException {
        if(this.context != Context.TEXT) {
            error("Elements are not valid in this context");
        }
        try {
            this.context = Context.ELEMENT;
            CharSequence name = symbol();
            if(name == null) { error("Unexpected end of template after <"); }
            NodeElement element = new NodeElement(name.toString());
            // TODO: Validate element name looks like a dom element name?
            // TODO: Check close elements don't have attributes?
            CharSequence attributeName = null;
            while(true) {
                CharSequence s = symbol();
                if(s == null) { error("Unexpected end of template in element"); }
                if(symbolIsSingleChar(s, '>')) {
                    break;
                } else if(symbolIsSingleChar(s, '=')) {
                    if(attributeName == null) {
                        error("Unexpected = in element");
                    }
                    try {
                        this.context = Context.ATTRIBUTE_VALUE;
                        element.addAttribute(attributeName.toString(), parseOneValue(-1));
                    } finally {
                        this.context = Context.ELEMENT;
                    }
                    attributeName = null;
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
                error("No attribute value in element");
            }
            return element;
        } finally {
            this.context = Context.TEXT;
        }
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
                (c == '"') ||
                (c == '=');
    }

    protected boolean isReservedCharacter(int c) {
        // NOTE: Don't remove ' from the reserved characters as the escaper assumes it can't be used
        return (c == ',') || (c == '\'') || (c == ';');
    }

    protected void error(String error) throws ParseException {
        // Default error position is the last character consumed
        error(error, this.pos);
    }

    protected void error(String error, int errorPosition) throws ParseException {
        // TODO: Report errors nicely
        int p = errorPosition - 1;  // back one so the relevant character is found
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
        // Single character symbol? (never EOF at this point)
        if(isSingleCharSymbol(c = read())) {
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
            } else if(c == '/') {
                // Skip a comment? (// until end of line)
                int symbolEnd = this.pos - 1;
                if(read() != '/') {
                    this.pos = symbolEnd + 1;   // not actually a comment
                } else {
                    while(((c = read()) != -1) && (c != '\n')) { /* empty */ }
                    if(symbolEnd > startPos) {
                        return this.source.subSequence(startPos, symbolEnd);
                    } else {
                        return symbol();
                    }
                }
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
