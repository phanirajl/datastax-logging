package com.datastax.log.agent.service

import com.datastax.log.agent.config.Config
import spock.lang.Specification
import spock.lang.Subject

class LogHandlerSpec extends Specification {

    @Subject
    LogHandler logHandler
    LogUploader logUploader
    Config config

    def "setup"() {
        logUploader = Mock()
        config = Mock()
        logHandler = new LogHandler(logUploader, config)
    }

    def "test added lines are uploaded correctly"() {
        given:
            String line1 = "test-1"
            String line2 = "test-2"
            logHandler.collectLine(line1)
            logHandler.collectLine(line2)
        when:
            logHandler.processUpload()
        then:
            1 * logUploader.uploadToServer(*_) >> { arguments ->
                final List<String> lineList = arguments[0]
                assert lineList == [new String(line1), new String(line2)]
            }
    }

    def "test upload is not called when no lines added"() {
        given:
        when:
            logHandler.processUpload()
        then:
            0 * logUploader.uploadToServer(*_)
    }

    def "test added lines are removed after upload success"() {
        given:
            String line1 = "test-1"
            String line2 = "test-2"
            logHandler.collectLine(line1)
            logHandler.collectLine(line2)
            logUploader.uploadToServer(_) >> true
        when:
            logHandler.processUpload()
        then:
            assert logHandler.collectionList.size() == 0
            assert logHandler.uploadList.size() == 0
    }

    def "test added lines are still in place after upload error"() {
        given:
            String line1 = "test-1"
            String line2 = "test-2"
            logHandler.collectLine(line1)
            logHandler.collectLine(line2)
            logUploader.uploadToServer(_) >> false
        when:
            logHandler.processUpload()
        then:
            assert logHandler.collectionList.size() == 2
            assert logHandler.uploadList.size() == 0
    }
}

