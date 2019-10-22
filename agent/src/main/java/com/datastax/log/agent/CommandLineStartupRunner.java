package com.datastax.log.agent;

import com.datastax.log.agent.service.LogCollector;
import com.datastax.log.agent.service.LogHandler;
import org.apache.commons.io.input.Tailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.File;

/**
 * SpringBoot provides a CommandLineRunner hook to startup application resources.
 * We'll use it to read the input log filename from the command line,
 * and start the worker threads used for log line processing.
 *
 * @author cingham
 */
@Component
public class CommandLineStartupRunner implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(CommandLineStartupRunner.class);

    @Autowired
    LogCollector logCollector;

    @Autowired
    LogHandler logHandler;

    Tailer tailer;
    Thread logHandlerThread;

	/**
	 * Entry point provided by SpringBoot
	 * @param args command line args
	 * @throws Exception
	 */
	@Override
    public void run(String...args) throws Exception {
        File inputFile = checkForFileParameter(args);

        // start thread to continuously read in log file lines using the apache Tailer interface.  See:
        // https://commons.apache.org/proper/commons-io/javadocs/api-2.4/org/apache/commons/io/input/Tailer.html
        tailer = Tailer.create(inputFile, logCollector);

        // start thread to periodically upload collected log lines
        Thread logHandlerThread = new Thread(logHandler);
        logHandlerThread.start();
    }

	/**
	 * Check that a valid input filename was provided on the command line
	 * @param args
	 * @return the File object representing the specified filename
	 */
	private File checkForFileParameter(String...args) {
        if (args.length == 0 || args[0] == null || args[0].length() == 0) {
            throw new RuntimeException("Missing filename argument on command line");
        }
        String filename = args[0];
        File inputFile = new File(filename);
        if (!inputFile.exists()) {
            throw new RuntimeException("Cannot find file: " + filename);
        }
        return inputFile;
    }

    /**
     * Cleanup thread resources on app shutdown
     */
    @PreDestroy
    public void shutdown() {
        if (tailer != null) {
            tailer.stop();
        }

        if (logHandlerThread != null) {
            logHandlerThread.interrupt();
        }
    }
}

