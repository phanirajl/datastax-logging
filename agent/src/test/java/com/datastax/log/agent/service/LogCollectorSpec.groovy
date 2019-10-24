package com.datastax.log.agent.service

import spock.lang.Specification
import spock.lang.Subject

class LogCollectorSpec extends Specification {

    @Subject
    LogCollector logCollector

    def "setup"() {
        File file = new File("foo.bar")
        logCollector = new LogCollector(file)
    }

	def "test empty list"() {
		when:
			boolean result = logCollector.hasLinesToUpload()
		then:
			result == false
	}

    def "test handle() adds lines correctly"() {
        given:
			String line1 = "test-1"
			String line2 = "test-2"
        when:
			logCollector.handle(line1)
			logCollector.handle(line2)
        then:
			logCollector.hasLinesToUpload() == true
    }

	def "test prepareForUpload() moves lists correctly"() {
		given:
			String line1 = "test-1"
			String line2 = "test-2"
			logCollector.handle(line1)
			logCollector.handle(line2)
		when:
			List<String> result = logCollector.beforeUpload()
		then:
			logCollector.collectionList.size() == 0
			logCollector.uploadList.size() == 2
			result.size() == 2
	}

	def "test successful upload clears lists correctly"() {
		given:
			String line1 = "test-1"
			String line2 = "test-2"
			logCollector.handle(line1)
			logCollector.handle(line2)
		when:
			logCollector.beforeUpload()
			logCollector.afterUpload(true)
		then:
			logCollector.collectionList.size() == 0
			logCollector.uploadList.size() == 0
	}

	def "test failed upload concatenates lists correctly"() {
		given:
			String line1 = "test-1"
			String line2 = "test-2"
			String line3 = "test-3"
			logCollector.handle(line1)
			logCollector.handle(line2)
		when:
			logCollector.beforeUpload()	// about to upload
			logCollector.handle(line3)	// another line came in asynchronously
			logCollector.afterUpload(false)	// upload failed
		then:
			logCollector.collectionList.size() == 3
			logCollector.uploadList.size() == 0
			// check correct ordering
			logCollector.collectionList.get(0).equals(line1)
			logCollector.collectionList.get(1).equals(line2)
			logCollector.collectionList.get(2).equals(line3)
	}
}
