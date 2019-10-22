package com.datastax.log.agent.service

import spock.lang.Specification
import spock.lang.Subject

class LogCollectorSpec extends Specification {

    @Subject
    LogCollector logCollector
    LogHandler logHandler

    def "setup"() {
        logHandler = Mock()
        logCollector = new LogCollector(logHandler)
    }

    def "test handle() passes the line correctly"() {
        given:
            String line = "test-1"
        when:
            logCollector.handle(line)
        then:
            1 * logHandler.collectLine(_)
    }
}
