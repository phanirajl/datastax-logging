package com.datastax.log.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 *  POJO to represent the JSON format sent from each agent
 *
 * @author cingham
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LogDto {
	String filename;
    List<String> lines;
}
