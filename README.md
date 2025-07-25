# Web Crawler Application

A Java-based web crawler that systematically browses and maps web pages starting from a given URL. The application uses
a multi-threaded approach to efficiently crawl websites while handling rate limiting and retries for failed requests.

## Features

- Multi-threaded crawling with thread pool
- Automatic retry mechanism for failed requests
- Duplicate URL detection
- Console logging of crawler progress
- Final crawl statistics including total URLs and execution time

## Prerequisites

- Java 22 or higher
- Maven 3.6 or higher

## Building the Application

To build the application, run:
mvn exec:java -Dexec.mainClass=org.monzo.crawler.App "-Dexec.args=-u monzo.com"
