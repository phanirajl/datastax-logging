package com.datastax.log.agent

import spock.lang.Specification
import spock.lang.Subject

class CommandLineStartupRunnerSpec extends Specification {

    @Subject
    CommandLineStartupRunner commandLineStartupRunner

    def "setup"() {
        commandLineStartupRunner = new CommandLineStartupRunner()
    }

    def "test checkForFileParameter() is missing filename"() {
        when:
            commandLineStartupRunner.checkForFileParameter()
        then:
            thrown RuntimeException
    }

    def "test checkForFileParameter() file does not exist"() {
        when:
            commandLineStartupRunner.checkForFileParameter("foo.log")
        then:
            thrown RuntimeException
    }

    def "test checkForFileParameter() file does exist"() {
        when:
            // Testing the existence of a known file is difficult because it depends on OS, environment, etc.
            // (Note - this is not the same as the application classpath).
            // We'll simply check for the root directory of the current file system.
            List<File> inputFiles = commandLineStartupRunner.checkForFileParameter("/")
        then:
			inputFiles.size() == 1
            inputFiles.get(0).exists()
    }

	def "test checkForFileParameter() for multiple files created"() {
		when:
			List<File> inputFiles = commandLineStartupRunner.checkForFileParameter("/", "/")
		then:
			inputFiles.size() == 2
	}
}
