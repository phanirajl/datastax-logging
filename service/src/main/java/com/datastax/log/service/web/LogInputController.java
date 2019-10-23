package com.datastax.log.service.web;

import com.datastax.log.service.dto.LogDto;
import com.datastax.log.service.dto.ServerResponseDto;
import com.datastax.log.service.service.LogAppender;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Rest Controller to handle incoming requests from the agents.
 *
 * @author cingham
 */
@RestController
public class LogInputController {

	LogAppender logAppender;

	/**
	 * Injection constructor
	 *
	 * @param logAppender class to handle log file appending
	 */
	LogInputController(LogAppender logAppender) {
		this.logAppender = logAppender;
	}

	/**
	 * Main Rest endpoint to post new log data, in the format:
	 * 			/log-aggregator/{clientId}
	 * The body of the request should contain a JSON representation of the LogDto
	 *
	 * @param clientId from the URI
	 * @param logDto from the request body
	 * @return ServerResponseDto with the status
	 * @throws Exception any error encountered, will be handled by the ControllerExceptionHandler
	 */
	@PostMapping(value = "/log-aggregator/{clientId}", consumes = "application/json",
		produces = "application/json")
	public ServerResponseDto postToLog(@PathVariable String clientId,
							@RequestBody LogDto logDto) throws Exception {
		logAppender.appendToFile(clientId, logDto.getFilename(), logDto.getLines());
		return new ServerResponseDto(HttpStatus.OK.value(), "Success");
	}
}
