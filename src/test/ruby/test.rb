# coding: UTF-8

require 'json'
require 'rspec'

Parser = Java::OrgHaploTemplateHtml::Parser
NestedJavaDriver = Java::OrgHaploTemplateDriverNestedjava::NestedJavaDriver
JRubyJSONDriver = Java::OrgHaploTemplateDriverJrubyjson::JRubyJSONDriver
RhinoJavaScriptDriver = Java::OrgHaploTemplateDriverRhinojs::RhinoJavaScriptDriver

# ---------------------------------------------------------------------------

template_inclusions = {
  'template1' => Parser.new(<<__E, "template1").parse(),
    <b> "Included Template 1: " value1 </b>
__E
  'template2' => Parser.new(<<__E, "template2").parse(),
    within(nested) {
      <i> "Included Template 2: " ^{rootValue} </i>
    }
__E
  'template3' => Parser.new(<<__E, "template2").parse(),
    <span> "T3 " template:template1() " - " generic-function() </span>
__E
  'template4' => Parser.new(<<__E, "template4").parse(),
    <span> template:unknown-template() </span>
__E
  'self-inclusion' => Parser.new(<<__E, "self-inclusion").parse(),
    <span> template:self-inclusion() </span>
__E
  'components' => Parser.new(<<__E, "components").parse()
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
}
included_template_renderer = Java::OrgHaploTemplateHtml::SimpleIncludedTemplateRenderer.new(template_inclusions)

$template_for_deferred = template_inclusions['template1']

# ---------------------------------------------------------------------------

java_import Java::OrgHaploTemplateHtml::Context

describe 'Escaping' do
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
    it "escapes \"#{input}\" as \"#{escaped}\" in the context #{attribute_context}" do
      output = Java::OrgHaploTemplateHtml::Escape.escapeString(input, attribute_context)
      expect(output).to eq escaped
    end
  end
end

# ---------------------------------------------------------------------------

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
  template = Parser.new(template_source, "tree").parse()
  if ARGV[0] == 'tree'
    STDOUT.write template.dump()
  else
    view = JSON.parse(File.read(ARGV[2]))
    driver = Java::OrgHaploTemplateHtmlDriverNestedjava::NestedJavaDriver.new(view_value_to_java(view), nil)
    puts template.renderString(driver)
  end
  exit(0)
end
# ---------------------------------------------------------------------------

class TestParserConfiguration < Java::OrgHaploTemplateHtml::ParserConfiguration
  def functionArgumentsAreURL(functionName)
    (functionName == "testFunctionWithURL")
  end
  def validateFunction(parser, function)
    if function.getFunctionName() == "badFunction"
      parser.error("badFunction doesn't validate")
    elsif function.getFunctionName() == "textOnly"
      if parser.getCurrentParseContext() != Context::TEXT
        parser.error("textOnly() can only be used in text context")
      end
    end
  end
end

class TestFunctionRenderer
  ArgumentRequirement = Java::OrgHaploTemplateHtml::FunctionBinding::ArgumentRequirement
  def renderFunction(builder, binding)
    case binding.getFunctionName()
    when "generic-function"
      builder.append("TEST GENERIC FUNCTION RENDER")
      true
    when 'textOnly'
      builder.append('TEXT ONLY')
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
        when 'a'; str = binding.hasArguments().to_s
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
    'Java::JavaUtil::LinkedHashMap' => 'Hash',
    'Java::OrgMozillaJavascript::NativeObject' => 'Hash'
  }
  def obj_with_class(obj)
    obj.to_s+"/"+(CLASS_NAME_MAP[obj.class.name] || obj.class.name)
  end
end

class JSTestFunctionRenderer
  def initialize(renderer)
    @renderer = renderer
  end
  def renderFunction(owner, builder, binding)
    @renderer.renderFunction(builder, binding)
  end
end

class JSIncludedTemplateRenderer
  def initialize(renderer)
    @renderer = renderer
  end
  def renderIncludedTemplate(owner, *args)
    @renderer.renderIncludedTemplate(*args)
  end
end

# ---------------------------------------------------------------------------

JSScriptableObject = Java::OrgMozillaJavascript::ScriptableObject
JSContext = Java::OrgMozillaJavascript::Context
$jscontext = JSContext.enter();
$jscontext.setLanguageVersion(JSContext::VERSION_1_7)
$jsscope = $jscontext.initStandardObjects()
JSScriptableObject.defineClass($jsscope, Java::OrgHaploTemplateDriverRhinojs::HaploTemplate.java_class)
JSScriptableObject.defineClass($jsscope, Java::OrgHaploTemplateDriverRhinojs::HaploTemplateDeferredRender.java_class)
JSScriptableObject.defineClass($jsscope, Java::OrgHaploTemplateDriverRhinojs::JSFunctionThis.java_class)

def view_json_to_rhino(json)
  $jsscope.put('inputJSON', $jsscope, json)
  $jscontext.evaluateString($jsscope, "parsedJSON = JSON.parse(inputJSON);", "<testjson>", 1, nil);
  $jsscope.get('parsedJSON', $jsscope)
end

# ---------------------------------------------------------------------------

def maybe_add_in_deferred_render(view_kind, view)
  value = case view_kind
    when :java; view.get("deferred")
    when :rubyjson; view["deferred"]
    when :js; view.get("deferred", view)
  end
  if(value.kind_of?(String) && value =~ /\A\*\*/)
    # Contains a magic var, create a new deferred render
    template = $template_for_deferred
    render_view = {"value1" => "Deferred view"}
    case view_kind
      when :java; view.put("deferred", template.deferredRender(NestedJavaDriver.new(view_value_to_java(render_view))))
      when :rubyjson; view["deferred"] = template.deferredRender(JRubyJSONDriver.new(render_view))
      when :js; view.put("deferred", view, template.deferredRender(RhinoJavaScriptDriver.new(view_json_to_rhino('{"value1":"Deferred view"}'))))
    end
  end
  view
end

# ---------------------------------------------------------------------------

JavaSystem = Java::JavaLang::System
template_render_time = 0
number_of_renders = 0
tests = 0
passed = 0
failed = 0
files = Dir.glob("test/case/**/*.*").sort
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
        template = Parser.new(template_source.strip, "expected-parse-error", TestParserConfiguration.new).parse()
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
          template = Parser.new(required_cmd.call.strip, "test-case", TestParserConfiguration.new).parse()
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
      drivers << NestedJavaDriver.new(maybe_add_in_deferred_render(:java, view_value_to_java(view)))
      drivers << JRubyJSONDriver.new(maybe_add_in_deferred_render(:rubyjson, view))
      drivers << RhinoJavaScriptDriver.new(maybe_add_in_deferred_render(:js, view_json_to_rhino(view_json)))
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
            driver.setIncludedTemplateRenderer(included_template_renderer)
            # This is not a very good benchmark
            render_start = JavaSystem.nanoTime()
            output = template.renderString(driver)
            template_render_time += JavaSystem.nanoTime() - render_start
            number_of_renders += 1
          end
        rescue Java::OrgHaploTemplateHtml::RenderException => render_exception
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

# Also run some tests of the Rhino JavaScript integration
Java::OrgHaploTemplateDriverRhinojs::JSPlatformIntegration.parserConfiguration = TestParserConfiguration.new
Java::OrgHaploTemplateDriverRhinojs::JSPlatformIntegration.includedTemplateRenderer = JSIncludedTemplateRenderer.new(included_template_renderer)
Java::OrgHaploTemplateDriverRhinojs::JSPlatformIntegration.platformFunctionRenderer = JSTestFunctionRenderer.new(TestFunctionRenderer.new)
$jsscope.put('$testcount', $jsscope, 0.to_java)
$jsscope.put('$testpass', $jsscope, 0.to_java)
$jscontext.evaluateString($jsscope, File.read("test/rhino.js"), "test/rhino.js", 1, nil);
js_test_count = $jsscope.get('$testcount', $jsscope).to_i
raise "No JS tests" unless js_test_count > 0
tests += js_test_count
passed += (js_test_pass =  $jsscope.get('$testpass', $jsscope).to_i)
failed += js_test_count - js_test_pass

puts
puts (failed == 0) ? "PASSED" : "FAILED"
puts "Approx render time per template: #{(template_render_time.to_f / number_of_renders / 1000000).round(2)} ms"
puts "#{tests} tests, #{passed} passed, #{failed} failed, in #{files.length} files"
