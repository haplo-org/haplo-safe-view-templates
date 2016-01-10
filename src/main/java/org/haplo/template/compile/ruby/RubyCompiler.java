/* Haplo Safe View Templates                          http://haplo.org
 * (c) Haplo Services Ltd 2015 - 2016    http://www.haplo-services.com
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.         */

package org.haplo.template.compile.ruby;

import org.haplo.template.html.Compiler;
import org.haplo.template.html.Template;

public class RubyCompiler implements Compiler {
    StringBuilder builder;

    public RubyCompiler() {
        this.builder = new StringBuilder();
    }

    // ----------------------------------------------------------------------

    public void start(Template template) {
        this.builder.append("_hsvt_out = '' # ").append(template.getName()).append('\n');
    }

    public void literal(String literal) {
        this.builder.append("_hsvt_out << ");
        appendRubyString(literal);
        this.builder.append('\n');
    }

    public void end() {
        this.builder.append("_hsvt_out\n");
    }

    // ----------------------------------------------------------------------

    private void appendRubyString(CharSequence string) {
        this.builder.append('"');
        int pos = 0, start = 0, len = string.length();
        for(; pos < len; pos++) {
            char c = string.charAt(pos);
            char esc = 0;
            switch(c) {
                case '\\': esc = '\\'; break;
                case '\n': esc = 'n'; break;
                case '"': esc = '"'; break;
            }
            if(esc != 0) {
                this.builder.append(string, start, pos).
                        append('\\').append(esc);
                start = pos + 1;
            }
        }
        if(start < pos) {
            this.builder.append(string, start, pos);
        }
        this.builder.append('"');
    }

    // ----------------------------------------------------------------------

    public String output() {
        return this.builder.toString();
    }
}
