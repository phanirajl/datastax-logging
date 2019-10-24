package com.datastax.log.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 *  POJO to represent the JSON format we will upload to the service
 *
 * @author cingham
 */
@AllArgsConstructor
@Getter
@Setter
public class LogDto {
    String filename;
    List<String> lines;
}
