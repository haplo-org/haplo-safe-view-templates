
/* Haplo Safe View Templates                         https://haplo.org
 * (c) Haplo Services Ltd 2015 - 2019            https://www.haplo.com
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.         */

package org.haplo.template.html;

final class NodeFunctionTranslatedString extends NodeFunction.ExactlyOneArgument {
    private String category;

    static private final String DEFAULT_CATEGORY = "template";

    NodeFunctionTranslatedString(String category) {
        this.category = (category == null) ? DEFAULT_CATEGORY : category;
    }

    public String getFunctionName() {
        return DEFAULT_CATEGORY.equals(this.category) ? "i" : "i:"+this.category;
    }

    public void postParse(Parser parser, int functionStartPos) throws ParseException {
        super.postParse(parser, functionStartPos);
        if(!(getSingleArgument() instanceof NodeLiteral)) {
            parser.error(this.getFunctionName()+"() must have a literal string as the argument", functionStartPos);
        }
    }

    public void render(StringBuilder builder, Driver driver, Object view, Context context) throws RenderException {
        NodeLiteral argument = (NodeLiteral)getSingleArgument();
        String string = argument.getLiteralString();
        // TODO: Translate string
        // TODO: Where there are block, interpolate
        // TODO: Plural handling
        builder.append(string);
    }

    public String getDumpName() {
        return "TRANSLATED-STRING";
    }
}
