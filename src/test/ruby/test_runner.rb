java_import org.jruby.exceptions.RaiseException
java_import org.junit.runner.Description
java_import org.junit.runner.notification.Failure
require 'rspec'

class JUnitFormatter
  RSpec::Core::Formatters.register JUnitFormatter,
    :example_group_started, :example_group_finished,
    :example_started, :example_passed, :example_failed, :example_pending

  def initialize(output_stream)
    @group = []
  end

  def self.notifier=(notifier)
    @@notifier = notifier
  end

  def example_group_started(notification)
    @group << notification.group.description
  end

  def example_group_finished(notification)
    @group.pop
  end

  def example_started(notification)
    @@notifier.fireTestStarted(description_of(notification))
  end

  def example_passed(notification)
    @@notifier.fireTestFinished(description_of(notification))
  end

  def example_failed(notification)
    exception = RaiseException.new(notification.exception.to_java)
    @@notifier.fireTestFailure(Failure.new(description_of(notification), exception))
  end

  def example_pending(notification)
    @@notifier.fireTestIgnored(description_of(notification))
  end

  private

  def description_of(notification)
    Description.createTestDescription(@group.join('/'), notification.example.description, [].to_java)
  end
end

def run(test_file, notifier)
  config = RSpec.configuration
  config.start_time = Time.now
  config.backtrace_exclusion_patterns << /sun\.reflect/
  config.backtrace_exclusion_patterns << /org\.jruby/
  config.formatter = JUnitFormatter
  JUnitFormatter.notifier = notifier

  RSpec::Core::Runner.run([test_file])
end
