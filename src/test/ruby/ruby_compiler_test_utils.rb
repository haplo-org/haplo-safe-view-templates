# coding: UTF-8

# Haplo Safe View Templates                          http://haplo.org
# (c) Haplo Services Ltd 2015 - 2016    http://www.haplo-services.com
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

module HsvtRubyCompiler
  Parser = Java::OrgHaploTemplateHtml::Parser
  RubyCompiler = Java::OrgHaploTemplateCompileRuby::RubyCompiler

  def self.compile(source, name)
    template = Parser.new(source, name).parse()
    compiler = RubyCompiler.new
    template.compile(compiler)
    compiler.output()
  end
end
