package com.datastax.log.agent.service;

import java.util.List;

import com.datastax.log.agent.config.Config;
import com.datastax.log.agent.dto.LogDto;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;

/**
 * Does the work of sending a list of lines to the host server.
 * 
 * @author cingham
 */
@Service
public class LogUploader {
    private static final Logger logger = LoggerFactory.getLogger(LogUploader.class);

	private final String clientId;
	private final String hostUrl;
	private final RestTemplate restTemplate;

	protected LogUploader(@Qualifier("serviceRestTemplate") RestTemplate restTemplate, Config config) {
		this.restTemplate = restTemplate;
		clientId = config.getClientId();
		hostUrl = config.getHostUrl();
	}

	/**
	 * Send lines to host server wrapped in a LogDto object (as JSON)
	 * @param lineList
	 * @return success - if the server accepted the payload
	 */
	public boolean uploadToServer(List<String> lineList) {
		LogDto logDto = new LogDto(clientId, lineList);

		try {
			ResponseEntity response = restTemplate.postForEntity(hostUrl, logDto, String.class);
			if (response.getStatusCode() == HttpStatus.OK) {
				logger.info("Successfully uploaded {} lines to server.", lineList.size());
				return true;
			} else {
				logger.error("Error uploading to {}, status={}, message={}", hostUrl,
					response.getStatusCode(), response.getBody());
				return false;
			}
		} catch (IllegalArgumentException iae) {
			// problem with URL format so we can never report to server, try to exit the app.
			logger.error("Host URL is not valid: {}, exception={}", hostUrl, iae.toString());
			throw iae;
		} catch (Exception ex) {
			// other exceptions may be network timeouts, etc. so we'll log and try again later.
			logger.error("Error connecting to {}, exception={}", hostUrl, ex.toString());
			return false;
		}
	}
}

