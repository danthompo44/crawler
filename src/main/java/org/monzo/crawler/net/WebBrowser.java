package org.monzo.crawler.net;

import org.monzo.crawler.exceptions.WebBrowserFailure;
import io.github.resilience4j.retry.Retry;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Callable;

/**
 * A web browser implementation used to send HTTP requests to URLs and retrieve
 * the web pages HTML
 */
public class WebBrowser implements Browser {
    private final CloseableHttpClient client;
    private final HttpClientResponseHandler<String> handler;
    private final Retry retry;

    /**
     * Creates a WebBrowser instance which can be used to send HTTP requests and retrieve the web pages content
     * if a 200 code is received. Handles retry logic if a >= 500 code is returned.
     * @param client The Closeable HttpClient to be used to send requests
     * @param retry The retry object to wrap the sending of the requests in a retry mechanism
     */
    public WebBrowser(CloseableHttpClient client, HttpClientResponseHandler<String> handler, Retry retry){
        this.client = client;
        this.handler = handler;
        this.retry = retry;
    }

    /**
     * Sends an HTTP GET request to the given URL.
     * If the returned status code is 200, the response body shall be returned.
     * If any other status code is returned, then a WebBrowserException will be thrown
     * @param uri The URI to scrape
     * @return The pages HTML if the request was successful
     * occurs
     */
    public synchronized String get(URI uri) {
        HttpGet req = new HttpGet(uri);

        Callable<String> callable = () -> {
            try {
                return client.execute(req, handler);
            }
            catch(IOException e){
                throw new WebBrowserFailure(e); // Trade off here - logs to console the exception
            }
        };

        try {
            return retry.executeCallable(callable);
        }
        catch(WebBrowserFailure e) {
            throw e;
        }
        catch (Exception e) {
            throw new WebBrowserFailure(e);
        }
    }
}
