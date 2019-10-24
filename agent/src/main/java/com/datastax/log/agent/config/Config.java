package com.datastax.log.agent.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotEmpty;

/**
 * Configuration loaded by SpringBoot to gather parameters from application.yml
 *
 * @author cingham
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("log-agent")
@Getter
@Setter
public class Config {
	@NotEmpty
    private String clientId;
	@NotEmpty
    private String hostUrl;
	@NotEmpty
    private int delayBetweenUploads;
}
