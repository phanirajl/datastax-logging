package com.datastax.log.service.web

import com.datastax.log.service.dto.LogDto
import com.datastax.log.service.dto.ServerResponseDto
import com.datastax.log.service.service.LogAppender
import spock.lang.Specification
import spock.lang.Subject

class LogInputControllerSpec extends Specification {

	@Subject
	LogInputController controller
	LogAppender logAppender
	List<String> testLines
	String filename
	LogDto logDto

	def "setup"() {
		testLines = Arrays.asList("test-1", "test-2")
		filename = "foobar.log"
		logDto = new LogDto(filename, testLines)
		logAppender = Mock()
		controller = new LogInputController(logAppender)
	}

	def "test that no exception returns success"() {
 		when:
			ServerResponseDto dto = controller.postToLog("clientA", logDto)
		then:
			dto.getStatus() == 200
			dto.getMessage().contains("Success")
	}
}
