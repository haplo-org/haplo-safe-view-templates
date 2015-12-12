package template;

class HTML {
    // Tests to see if a tag name is a void tag
    // List from http://www.w3.org/TR/html-markup/syntax.html#syntax-elements
    public static boolean isVoidTag(String name) {
        switch(name) {
            case "area":
            case "base":
            case "br":
            case "col":
            case "command":
            case "embed":
            case "hr":
            case "img":
            case "input":
            case "keygen":
            case "link":
            case "meta":
            case "param":
            case "source":
            case "track":
            case "wbr":
                return true;
            default:
                return false;
        }
    }
}
