package com.datastax.log.agent.service;

import com.datastax.log.agent.config.Config;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class to collect and upload log file lines that are tailed by this agent.
 * It contains two lists:
 *    collectionList - gathers lines as they are read in asynchronously from the log file
 *    uploadList - the batch of lines currently in the process of being uploaded
 * 
 * It includes a worker thread to periodically attempt uploading log data to server.
 * 
 * @author cingham
 */
@Service
public class LogHandler implements Runnable {
	
	List<String> collectionList = new ArrayList<>();
	List<String> uploadList = new ArrayList<>();

	// this lock ensures the actions of adding new line elements to the list
	// and manipulating lists just before upload do not interfere with each other
	ReentrantLock collectionListLock = new ReentrantLock();
	
	LogUploader uploader;
	long delayBetweenUploads;

	/**
	 * Injection constructor
	 *
	 * @param uploader class that handles uploading collected log lines to service host
	 * @param config
	 */
	protected LogHandler(LogUploader uploader, Config config) {
		this.uploader = uploader;
		this.delayBetweenUploads = config.getDelayBetweenUploads()*1000;  // seconds to millis
	}

	/**
	 * Called by the LogCollector as new lines are read in from the file
	 *
	 * @param line
	 */
	public void collectLine(String line) {
		collectionListLock.lock();	// temporarily block any upload list manipulation
	    try {
	    	collectionList.add(line);
	    } finally {
	    	collectionListLock.unlock();
	    }
	}
	
	/**
	 * Worker thread which periodically attempts to upload current batch of lines
	 */
	public void run() {
		try {
			while (!Thread.interrupted()) {
				Thread.sleep(delayBetweenUploads);
				processUpload();
			}
		} catch(InterruptedException ie) {
			// interrupted during sleep(), just exit thread
		}
	}
	
	private void processUpload() {
		if (collectionList.size() == 0) {
			// no new lines added, nothing to do
			return;
		}

		// ensure uploadList contains the latest batch of lines,
		// (collectionList becomes uploadList, empty uploadList becomes new collectionList)
		lockAndSwapLists();

		// do the upload
		boolean success = uploader.uploadToServer(uploadList);
		
		if (success) {
			uploadList.clear();
		} else {
			// problem during upload:
			// 1) concatenate any new lines that just came in onto upload list thus keeping the ordering
			// 2) uploadList then becomes collectionList to continue collecting new lines as usual
			// 3) later we'll try again during the next upload cycle (in a few seconds)
			lockAndConcatenateLists();
		}
	}

	/**
	 * Swap collectionList and uploadList, while locking to prevent contention issues
	 */
	private void lockAndSwapLists() {
		collectionListLock.lock();		// temporarily block new lines from being added
	    try {
	    	doListSwap();
	    } finally {
	    	collectionListLock.unlock();
	    }		
	}

	/**
	 * Concatenate any new lines that just came in onto upload list thus keeping the ordering,
	 * uploadList then becomes collectionList to continue collecting new lines as usual
	 */
	private void lockAndConcatenateLists() {
		collectionListLock.lock();		// temporarily block new lines from being added
	    try {
	    	// add any new lines to uploadList, keeping the ordering
	    	uploadList.addAll(collectionList);
	    	collectionList.clear();

	    	// restore the uploadList back to collectionList
	    	doListSwap();
	    } finally {
	    	collectionListLock.unlock();
	    }		
	}
	
	private void doListSwap() {
    	List<String> temp = uploadList;
    	uploadList = collectionList;
    	collectionList = temp;
	}
}
