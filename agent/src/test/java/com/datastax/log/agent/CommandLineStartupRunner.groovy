package com.datastax.log.agent

import com.datastax.log.agent.config.Config
import com.datastax.log.agent.service.LogHandler
import com.datastax.log.agent.service.LogUploader
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
            File inputFile = commandLineStartupRunner.checkForFileParameter("/")
        then:
            inputFile.exists()
    }
}
