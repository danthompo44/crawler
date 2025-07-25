package org.monzo.crawler.net;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;

/**
 * Implementation of the HttClientResponseHandler that returns a string.
 * Code extracted out into its own class for easier unit testing
 */
public class BrowserResponseHandler implements HttpClientResponseHandler<String> {
    @Override
    public String handleResponse(ClassicHttpResponse response) throws HttpException, IOException {
        if (response.getCode() == 200) {
            return EntityUtils.toString(response.getEntity());
        } else if (response.getCode() >= 500) {
            throw new HttpException("Server error: " + response.getCode());
        }
        else {
            throw new HttpException("Unexpected status code: ", response.getCode());
        }
    }
}
