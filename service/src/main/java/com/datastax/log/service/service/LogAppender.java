package com.datastax.log.service.service;

import com.datastax.log.service.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

/**
 * Adds lines to the local log file
 * 
 * @author cingham
 */
@Service
public class LogAppender {
    private static final Logger logger = LoggerFactory.getLogger(LogAppender.class);

    private final File filePath;

	/**
	 * Injection constructor
	 *
	 * @param config app config options
	 */
	LogAppender(Config config) {
		filePath = new File(config.getFilePath());
		// make sure path exists
		filePath.mkdirs();
	}

	/**
	 * Opens a local log file (creates it if new) for appending.
	 * The filename will be in the format:
	 * 		{filePath}/{clientId}-{filename}
	 *
	 * 	The filePath location must be defined in application.yml.
	 * 	This naming convention allows for multiple files from the same client,
	 * 	and also prevents possible duplicate filenames from different clients.
	 *
	 * @param clientId the clientId send from the agent
	 * @param filename the filename sent from the agent
	 * @param lines the lines of the log sent from the agent
	 * @throws Exception any IO error encountered
	 *
	 */
	public void appendToFile(String clientId, String filename, List<String> lines) throws Exception {

		File theFile = new File(filePath, clientId + "-" + filename);

		try {
			// open file for appending
			BufferedWriter out = new BufferedWriter(new FileWriter(theFile, true));
			for (String line : lines) {
				out.write(line);
				out.newLine();
			}
			out.close();
			logger.info("Added {} lines to file {}.", lines.size(), theFile);
		} catch (Exception ex) {
			// log the error and rethrow so the controller can relay the error to the client
			logger.error("Error writing to file {}, exception={}", theFile, ex.toString());
			throw ex;
		}
	}
}
