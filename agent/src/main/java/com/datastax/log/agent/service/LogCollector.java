package com.datastax.log.agent.service;

import org.apache.commons.io.input.TailerListenerAdapter;
import org.springframework.stereotype.Service;

/**
 * Class to collect log lines from the input file.
 * Uses TailerListener interface from org.apache.commons.io.input.Tailer
 * to read in (tail) lines of a file as they become available.
 *
 * See: https://commons.apache.org/proper/commons-io/javadocs/api-2.4/org/apache/commons/io/input/Tailer.html
 *
 * @author cingham
 */
@Service
public class LogCollector extends TailerListenerAdapter {

    private LogHandler logHandler;

    /**
     * Injection constructor
     *
     * @param logHandler the class that holds and manages the collected log lines
     */
    public LogCollector(LogHandler logHandler) {
        this.logHandler = logHandler;
    }

    /**
     * Called by the Tailer thread as each new line becomes available
     *
     * @param line the new line of text from the log file
     */
    public void handle(String line) {
        logHandler.collectLine(line);
    }
}
