package com.datastax.log.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 *  POJO to represent the JSON response returned from the server
 *
 * @author cingham
 */
@AllArgsConstructor
@Getter
@Setter
public class ServerResponseDto {
	int status;
	String message;
}
