package com.datastax.log.agent.service;

import java.io.File;
import java.util.Collections;
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

	private final String hostUrl;
	private final HttpHeaders headers;
	private final RestTemplate restTemplate;

	/**
	 * Injection constructor
	 *
	 * @param restTemplate the RestTemplate used to send requests to the server
	 * @param config app config settings
	 */
	protected LogUploader(@Qualifier("serviceRestTemplate") RestTemplate restTemplate, Config config) {
		this.restTemplate = restTemplate;

		// url should contain the clientId
		this.hostUrl = addClientToUrl(config.getHostUrl(), config.getClientId());

		// setup headers once so we don't do it for each request
		headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
	}

	/**
	 * Send a batch of log lines to the host server.
	 * The url should be in a format like "http://hostname.com/log-aggregator/{clientId}
	 * The filename and log lines will be wrapped in the LogDto payload as JSON
	 *
	 * @param file filename to specify in the upload
	 * @param lines line list of the log file
	 * @return success - if the server accepted the payload
	 */
	public boolean uploadToServer(File file, List<String> lines) {
		LogDto logDto = new LogDto(file.getName(), lines);

		try {
			HttpEntity<LogDto> entity = new HttpEntity<>(logDto, headers);
			ResponseEntity response = restTemplate.exchange(hostUrl, HttpMethod.POST, entity, String.class);
			if (response.getStatusCode() == HttpStatus.OK) {
				logger.info("Successfully uploaded {} lines to server.", lines.size());
				return true;
			} else {
				logger.error("Error uploading to {}, status={}, message={}", hostUrl,
					response.getStatusCode(), response.getBody());
				return false;
			}
		} catch (IllegalArgumentException iae) {
			// problem with URL format so we can never report to server,
			// try to exit the app via this RuntimeException.
			logger.error("Host URL is not valid: {}, exception={}", hostUrl, iae.toString());
			throw iae;
		} catch (Exception ex) {
			// other exceptions may be network timeouts, etc. so we'll log and try again later.
			logger.error("Error connecting to {}, exception={}", hostUrl, ex.toString());
			return false;
		}
	}

	private static String addClientToUrl(String baseUrl, String clientId) {
		if (baseUrl.charAt(baseUrl.length()-1) != '/') {
			return baseUrl + '/' + clientId;
		} else {
			return baseUrl + clientId;
		}
	}
}

