package core;

import net.BrowserResponseHandler;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;

class BrowserResponseHandlerTests {

    private final BrowserResponseHandler handler = new BrowserResponseHandler();

    /**
     * Used to test that the response handler returns the response body when a 200 code
     * is returned in the response
     *
     * @throws IOException   If the autocloseable fails to close
     * @throws HttpException If the handler fails to handle the response
     */
    @Test
    void testBrowserHandler200() throws IOException, HttpException {
        try (HttpEntity entity = mock(HttpEntity.class)) {
            String content = "success";
            InputStream contentStream = new ByteArrayInputStream((content).getBytes(StandardCharsets.UTF_8));

            try (ClassicHttpResponse res = mock(ClassicHttpResponse.class)) {
                when(res.getCode()).thenReturn(200);
                when(res.getEntity()).thenReturn(entity);
                when(entity.getContent()).thenReturn(contentStream);

                try (MockedStatic<EntityUtils> utils = mockStatic(EntityUtils.class)) {
                    utils.when(() -> EntityUtils.toString(entity)).thenReturn(content);
                    Assertions.assertEquals(content, handler.handleResponse(res));
                }
            }
        }
    }

    /**
     * Asserts that the response handler throws an HttpException when a 500 code is returned
     * @throws IOException if the HTTPResponse auto closeable cannot close
     */
    @Test
    void testBrowserHandler500() throws IOException {
        try (ClassicHttpResponse res = mock(ClassicHttpResponse.class)) {
            when(res.getCode()).thenReturn(500);
            Assertions.assertThrows(HttpException.class, () -> handler.handleResponse(res));
        }
    }

    /**
     * Asserts that the response handler throws an HttpException when a 201 code is returned
     * @throws IOException if the HTTPResponse auto closeable cannot close
     */
    @Test
    void testBrowserHandler201() throws IOException {
        try (ClassicHttpResponse res = mock(ClassicHttpResponse.class)) {
            when(res.getCode()).thenReturn(201);
            Assertions.assertThrows(HttpException.class, () -> handler.handleResponse(res));
        }
    }

    /**
     * Asserts that the response handler throws an HttpException when a 300 code is returned
     * @throws IOException if the HTTPResponse auto closeable cannot close
     */
    @Test
    void testBrowserHandler199() throws IOException {
        try (ClassicHttpResponse res = mock(ClassicHttpResponse.class)) {
            when(res.getCode()).thenReturn(300);
            Assertions.assertThrows(HttpException.class, () -> handler.handleResponse(res));
        }
    }
}
