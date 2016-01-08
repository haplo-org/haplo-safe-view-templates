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
