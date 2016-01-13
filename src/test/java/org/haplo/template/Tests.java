/* Haplo Safe View Templates                          http://haplo.org
 * (c) Samir Talwar 2016                   https://www.samirtalwar.com
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.         */

package org.haplo.template;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.runner.RunWith;

@RunWith(RSpecTestRunner.class)
@Tests.Files({
    "src/test/ruby/test.rb"
})
public final class Tests {
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Files {
        String[] value();
    }
}
