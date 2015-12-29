package org.haplo.template.html;

public class ParserConfiguration {
    public boolean functionArgumentsAreURL(String functionName) {
        return false;
    }
    public void validateFunction(Parser parser, NodeFunction function) throws ParseException {
    }
}
