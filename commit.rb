#!/usr/bin/env ruby

require 'js_base'
require 'trollop'

class ProgramException < Exception; end

class Commit


  ANDROID_PROJECTS = "RBuddyApp"

  def initialize
    @options = nil
  end

  def run(argv)

    @options = parse_arguments(argv)
    @detail = @options[:detail]
    @verbose = @options[:verbose] || @detail

    @saved_directory = Dir.pwd

    begin
      run_java_tests if @options[:java]
      run_android_tests if @options[:android]

      # ...to do: commit stuff

    ensure
      Dir.chdir(@saved_directory)
    end
  end


  def parse_arguments(argv)
    p = Trollop::Parser.new do
      banner <<-EOS
      Runs unit tests, generates commit for RBuddy project
      EOS
      opt :clean, "clean projects before running tests"
      opt :detail, "display lots of detail"
      opt :verbose, "display progress"
      opt :java,   "include Java projects", :default=>true
      opt :android,"include Android projects",:default=>true
    end

    Trollop::with_standard_exception_handling p do
      p.parse argv
    end
  end

  def run_android_tests
    ANDROID_PROJECTS.split.each do |project_root|
      proj_main = File.join(@saved_directory,project_root)
      proj_test = proj_main + "Test"
      echo "...Android project: #{project_root}"

      Dir.chdir(proj_test)

      runcmd("ant clean","...cleaning") if @options[:clean]
      runcmd("ant debug","...building")
      runcmd("ant debug install","...installing")
      runcmd("ant test","...testing")

      Dir.chdir(@saved_directory)

    end
  end


  def run_java_tests
    runcmd("ant test_all_projects","...running Java project tests")
  end

  def runcmd(cmd,message)
    if !@verbose
      echo message
    else
      echo(sprintf("%-40s (%s)",message,cmd))
    end
    output,success = scall(cmd,false)
    if @detail || !success
      puts output
      puts
    end
    if !success
      s = cmd
      if message
        s = "(#{message}) #{s}"
        raise ProgramException,"Problem executing command: #{s}"
      end
    end
    [output,success]
  end

  def echo(msg)
    puts msg
  end

end


if __FILE__ == $0
  Commit.new.run(ARGV)
end
