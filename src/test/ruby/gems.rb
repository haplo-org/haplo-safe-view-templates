INSTALL_DIR = File.absolute_path(File.dirname(__FILE__) + '/../../../gems')
GEMS = [
  { name: 'rspec', version: '3.4.0' }
]

require 'ostruct'
require 'rubygems/commands/install_command'

GEMS.collect(&OpenStruct.method(:new)).each do |gem|
  command = Gem::Commands::InstallCommand.new
  command.handle_options ["--install-dir", INSTALL_DIR, "--no-ri", "--no-rdoc", gem.name, '--version', gem.version]
  begin
    command.execute
  rescue Gem::SystemExitException => e
    raise e if e.exit_code > 0
  end
end
