# coding: UTF-8

# Haplo Safe View Templates                          http://haplo.org
# (c) Haplo Services Ltd 2015 - 2016    http://www.haplo-services.com
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

RSpec.describe do
  it "compiles templates containing literals" do
    expect(HsvtRubyCompiler.compile('<div class="hello"> "abc\\n\\"" </div>', 'literals-only')).to eq <<'__EOT'
_hsvt_out = '' # literals-only
_hsvt_out << "<div class=hello>abc\n\"</div>"
_hsvt_out
__EOT
  end
end
