# coding: UTF-8

# Haplo Safe View Templates                          http://haplo.org
# (c) Haplo Services Ltd 2015 - 2016    http://www.haplo-services.com
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.


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

class JSTestTextTranslator
  def getTextTranslator(owner, category)
    TextTranslator.new(category)
  end
  class TextTranslator
    def initialize(category)
      @given_category = category
      @count = 0
    end
    def translate(category, text)
      raise "Bad category" unless @given_category == category
      @count += 1
      "JS:#{@count}:#{category}:#{text.upcase}"
    end
  end
end
