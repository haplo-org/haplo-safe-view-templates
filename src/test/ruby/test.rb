# coding: UTF-8

# Haplo Safe View Templates                          http://haplo.org
# (c) Haplo Services Ltd 2015 - 2016    http://www.haplo-services.com
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.


require 'json'
require 'rspec'

Parser = Java::OrgHaploTemplateHtml::Parser
NestedJavaDriver = Java::OrgHaploTemplateDriverNestedjava::NestedJavaDriver
JRubyJSONDriver = Java::OrgHaploTemplateDriverJrubyjson::JRubyJSONDriver
RhinoJavaScriptDriver = Java::OrgHaploTemplateDriverRhinojs::RhinoJavaScriptDriver

# ---------------------------------------------------------------------------

included_templates = {}
Dir.glob("test/included-templates/*.hsvt").sort.each do |filename|
  filename =~ /\/([^\/]+?)\.hsvt\z/
  included_templates[$1] = Parser.new(File.read(filename), $1).parse()
end

included_template_renderer = Java::OrgHaploTemplateHtml::SimpleIncludedTemplateRenderer.new(included_templates)

$template_for_deferred = included_templates['template1']

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
    ['ping -- hello','ping - - hello',Context::COMMENT],
    ['<x>','<x>',Context::COMMENT],
    ['"hello"','&quot;hello&quot;',Context::ATTRIBUTE_VALUE]
  ].each do |input, escaped, attribute_context|
    it "escapes \"#{input}\" as \"#{escaped}\" in the context #{attribute_context}" do
      output = Java::OrgHaploTemplateHtml::Escape.escapeString(input, attribute_context)
      expect(output).to eq(escaped)
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

require "#{File.dirname(__FILE__)}/test_parser_config"
require "#{File.dirname(__FILE__)}/test_function_renderer"
require "#{File.dirname(__FILE__)}/test_rhino_integration"

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

class TextTranslator
  def getLocaleId
    "en"
  end
  def translate(category, text)
    # include category, and upcase everything which isn't in {} to avoid upcasing interpolations
    nesting = 0
    text2 = text.chars.map do |c|
      if c == '{'
        nesting += 1
      elsif c == '}'
        nesting -= 1
      end
      nesting == 0 ? c.upcase : c
    end .join('')
    "#{category}:#{text2}"
  end
end

# ---------------------------------------------------------------------------

files = Dir.glob("test/case/**/*.*").sort
files.each do |filename|
  comment, *commands = File.open(filename, "r:UTF-8") { |f| f.read }.split("\n---\n")
  raise "Bad test #{filename} - no tests" if commands.empty?

  describe "#{filename.gsub(/^test\/case\/|\.txt$/, '')} - #{comment.strip}" do
    test_case_index = 0

    if comment =~ /^\s*PARSE ERROR:/
      # Test one or more parse errors
      raise "Expected an even number of test elements in #{filename}" if commands.length.odd?

      commands.each_slice(2).each do |template_source, expected_error|
        test_case_index += 1
        it "Test Case ##{test_case_index}" do
          exception = nil
          begin
            Parser.new(template_source.strip, "expected-parse-error", TestParserConfiguration.new).parse()
          rescue => e
            exception = e
          end
          message = exception ? exception.message : '(exception not thrown)'
          expect(message).to eq(expected_error.strip)
        end
      end
    else
      current_template_string = nil
      test_cases_by_template = commands.chunk do |string|
        if string =~ /NEW TEMPLATE(:\s*(.+))?/
          current_template_string = nil
          next
        end
        if current_template_string.nil?
          current_template_string = string
          next
        end
        current_template_string
      end

      test_cases_by_template.each do |template_string, template_test_cases|
        raise "Expected an even number of test elements in #{filename}" if template_test_cases.length.odd?

        template_test_cases.each_slice(2).each do |view_json, expected_output|
          expected_output.strip!

          test_case_index += 1
          it "Test Case ##{test_case_index}" do
            view = JSON.parse(view_json)
            template = Parser.new(template_string.strip, "test-case", TestParserConfiguration.new).parse()

            if expected_output =~ /\ATREE:\s*(.+)\z/m
              # Testing the tree, not the rendered output
              expected_output = $1

              output = template.dump().strip
              expect(output).to eq(expected_output)
            else
              [
                ['nested Java driver',
                 NestedJavaDriver.new(maybe_add_in_deferred_render(:java, view_value_to_java(view)))],
                ['JRuby JSON driver',
                 JRubyJSONDriver.new(maybe_add_in_deferred_render(:rubyjson, view))],
                ['Rhino JavaScript driver',
                 RhinoJavaScriptDriver.new(maybe_add_in_deferred_render(:js, view_json_to_rhino(view_json)))]
              ].each do |driver_name, driver|
                driver.setFunctionRenderer(TestFunctionRenderer.new)
                driver.setIncludedTemplateRenderer(included_template_renderer)
                driver.setTextTranslator(TextTranslator.new)
                begin
                  output = template.renderString(driver)
                rescue Java::OrgHaploTemplateHtml::RenderException => render_exception
                  output = "RENDER ERROR: #{render_exception.message}"
                end

                expect(output).to eq(expected_output),
                  -> { "#{driver_name} failed.\nexpected: #{expected_output.inspect}\n     got: #{output.inspect}" }
              end
            end
          end
        end
      end
    end
  end
end

# Extract translatable strings from template.
describe "translated strings" do
  it "can extra strings from template" do
    template = Parser.new(<<__E, "test-case", TestParserConfiguration.new).parse()
      i("Text!") " "
      <div> i("String 1") </div>
      <form>
        <input type="submit" value=i("Button text")>
      </form>
      do() {
        i("T3")
      } block {
        i("T4")
      }
__E
    extracted = template.extractTranslatedStrings().to_a
    expect(extracted).to eq([
        "Text!",
        "String 1",
        "Button text",
        "T3",
        "T4"
    ])
  end
end

# Also run some tests of the Rhino JavaScript integration
Java::OrgHaploTemplateDriverRhinojs::JSPlatformIntegration.parserConfiguration = TestParserConfiguration.new
Java::OrgHaploTemplateDriverRhinojs::JSPlatformIntegration.includedTemplateRenderer = JSIncludedTemplateRenderer.new(included_template_renderer)
Java::OrgHaploTemplateDriverRhinojs::JSPlatformIntegration.platformFunctionRenderer = JSTestFunctionRenderer.new(TestFunctionRenderer.new)
Java::OrgHaploTemplateDriverRhinojs::JSPlatformIntegration.textTranslatorFactory = JSTestTextTranslator.new()
$jsscope.put('$testcount', $jsscope, 0.to_java)
$jsscope.put('$testpass', $jsscope, 0.to_java)
$jscontext.evaluateString($jsscope, File.read("test/rhino.js"), "test/rhino.js", 1, nil);
js_test_count = $jsscope.get('$testcount', $jsscope).to_i
raise "No JS tests" unless js_test_count > 0

tests = js_test_count
passed = (js_test_pass =  $jsscope.get('$testpass', $jsscope).to_i)
failed = js_test_count - js_test_pass

puts
puts "#{tests} tests, #{passed} passed, #{failed} failed, in #{files.length} files"
exit 1 if failed > 0
