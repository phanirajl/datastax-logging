package com.datastax.log.service.web

import com.datastax.log.service.dto.ServerResponseDto
import spock.lang.Specification
import spock.lang.Subject

class ControllerExceptionHandlerSpec extends Specification {

    @Subject
	ControllerExceptionHandler handler

    def "setup"() {
		handler = new ControllerExceptionHandler()
    }

    def "test that Exception generates custom response Dto"() {
        given:
			Exception ex = new RuntimeException("foobar")
        when:
			ServerResponseDto dto = handler.handleGenericException(ex)
        then:
			dto.getStatus() == 500
			dto.getMessage().contains("foobar")
    }
}
