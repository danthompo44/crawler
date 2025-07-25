import io.github.resilience4j.retry.Retry;
import net.WebBrowser;
import net.WebBrowserException;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpRetryException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class WebBrowserTests {

    private static WebBrowser browser;
    private static CloseableHttpClient clientMock;
    private static final Retry retry = Retry.ofDefaults("webBrowserRetry");
    private final static URI uri = URI.create("http://test.com");

    @BeforeAll
    public static void setup(){
        clientMock = mock(CloseableHttpClient.class);
        browser = new WebBrowser(clientMock, retry);
    }

    /**
     * Tests to see if the body of the response is returned correctly when a 200 code is returned
     */
    @Test
    public void testWebBrowser200() throws IOException, WebBrowserException, ParseException {
        @SuppressWarnings("unchecked")
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        HttpEntity entity = mock(HttpEntity.class);
        String content = "success";
        InputStream contentStream = new ByteArrayInputStream((content).getBytes(StandardCharsets.UTF_8)); // Must mock getContent with InputStream

        when(response.getCode()).thenReturn(200);
        when(response.getEntity()).thenReturn(entity);
        when(entity.getContent()).thenReturn(contentStream);

        try (MockedStatic<EntityUtils> entityUtils = mockStatic(EntityUtils.class)){
            entityUtils.when(() -> EntityUtils.toString(entity)).thenReturn(content);
        }

        when(clientMock.execute(
                any(ClassicHttpRequest.class),
                any(HttpClientResponseHandler.class)))
                .thenAnswer(invocation -> {
            var handler = invocation.getArgument(1, HttpClientResponseHandler.class);
            return handler.handleResponse(response);
        });

        String body = browser.get(uri);
        assert body.equals(content);
    }

    /**
     * Tests to see if a HttpException is thrown when a 201 code is returned
     */
    @Test
    public void testWebBrowser201() throws IOException {
        try (CloseableHttpResponse response = mock(CloseableHttpResponse.class)) {
            HttpEntity entity = mock(HttpEntity.class);
            String content = "success";
            InputStream contentStream = new ByteArrayInputStream((content).getBytes(StandardCharsets.UTF_8)); // Must mock getContent with InputStream

            when(response.getCode()).thenReturn(201);
            when(response.getEntity()).thenReturn(entity);
            when(entity.getContent()).thenReturn(contentStream);

            when(clientMock.execute(
                    any(ClassicHttpRequest.class),
                    any(HttpClientResponseHandler.class)))
                    .thenAnswer(invocation -> {
                        var handler = invocation.getArgument(1, HttpClientResponseHandler.class);
                        return handler.handleResponse(response);
                    });
        }

        assertThrows(HttpException.class, () -> browser.get(uri));
    }

    /**
     * Tests to see if a HttpRetryException is thrown when a 500 code is returned
     */
    @Test
    public void testWebBrowser500() throws IOException {
        try (CloseableHttpResponse response = mock(CloseableHttpResponse.class)) {
            HttpEntity entity = mock(HttpEntity.class);
            String content = "fail";
            InputStream contentStream = new ByteArrayInputStream((content).getBytes(StandardCharsets.UTF_8)); // Must mock getContent with InputStream

            when(response.getCode()).thenReturn(500);
            when(response.getEntity()).thenReturn(entity);
            when(entity.getContent()).thenReturn(contentStream);

            when(clientMock.execute(
                    any(ClassicHttpRequest.class),
                    any(HttpClientResponseHandler.class)))
                    .thenAnswer(invocation -> {
                        var handler = invocation.getArgument(1, HttpClientResponseHandler.class);
                        return handler.handleResponse(response);
                    });
        }

        assertThrows(WebBrowserException.class, () -> browser.get(uri));

    }
}
