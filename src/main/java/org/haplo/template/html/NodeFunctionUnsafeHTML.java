/* Haplo Safe View Templates                          http://haplo.org
 * (c) Haplo Services Ltd 2015 - 2016    http://www.haplo-services.com
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.         */

package org.haplo.template.html;

final class NodeFunctionUnsafeHTML extends NodeFunctionUnsafeBase {
    NodeFunctionUnsafeHTML() {
    }

    public String getFunctionName() {
        return "unsafeHTML";
    }

    // Only allowed in TEXT context, as it would be too dangerous to allow it anywhere else
    protected Context allowedContext() {
        return Context.TEXT;
    }

    public void render(StringBuilder builder, Driver driver, Object view, Context context) throws RenderException {
        // Render argument without any escaping
        getSingleArgument().render(builder, driver, view, Context.UNSAFE);
    }

    public String getDumpName() {
        return "UNSAFEHTML";
    }
}
