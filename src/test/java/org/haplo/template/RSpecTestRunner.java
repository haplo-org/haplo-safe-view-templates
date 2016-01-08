package org.haplo.template;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig;
import org.jruby.javasupport.JavaEmbedUtils;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public final class RSpecTestRunner extends ParentRunner<RSpecTestRunner.RubyTestFile> {
    private static final Path GEM_HOME = Paths.get("gems").toAbsolutePath();
    private final RubyInstanceConfig rubyConfig;
    private final Ruby runtime;

    public RSpecTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);

        rubyConfig = new RubyInstanceConfig();
        Map<String, String> environment = new HashMap<>(System.getenv());
        environment.put("GEM_HOME", GEM_HOME.toString());
        environment.put("GEM_PATH", GEM_HOME.toString());
        rubyConfig.setEnvironment(environment);

        runtime = JavaEmbedUtils.initialize(singletonList("src/test/ruby"), rubyConfig);
        invokeFunction(null, "require", "gems");
        invokeFunction(null, "require", "test_runner");
    }

    @Override
    public List<RubyTestFile> getChildren() {
        return Arrays.stream(getTestClass().getAnnotation(Tests.Files.class).value())
            .map(RubyTestFile::new)
            .collect(toList());
    }

    @Override
    public Description describeChild(RubyTestFile child) {
        return Description.EMPTY;
    }

    @Override
    public void runChild(RubyTestFile child, RunNotifier notifier) {
        invokeFunction(null, "run", child.path, notifier);
    }

    private void invokeFunction(Object object, String method, Object... args) {
        JavaEmbedUtils.invokeMethod(runtime, object, method, args, Object.class);
    }

    public final class RubyTestFile {
        public final String path;

        public RubyTestFile(String path) {
            this.path = path;
        }
    }
}
