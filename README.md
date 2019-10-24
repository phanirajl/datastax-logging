# datastax-logging
DataStax coding exercise for logging relay by Charles Ingham

## Overview
The system will consists of two components - a log forwarding Agent and a log aggregation Service.

#### The Agent
The Agent is responsible for tailing a given log file and delivering its contents to the remote Service in a timely manner.


#### The Service
The Service is responsible for accepting chunked log contents from the client Agent(s) and stitching each one together, creating a copy of the original logs. The contents of the logs reconstructed on the server preserve the original order of entries (lines).

## Architecture

	 
![Architecture Diagram](https://raw.githubusercontent.com/cingham1/aws-lambda-ical/master/src/main/resources/ical-architecture-diagram.jpg)

#### REST API

The Agent will send periodic log lines to the Service using an API call something like this:
```
HTTP POST:  http://service-domain.com/log-aggregator/{clientId}
```
The body of the POST will contain the filename the client is trying to replicate, and an array of log lines that recently came in.  It will be formatted as JSON, for example:

```
{ "filename" : "test1.log",
  "lines": [ 
        "log line 1..." , 
        "log line 2...",
        "log line 3..."
  ] 
}
```
**Success response:**
* Status: HTTP 200 OK
* Headers: 
    * Content-Type: application/json
* Body: `{ "status" : 200, "message" : "Success" }`

**Error response:**
* Status: HTTP 400 Bad Request, or 500 Internal Error
* Headers: 
    * Content-Type: application/json
* Body: `{ "status":<status code>, "message":<error description> }`

#### Service File Storage
The REST API allows for a clientId (unique to the Agent) and the filename to be passed in each request.  The filepath location where the file should be stored on the local file system is defined in the Service configuration.  The resulting created file will have the naming convention:
`{filePath}/{clientId}-{filename}`
For example: ./logfile-storage/client56-output.log
This naming convention allows for multiple files from the same client, and also prevents possible duplicate filenames from different clients.

#### Fault Tolerance
The Agent attempts to be fault tolerant by keeping two internal line lists for each file being processed.
* collectionList - gathers lines as they are read in asynchronously from the log file
* uploadList - the batch of lines currently in the process of being uploaded

The lists are swapped and manipulated before and after each upload which allows for 
* data consistency, even when the upload fails.
* log lines can still be read in asynchronously while an upload is occurring

See [LogCollector.java](https://github.com/cingham1/datastax-logging/blob/master/agent/src/main/java/com/datastax/log/agent/service/LogCollector.java)


## Configuration
#### Agent
The Agent will take one or more names of existing files on the command line to begin tailing them.  In addition, the following options are available in application.yml:

`log-agent.clientId: <clientId>`
Unique client id sent to the service host to distinguish which client the logs come from

`log-agent.hostUrl: <hostUrl>`
URL of the Service host where the log file should be recreated.  The client id will be added when the request is made.  
For example: http://service-domain.com/log-aggregator/

`log-agent.delayBetweenUploads: <delay in seconds>`
We don't want each incoming log line to trigger individual API calls so we will batch them and attempt an upload every few seconds.


#### Service
The Service has the following options available in application.yml:

`log-service.filePath: <local pathname>`
The location on the local file system where the log files will be written to. 
For example: ./output-files/

  
  
## Build

This project is built with Java 8 using Maven 3.

#### Running the tests

Spock unit tests may be run from an IDE or from the command line using the following:

```
mvn test
```

#### Building the packages
Use Maven to build, run tests, and package 

```
mvn clean package shade:shade
```

#### Deployment
Normally the application would be deployed to a staging area or Dev server, but for the purposes of this programming exercise no special deployment is implemented.

#### Running the Agent

After a build the .jar file for the Agent can be found under "./agent/target".  It can be run with the following:
```
java -jar <.jar file> <filename1> <filename2> ...
```
The application takes one or more filenames on the command line and will tail them as log file lines and periodically upload lines to the Service.


#### Service

After a build the .jar files for the Service can be found under "./agent/service".  It can be run with the following:
```
java -jar <jar-file> 
```
The application will listen for incoming requests on its Rest API, and for each request it will attempt to recreate the given file on the local file system.

## Additional Considerations
* How would you change the protocol to allow each agent to aggregate multiple log files concurrently? (From the interview task description)

  This is implemented in the code - multiple files are ingested concurrently.  The uploads however are currently done sequentially, and could be implemented to upload as separate threads.
  
* How would you design the system to allow aggregation from hundreds of thousands of agents? (From the interview task description)

  Scaling instances of both the Agent and Service onto multiple servers should not be too difficult, however: 
  1. The unique clientId used in this implementation would need to be created/registered somehow for each Agent instance.
  2. Multiple instances of the Service with a Load Balancer would allow for backend scalability, with maybe a shared file system or S3 to store the new log files.

* A known issue exists with the current implementation: starting the Agent on an existing multi-megabyte file would be problematic.  It currently will attempt to read the complete file and upload it in one push, ideally it would process the existing file in chunks.  


## Versioning

* 1.0.0 - Initial version

## Authors

* **Charles Ingham** 

## References 

* [Apache Tailer Library](https://commons.apache.org/proper/commons-io/javadocs/api-2.4/org/apache/commons/io/input/Tailer.html) - file input tracking

#### Built With

* [Eclipse](https://www.eclipse.org/) - IDE
* [Maven](https://maven.apache.org/) - Dependency Management


## License

This project is licensed under the Apache License V2.0 - see the [LICENSE](LICENSE) file for details


