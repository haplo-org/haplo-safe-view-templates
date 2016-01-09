# coding: UTF-8

# Haplo Safe View Templates                          http://haplo.org
# (c) Haplo Services Ltd 2015 - 2016    http://www.haplo-services.com
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.


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

