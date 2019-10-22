package com.datastax.log.agent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration bean providing the RestTemplate used to upload to the server.
 * Wrapping the RestTemplate in a bean allows for easier Mocks in unit tests.
 *
 * @author cingham
 */
@Configuration
public class RestTemplateConfiguration {

    @Bean(name = "serviceRestTemplate")
    public RestTemplate getServiceRestTemplate() {
        return new RestTemplate();
    }
}
