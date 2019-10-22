package com.datastax.log.agent.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration loaded by SpringBoot to gather parameters from application.yml
 *
 * @author cingham
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("agent")
@Getter
@Setter
public class Config {
    private String clientId;
    private String hostUrl;
    private int delayBetweenUploads;
}
