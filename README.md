# datastax-logging
DataStax coding exercise for logging relay by Charles Ingham

## Overview
...

**Problems:**
...
**Solution:**

...

## Architecture


![Architecture Diagram](https://raw.githubusercontent.com/cingham1/aws-lambda-ical/master/src/main/resources/ical-architecture-diagram.jpg)

#### REST API

```
HTTP GET:  https://ical.cingham.net/hosting-relay/homeaway
```
Example URL for all defined sites collated together

```
HTTP GET:  https://ical.cingham.net/hosting-relay/all
```

**Success response:**
* Status: HTTP 200 OK
* Headers: 
    * Content-Type: text/calendar; charset=utf-8
    * Content-Disposition: inline; filename=hosting.ics
* Body: `<data feed text file in ical format>`

**Error response:**
* Status: HTTP 400 Bad Request, or 500 Internal Error
* Headers: 
    * Content-Type: application/problem+json
* Body: `{ "status":<status code>, "message":<error description> }`

## Build

This project is built with Java 8 using Maven 3.

#### Running the tests

Spock unit/integration tests may be run from an IDE or from the command line using the following:

```
mvn test
```

#### Build
Use Maven to build, run tests, and package 

```
mvn clean package shade:shade
```


#### Deployment to AWS

## Agent

After a build the .jar file found under agent/target can be run.

## Service

The server will collect data ... 



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


