
require 'json'

nestedjava_inclusions = java.util.HashMap.new
nestedjava_inclusions.put("template1", Java::Template::Parser.new(<<__E).parse())
  <b> "Included Template 1: " value1 </b>
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
    m = java.util.HashMap.new
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
  template = Java::Template::Parser.new(File.read(ARGV[1])).parse()
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

try_quoting

tests = 0
passed = 0
failed = 0
files = Dir.glob("test-case/**/*.*").sort
files.each do |filename|
  comment, template_source, *rest = File.read(filename).split("\n---\n")
  raise "Bad test #{filename} - no tests" if rest.empty?
  was_testing_error = false
  begin
    template = Java::Template::Parser.new(template_source.strip).parse()
  rescue => e
    was_testing_error = true
    tests += 1
    if rest.length == 1 && rest.first =~ /\A\s*PARSE EXCEPTION:\s*(.+?)\s*\z/m && $1 == e.message
      passed += 1
    else
      failed += 1
      puts
      puts "#{filename}: Unexpected exception:"
      puts "EXPECTED: #{$1}"
      puts "OUTPUT:   #{e.message}"
      puts comment.strip
    end
  end
  while rest.length >= 2
    tests += 1
    view_json = rest.shift.strip
    expected_output = rest.shift.strip
    view = JSON.parse(view_json)
    driver = Java::TemplateDriverNestedjava::NestedJavaDriver.new(view_value_to_java(view), nestedjava_inclusions)
    output = template.renderString(driver)
    if output == expected_output
      passed += 1
    else
      failed += 1
      puts
      puts "#{filename}: Bad template output"
      puts comment.strip
      puts "EXPECTED: #{expected_output}"
      puts "OUTPUT:   #{output}"
    end
  end
  unless rest.empty? || (was_testing_error && rest.length == 1)
    raise "Incomplete tests in #{filename}"
  end
end

puts
puts (failed == 0) ? "PASSED" : "FAILED"
puts "#{tests} tests, #{passed} passed, #{failed} failed, in #{files.length} files"
