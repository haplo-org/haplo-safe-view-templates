# coding: UTF-8

require 'json'

template_inclusions = java.util.HashMap.new
template_inclusions.put("template1", Java::Template::Parser.new(<<__E).parse())
  <b> "Included Template 1: " value1 </b>
__E
template_inclusions.put("template2", Java::Template::Parser.new(<<__E).parse())
  within(nested) {
    <i> "Included Template 2: " ^{rootValue} </i>
  }
__E
template_inclusions.put("components", Java::Template::Parser.new(<<__E).parse())
  within(component) {
    <div class="component">
      value1 " "
      <span class="anon"> yield() </span>
      <span class="e1"> yield:extra1() </span>
      <span class="e2"> yield:extra2() </span>
      <span class="three"> yield:extra3() </span>
    </div>
  }
__E

java_import Java::Template::Context
def try_quoting
  [
    ['<ping>"','<ping>"',Context::UNSAFE],
    ['','',Context::TEXT],
    ['x','x',Context::TEXT],
    ['&','&amp;',Context::TEXT],
    ['<ping>','&lt;ping&gt;',Context::TEXT],
    ['a<ping>b','a&lt;ping&gt;b',Context::TEXT],
    ['"hello"','"hello"',Context::TEXT],
    ['"hello"','&quot;hello&quot;',Context::ATTRIBUTE_VALUE]
  ].each do |input, escaped, attribute_context|
    output = Java::Template::Escape.escapeString(input, attribute_context)
    if output != escaped
      puts "Escaping fail"
      p [input, output]
    end
  end
end

def view_value_to_java(value)
  case value
  when Array
    value.map {|v| view_value_to_java(v)} .to_java
  when Hash
    m = java.util.LinkedHashMap.new
    value.each do |k,v|
      m.put(k.to_s.to_java, view_value_to_java(v))
    end
    m
  else
    value.to_java
  end
end

# ---------------------------------------------------------------------------
if ARGV[0] == 'run' || ARGV[0] == 'tree'
  template_source = File.open(ARGV[1], "r:UTF-8") { |f| f.read }
  # See if it looks like a test file, and if so, pick out the template
  template_source_split = template_source.split("\n---\n")
  template_source = template_source_split[1] if template_source_split.length >= 3
  template = Java::Template::Parser.new(template_source).parse()
  if ARGV[0] == 'tree'
    STDOUT.write template.dump()
  else
    view = JSON.parse(File.read(ARGV[2]))
    driver = Java::TemplateDriverNestedjava::NestedJavaDriver.new(view_value_to_java(view), nil)
    puts template.renderString(driver)
  end
  exit(0)
end
# ---------------------------------------------------------------------------

class TestFunctionRenderer
  ArgumentRequirement = Java::Template::FunctionBinding::ArgumentRequirement
  def renderFunction(builder, binding)
    case binding.getFunctionName()
    when "generic-function"
      builder.append("TEST GENERIC FUNCTION RENDER")
      true
    when /\Atestargs-(.*)\z/
      builder.append('`')
      $1.split(//).each do |i|
        str = nil
        case i
        when 's'; str = binding.nextUnescapedStringArgument(ArgumentRequirement::OPTIONAL)
        when 'S'; str = binding.nextUnescapedStringArgument(ArgumentRequirement::REQUIRED)
        when 'v'; str = obj_with_class(binding.nextViewObjectArgument(ArgumentRequirement::OPTIONAL))
        when 'V'; str = obj_with_class(binding.nextViewObjectArgument(ArgumentRequirement::REQUIRED))
        when 'r'; binding.restartArguments()
        when 'n'; binding.skipArgument(ArgumentRequirement::OPTIONAL)
        when 'N'; binding.skipArgument(ArgumentRequirement::REQUIRED)
        when 'L'; binding.noMoreArgumentsExpected()
        else raise "Unknown instruction in test"
        end
        builder.append(str.nil? ? "NULL" : str)
        builder.append('`')
      end
      true
    else
      false
    end
  end
  CLASS_NAME_MAP = {
    'Java::JavaUtil::LinkedHashMap' => 'Hash'
  }
  def obj_with_class(obj)
    obj.to_s+"/"+(CLASS_NAME_MAP[obj.class.name] || obj.class.name)
  end
end

# ---------------------------------------------------------------------------

try_quoting

tests = 0
passed = 0
failed = 0
files = Dir.glob("test-case/**/*.*").sort
files.each do |filename|
  comment, *commands = File.open(filename, "r:UTF-8") { |f| f.read }.split("\n---\n")
  raise "Bad test #{filename} - no tests" if commands.empty?
  required_cmd = Proc.new do
    raise "Expected element in test #{filename}" if commands.empty?
    commands.shift
  end
  if comment =~ /\A\s*PARSE ERROR:/
    # Test one or more parse errors
    while ! commands.empty?
      template_source = required_cmd.call
      expected_error = required_cmd.call.strip
      tests += 1
      exception = nil
      begin
        template = Java::Template::Parser.new(template_source.strip).parse()
      rescue => e
        exception = e
      end
      message = exception ? exception.message : '(exception not thrown)'
      if message == expected_error
        passed += 1
      else
        failed += 1
        puts
        puts "#{filename}: Unexpected exception:"
        puts "EXPECTED: #{expected_error}"
        puts "OUTPUT:   #{message}"
        puts comment.strip
      end
    end
  else
    # Test templates
    template = nil
    while ! commands.empty?
      if template == nil
        begin
          template = Java::Template::Parser.new(required_cmd.call.strip).parse()
        rescue => e
          failed += 1
          puts "\n#{filename}: Unexpected parse error"
          (e.respond_to? :printStackTrace) ? e.printStackTrace() : p(e)
          next
        end
      end
      view_json = required_cmd.call.strip
      if view_json =~ /NEW TEMPLATE(:\s*(.+))?/
        comment = $2 if $2
        template = nil
        next
      end
      expected_output = required_cmd.call.strip
      view = JSON.parse(view_json)
      drivers = []
      drivers << Java::TemplateDriverNestedjava::NestedJavaDriver.new(view_value_to_java(view), template_inclusions)
      drivers << Java::TemplateDriverJrubyjson::JRubyJSONDriver.new(view, template_inclusions)
      if expected_output =~ /\ATREE:(.+)\z/m
        # Testing the tree, not the rendered output
        expected_output = $1.strip
        drivers = [:tree]
      end
      drivers.each do |driver|
        tests += 1
        output = nil
        begin
          if driver == :tree
            output = template.dump().strip
          else
            driver.setFunctionRenderer(TestFunctionRenderer.new)
            output = template.renderString(driver)
          end
        rescue Java::Template::RenderException => render_exception
          output = "RENDER ERROR: #{render_exception.message}"
        end
        if output == expected_output
          passed += 1
        else
          failed += 1
          puts
          puts "#{filename}: Bad template output"
          puts comment.strip
          puts "EXPECTED: #{expected_output}"
          puts "OUTPUT:   #{output}"
          puts "DRIVER:   #{driver.class.name.split('::').last}"
        end
      end
    end
  end
end

puts
puts (failed == 0) ? "PASSED" : "FAILED"
puts "#{tests} tests, #{passed} passed, #{failed} failed, in #{files.length} files"
