package com.datastax.log.agent;

import com.datastax.log.agent.service.LogCollector;
import com.datastax.log.agent.service.LogHandler;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * SpringBoot provides a CommandLineRunner hook to startup application resources.
 * We'll use it to read the input log filename(s) from the command line,
 * and start the worker threads used for log line processing.
 *
 * @author cingham
 */
@Component
public class CommandLineStartupRunner implements CommandLineRunner {
    private final LogHandler logHandler;
    private Thread logHandlerThread;

	CommandLineStartupRunner(LogHandler logHandler) {
		this.logHandler = logHandler;
	}

	/**
	 * Entry point provided by SpringBoot
	 * @param args command line args
	 * @throws Exception
	 */
	@Override
    public void run(String...args) throws Exception {
		// get filename(s) from command line
        List<File> inputFiles = checkForFileParameter(args);

        // for each file startup the collector thread
		for (File file : inputFiles) {
			LogCollector logCollector = new LogCollector(file);
			logHandler.addLogCollector(logCollector);
		}

        // start thread to periodically upload collected log lines
		logHandlerThread = new Thread(logHandler);
        logHandlerThread.start();
    }

	/**
	 * Check that valid input filename(s) are provided on the command line
	 * @param args command line args
	 * @return list of File objects representing the specified filename(s)
	 */
	private List<File> checkForFileParameter(String...args) {
		List<File> fileList = new ArrayList<>();
		int index = 0;
		while (args.length > index && args[index] != null && args[index].length() != 0) {
			String filename = args[index];
			File inputFile = new File(filename);
			if (!inputFile.exists()) {
				throw new RuntimeException("Cannot find input file: " + filename);
			}
			fileList.add(inputFile);
			index++;
		}

		if (fileList.size() == 0) {
			throw new RuntimeException("Missing filename argument on command line");
		}

		return fileList;
    }

    /**
     * Cleanup thread resources on app shutdown
     */
    @PreDestroy
    public void shutdown() {
        if (logHandlerThread != null) {
            logHandlerThread.interrupt();
        }
    }
}

