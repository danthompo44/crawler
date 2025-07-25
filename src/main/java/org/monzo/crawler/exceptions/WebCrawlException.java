package org.monzo.crawler.exceptions;

/**
 * An exception that extends runTime exception, and should be used to indicate a
 * critical failure in the system
 */
public class WebCrawlException extends RuntimeException {
    public WebCrawlException(Throwable err){
        super(err);
    }
}
