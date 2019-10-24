package com.datastax.log.agent.service;

import lombok.Getter;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class to collect and manage log file lines that are tailed for a given file.
 * It contains two lists:
 *    collectionList - gathers lines as they are read in asynchronously from the log file
 *    uploadList - the batch of lines currently in the process of being uploaded
 *
 * When an upload is about to start (beforeUpload() is called) the lists are swapped.  This allows
 * lines to continue to be pulled in on the collectionList (the upload may take a few seconds),
 * but we have a fixed snapshot in uploadList for the upload.
 * After the upload attempt (afterUpload(boolean) is called), on success we simply clear the uploadList.
 * On failure we add any new lines onto uploadList to keep the ordering, and swap the lists again.
 *
 * Uses TailerListener interface from org.apache.commons.io.input.Tailer
 * to read in (tail) lines of a file as they become available.
 * See: https://commons.apache.org/proper/commons-io/javadocs/api-2.4/org/apache/commons/io/input/Tailer.html
 *
 * @author cingham
 */
public class LogCollector extends TailerListenerAdapter {
	@Getter
	private final File file;
	private final Tailer tailer;

	private List<String> collectionList = new ArrayList<>();
	private List<String> uploadList = new ArrayList<>();

	// this lock ensures the actions of adding new line elements to the collectionList
	// and manipulating lists just before upload do not interfere with each other
	private final ReentrantLock collectionListLock = new ReentrantLock();

	public LogCollector(File file) {
		this.file = file;

		// start thread to continuously read in log file lines using the apache Tailer interface.  See:
		// https://commons.apache.org/proper/commons-io/javadocs/api-2.4/org/apache/commons/io/input/Tailer.html
		tailer = Tailer.create(file, this);
	}

	/**
	 * Called by the Tailer thread as each new line becomes available
	 *
	 * @param line the new line of text from the log file
	 */
	public void handle(String line) {
		collectionListLock.lock();	// temporarily block any upload list manipulation
		try {
			collectionList.add(line);
		} finally {
			collectionListLock.unlock();
		}
	}

	/**
	 * Determine whether lines have come in that need to be uploaded
	 *
	 * @return true means there are lines available
	 */
	protected boolean hasLinesToUpload() {
		return (collectionList.size() > 0);
	}

	/**
	 * Manage internal lists to prepare for an upload attempt
	 * @return current uploadList
	 */
	protected List<String> beforeUpload() {
		// ensure uploadList contains the latest batch of lines,
		// (collectionList becomes uploadList, empty uploadList becomes new collectionList)
		lockAndSwapLists();

		// give caller items ready to upload
		return uploadList;
	}

	/**
	 * Update internal lists just after an upload attempt
	 * @param success
	 */
	protected void afterUpload(boolean success) {
		if (success) {
			// upload attempt was successful, simply clear the upload list
			uploadList.clear();
		} else {
			// problem during upload, but we don't want to lose the lines we tried to send:
			//  1) concatenate any new lines that just came in onto upload list thus keeping the ordering
			//	2) uploadList then becomes collectionList to continue collecting new lines as usual
	 		// 	3) later we'll try again during the next upload cycle (in a few seconds)
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

	/**
	 * Cleanup thread resources on app shutdown
	 */
	public void shutdown() {
		if (tailer != null) {
			tailer.stop();
		}
	}
}
