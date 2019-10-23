package com.datastax.log.service.web;

import com.datastax.log.service.dto.ServerResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Handle any Exception encountered during controller processing
 * by returning our custom DTO to the client.
 *
 * @author cingham
 */
@RestControllerAdvice(annotations = RestController.class)
@Slf4j
public class ControllerExceptionHandler {

	/**
	 * Handle any Exception
	 *
	 * @param ex the Exception encountered during processing
	 * @return ServerResponseDto our custom DTO containing the status
	 */
	@ExceptionHandler
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ServerResponseDto handleGenericException(Exception ex) {
		return new ServerResponseDto(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.toString());
	}
}
