package net;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * A web browser implementation used to send HTTP requests to URLs and retrieve
 * the web pages HTML
 */
public class WebBrowser implements Browser {
    private final HttpClient client;
    private final Logger logger = LogManager.getLogger();

    public WebBrowser(HttpClient client){
        this.client = client;
    }

    /**
     * Sends an HTTP GET request to the given URL.
     * If the returned status code is 200, the response body shall be returned.
     * If any other status code is returned, then a WebBrowserException will be thrown
     * @param uri The URI to scrape
     * @return The pages HTML if the request was successful
     * @throws WebBrowserException If the response code is not 200 of an IOException/InterruptedException
     * occurs
     */
    public String get(URI uri) throws WebBrowserException {
        HttpRequest req = HttpRequest.newBuilder().GET().
                uri(uri).build();

        try {
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            logger.info("HTTP {} {} {}", res.request().method(), res.statusCode(), res.uri());

            if (res.statusCode() == 200) {
                return res.body();
            }
            else {
                throw new WebBrowserException("Only 200 is an acceptable return code");
            }
        } catch (IOException | InterruptedException e) {
            throw new WebBrowserException(e);
        }


    }
}
