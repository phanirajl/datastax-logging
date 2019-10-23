package com.datastax.log.service.service

import com.datastax.log.service.config.Config
import spock.lang.Specification
import spock.lang.Subject

class LogAppenderSpec extends Specification {
	String UNIT_TEST_PATH = "__temp-unit-test__"
	String TEST_CLIENT_ID = "client-1"
	String TEST_FILENAME = "test.log"

    @Subject
	LogAppender logAppender
	Config config
    List<String> testLines
	File testFile

    def "setup"() {
		testLines = Arrays.asList("test-1", "test-2")
        config = Mock()
		config.getFilePath() >> UNIT_TEST_PATH
		logAppender = new LogAppender(config)
		testFile = new File(UNIT_TEST_PATH, TEST_CLIENT_ID + "-" + TEST_FILENAME)
    }

	def "cleanup"() {
		testFile.delete()
		new File(UNIT_TEST_PATH).delete()
	}

    def "test that file is created with the expected contents"() {
        when:
            logAppender.appendToFile(TEST_CLIENT_ID, TEST_FILENAME, testLines)
        then:
			testFile.exists()
			checkExpectedFileContents(testFile) == true
    }

	def "test that file error generates Exception"() {
		given:
			String invalidFilename = "*<>=,?|\""	// invalid filename chars
		when:
			logAppender.appendToFile(TEST_CLIENT_ID, invalidFilename)
		then:
			thrown Exception
	}

	private boolean checkExpectedFileContents(File theFile) {
		BufferedReader br = new BufferedReader(new FileReader(theFile))
		ArrayList<String> input = new ArrayList<String>()
		String str
		while ((str = br.readLine()) != null) {
			input.add(str)
		}
		br.close()
		return input.equals(testLines)
	}
}
