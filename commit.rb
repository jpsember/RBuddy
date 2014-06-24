#!/usr/bin/env ruby

require 'js_base'
require 'trollop'

class ProgramException < Exception; end

class Commit


  ANDROID_PROJECTS = "RBuddyApp"
  GIT_STATE_FILENAME = ".commit_state"

  def initialize
    @options = nil
  end

  def run(argv)

    @options = parse_arguments(argv)
    @detail = @options[:detail]
    @verbose = @options[:verbose] || @detail
    @current_git_state = nil
    @previous_git_state = nil
    @saved_directory = Dir.pwd

    begin

      read_old_git_state
      determine_current_git_state

      passed_tests = false

      puts "...comparing current to previous states" if @verbose

      if @current_git_state != @previous_git_state

        puts "...states differ, running unit tests" if @verbose

        run_java_tests if @options[:java]
        run_android_tests if @options[:android]

        # Only update the state if it passed all tests
        update_old_git_state
        passed_tests = true

        update_old_git_state
      else
        passed_tests = true
      end

    ensure
      Dir.chdir(@saved_directory)
    end
  end

  def read_old_git_state
    if @options[:clean]
      File.delete(GIT_STATE_FILENAME) if File.exist?(GIT_STATE_FILENAME)
    end
    @previous_git_state = FileUtils.read_text_file(GIT_STATE_FILENAME,"")
  end

  def determine_current_git_state
    @current_git_state,_ = scall("git diff -p")
  end

  def update_old_git_state
    FileUtils.write_text_file(GIT_STATE_FILENAME,@current_git_state)
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
    warning "Not running android tests"
    return if true
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
