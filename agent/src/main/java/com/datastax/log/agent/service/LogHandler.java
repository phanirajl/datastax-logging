package com.datastax.log.agent.service;

import com.datastax.log.agent.config.Config;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Worker thread to periodically determine whether file lines need to be uploaded.
 * 
 * @author cingham
 */
@Service
public class LogHandler implements Runnable {

	private final List<LogCollector> logCollectors = new ArrayList<>();

	private final LogUploader uploader;
	private final long delayBetweenUploads;

	/**
	 * Injection constructor
	 *
	 * @param uploader class that handles uploading collected log lines to service host
	 * @param config app config options
	 */
	protected LogHandler(LogUploader uploader, Config config) {
		this.uploader = uploader;
		this.delayBetweenUploads = config.getDelayBetweenUploads()*1000;  // seconds to millis
	}

	/**
	 * Add a LogCollector, each of which represents a particular file and it's collected log lines
	 * @param logCollector
	 */
	public void addLogCollector(LogCollector logCollector) {
		this.logCollectors.add(logCollector);
	}

	/**
	 * Worker thread which periodically attempts to upload current batch of lines
	 */
	public void run() {
		try {
			while (!Thread.interrupted()) {
				Thread.sleep(delayBetweenUploads);
				processLogCollectors();
			}
		} catch(InterruptedException ie) {
			// interrupted during sleep(), just exit thread
		}
		shutdown();
	}

	/**
	 * Check each file we are watching and process upload as necessary
	 */
	private void processLogCollectors() {
		for (LogCollector logCollector : logCollectors) {
			processUpload(logCollector);
		}
	}

	/**
	 * For a given file, upload any new lines that came in
	 * @param logCollector
	 */
	private void processUpload(LogCollector logCollector) {
		if (!logCollector.hasLinesToUpload()) {
			return;		// nothing to do
		}

		// let the collector prepare its internal lists and give us the lines ready to upload
		List<String> lines = logCollector.beforeUpload();

		// do the upload
		boolean success = uploader.uploadToServer(logCollector.getFile(), lines);

		// let the collector know the status so it can update its lists accordingly
		logCollector.afterUpload(success);
	}

	/**
	 * Cleanup child thread resources (each LogCollector) on app shutdown
	 */
	private void shutdown() {
		for (LogCollector logCollector : logCollectors) {
			logCollector.shutdown();
		}
	}
}
