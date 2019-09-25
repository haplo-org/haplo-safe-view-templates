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

    protected Object valueForFunctionArgument(Driver driver, Object view) throws RenderException {
        StringBuilder builder = new StringBuilder(224);
        this.render(builder, driver, view, Context.UNSAFE);
        return builder.toString();
    }

    public void render(StringBuilder builder, Driver driver, Object view, Context context) throws RenderException {
        NodeLiteral argument = (NodeLiteral)getSingleArgument();
        String text = argument.getLiteralString();
        String translatedText = driver.translateText(this.category, text);
        if(this.hasAnyBlocks()) {
            // When there are blocks on this function, interpolate the string.
            this.renderInterpolated(translatedText, builder, driver, view, context);
        } else {
            // If there aren't any blocks, just output the translated text as is.
            builder.append(translatedText);
        }
    }

    public String getDumpName() {
        return "TRANSLATED-STRING";
    }

    protected String getOriginalString() {
        NodeLiteral argument = (NodeLiteral)getSingleArgument();
        return argument.getLiteralString();
    }

    // ----------------------------------------------------------------------

    private void renderInterpolated(String text, StringBuilder builder, Driver driver, Object view, Context context) throws RenderException {
        int index = 0,
            length = text.length();
        while(index < length) {
            int startInterpolation = text.indexOf('{', index);
            if(startInterpolation == -1) {
                if(index < length) {
                    builder.append(text, index, length);
                }
                return;
            }
            builder.append(text, index, startInterpolation);
            int endInterpolation = text.indexOf('}', startInterpolation);
            if(endInterpolation == -1) {
                throw new RenderException(driver, "Missing end } from interpolation");
            }
            String blockName = text.substring(startInterpolation+1, endInterpolation);
            // TODO: Plural handling by special annonations in this 'block name'
            Node block = this.getBlock((blockName.length() == 0) ? Node.BLOCK_ANONYMOUS : blockName);
            if(block == null) {
                throw new RenderException(driver, 
                    (blockName.length() == 0) ?
                    "When interpolating, i() does not have an anonymous block" :
                    "When interpolating, i() does not have block named "+blockName
                );
            }
            block.render(builder, driver, view, context);
            index = endInterpolation + 1;
        }
    }

}
