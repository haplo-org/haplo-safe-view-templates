# coding: UTF-8

# Haplo Safe View Templates                          http://haplo.org
# (c) Haplo Services Ltd 2015 - 2016    http://www.haplo-services.com
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.


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
        when 'A'; str = binding.allValueArguments().map { |x| x.to_s } .join('!')
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
