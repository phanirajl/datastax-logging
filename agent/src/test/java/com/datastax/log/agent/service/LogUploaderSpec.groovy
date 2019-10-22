package com.datastax.log.agent.service

import com.datastax.log.agent.config.Config
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.lang.Subject

import java.rmi.ServerException

class LogUploaderSpec  extends Specification {
    @Subject
    LogUploader logUploader
    RestTemplate restTemplate
    Config config
    List<String> inputList

    def "setup"() {
        inputList = Arrays.asList("test-1", "test-2")
        restTemplate = Mock()
        config = Mock()
        logUploader = new LogUploader(restTemplate, config)
    }

    def "test success when restTemplate response is OK"() {
        given:
            restTemplate.postForEntity(_,_,_) >>
                    new ResponseEntity(new String("foo"), HttpStatus.OK)
        when:
            boolean success = logUploader.uploadToServer(inputList)
        then:
            success == true
    }

    def "test not success when restTemplate response is not OK"() {
        given:
            restTemplate.postForEntity(_,_,_) >>
                    new ResponseEntity(new String("foo"), HttpStatus.BAD_REQUEST)
        when:
            boolean success = logUploader.uploadToServer(inputList)
        then:
            success == false
    }

	def "test not success when exception is thrown"() {
		given:
			restTemplate.postForEntity(_,_,_) >> { throw new ServerException("foo") }
		when:
			boolean success = logUploader.uploadToServer(inputList)
		then:
			success == false
	}
}
