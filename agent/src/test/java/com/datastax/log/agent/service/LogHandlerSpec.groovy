package com.datastax.log.agent.service

import com.datastax.log.agent.config.Config
import spock.lang.Specification
import spock.lang.Subject

class LogHandlerSpec extends Specification {

    @Subject
    LogHandler logHandler
    LogUploader logUploader
	LogCollector collector
    Config config
	List<String> testLines

    def "setup"() {
		testLines = Arrays.asList("test1", "test2")
        logUploader = Mock()
        config = Mock()
		collector = Mock()
        logHandler = new LogHandler(logUploader, config)
		logHandler.addLogCollector(collector)
    }

	def "test upload is not called when no lines exist"() {
		given:
			collector.hasLinesToUpload() >> false
			collector.beforeUpload() >> testLines
		when:
			logHandler.processLogCollectors()
		then:
			1 * collector.hasLinesToUpload()
			0 * collector.beforeUpload()
			0 * logUploader.uploadToServer(*_)
	}

	def "test added lines are uploaded correctly"() {
		given:
			collector.hasLinesToUpload() >> true
			collector.beforeUpload() >> testLines
			logUploader.uploadToServer(_,_) >> true
		when:
			logHandler.processLogCollectors()
		then:
			1 * logUploader.uploadToServer(_,_) >> { arguments ->
				final List<String> lineList = arguments[1]
				assert lineList == [new String("test1"), new String("test2")]
			}
	}

	def "test successful upload notifies collector of status"() {
		given:
			collector.hasLinesToUpload() >> true
			collector.beforeUpload() >> testLines
			logUploader.uploadToServer(_,_) >> true
		when:
			logHandler.processLogCollectors()
		then:
			1 * collector.afterUpload(_) >> { args ->
				assert args[0] == true
			}
	}

	def "test failed upload notifies collector of status"() {
		given:
			collector.hasLinesToUpload() >> true
			collector.beforeUpload() >> testLines
			logUploader.uploadToServer(_,_) >> false
		when:
			logHandler.processLogCollectors()
		then:
			1 * collector.afterUpload(_) >> { args ->
				assert args[0] == false
			}
	}
}

