package template;

public class Escape {
    static public void escape(CharSequence input, StringBuilder builder, Context context) {
        if(context == Context.UNSAFE) {
            builder.append(input);
            return;
        }
        int pos = 0, start = 0, len = input.length();
        for(; pos < len; pos++) {
            char c = input.charAt(pos);
            String entity = null;
            switch(c) {
                case '&': entity = "&amp;"; break;
                case '<': entity = "&lt;"; break;
                case '>': entity = "&gt;"; break;
                case '"': if(context == Context.ATTRIBUTE_VALUE) { entity = "&quot;"; }; break;
            }
            if(entity != null) {
                builder.append(input, start, pos).
                        append(entity);
                start = pos + 1;
            }
        }
        if(start < pos) {
            builder.append(input, start, pos);
        }
    }

    static public String escapeString(CharSequence input, Context context) {
        StringBuilder builder = new StringBuilder(input.length() + 64);
        escape(input, builder, context);
        return builder.toString();
    }
}
