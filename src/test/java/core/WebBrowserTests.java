package core;

import exceptions.WebBrowserFailure;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import net.Browser;
import net.WebBrowser;
import net.WebBrowserException;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WebBrowserTests {
    private static CloseableHttpClient clientMock;
    private static final URI uri = URI.create("http://test.com");
    private static final int MAX_RETRIES = 3;

    /**
     * Creates a Retry configuration for testing purposes with specific retry logic.
     * The Retry configuration will attempt retries up to three times with a wait
     * duration of 2 seconds between retries. It applies retry logic for all thrown
     * exceptions.
     *
     * @return a configured Retry instance with the specified retry logic for use in tests
     */
    private static Retry getTestRetry() {
        RetryConfig config = RetryConfig.<String>custom()
                .maxAttempts(MAX_RETRIES)
                .waitDuration(Duration.ofMillis(100))
                // Retry if an IO exception or 500 error
                .retryOnException(e -> e instanceof IOException || e instanceof WebBrowserFailure)
                .failAfterMaxAttempts(true)
                .build();

        return Retry.of("webBrowserRetry", config);
    }


    @BeforeAll
    static void setup(){
        clientMock = mock(CloseableHttpClient.class);
    }

    /**
     * Tests that when no exception is thrown on the first call to execute we see the response
     * body returned as expected.
     */
    @Test
    void testWebBrowserNoException() throws IOException, WebBrowserException {
        String expectedVal = "";
        HttpClientResponseHandler<String> handler = _ -> expectedVal; // Return an empty string
        when(clientMock.execute(
                any(ClassicHttpRequest.class),
                any(HttpClientResponseHandler.class))).thenReturn(expectedVal);

        Browser browser = new WebBrowser(clientMock, handler, getTestRetry());

        Assertions.assertEquals(expectedVal, browser.get(uri));
    }

    /**
     * Tests that when an exception happens on the first two calls, and the third call
     * to execute succeeds, that we see the body returned as expected
     */
    @Test
    void testWebBrowserSuccessOnFinalRetry() throws IOException, WebBrowserException {
        String expectedVal = "";
        HttpClientResponseHandler<String> handler = _ -> expectedVal; // Return an empty string
        when(clientMock.execute(
                any(ClassicHttpRequest.class),
                any(HttpClientResponseHandler.class)))
                .thenThrow(IOException.class) // First call - throws exception
                .thenThrow(IOException.class) // Second call - throws exception
                .thenReturn(expectedVal); // Third call - returns successfully

        Browser browser = new WebBrowser(clientMock, handler, getTestRetry());

        Assertions.assertEquals(expectedVal, browser.get(uri));
    }

    @Test
    void testWebBrowserFailsWhenMaxRetriesReached() throws IOException {
        String expectedVal = "";
        HttpClientResponseHandler<String> handler = _ -> expectedVal; // Return an empty string
        when(clientMock.execute(
                any(ClassicHttpRequest.class),
                any(HttpClientResponseHandler.class)))
                .thenThrow(IOException.class) // First call - throws exception
                .thenThrow(IOException.class) // Second call - throws exception
                .thenThrow(IOException.class) // Third call - fails with exception
                .thenReturn(expectedVal); // Fourth call - returns successfully

        Browser browser = new WebBrowser(clientMock, handler, getTestRetry());

        Assertions.assertThrows(WebBrowserFailure.class, () -> browser.get(uri));
    }

}
