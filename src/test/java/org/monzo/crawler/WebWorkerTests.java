package org.monzo.crawler;

import org.monzo.crawler.exceptions.VisitedURIException;
import org.monzo.crawler.net.Browser;
import org.monzo.crawler.net.URIQueue;
import org.monzo.crawler.net.WebBrowserException;
import org.monzo.crawler.net.WebWorker;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.mockito.Mockito.*;

class WebWorkerTests {
    private static Browser browser;
    private static final URI uri = URI.create("https://test.com");
    private static URIQueue queue;
    private static final URI expected = URI.create("https://test.com/internal-link");
    private static final URI expected2 = URI.create("https://test.com/internal-links");
    private static final String BODY = """
            <!DOCTYPE html>
            <html>
            <body>
            
            <h2>HTML Links</h2>
            <p>HTML links are defined with the a tag:</p>
            
            <a href="https://www.w3schools.com">This is a link</a>
            <a href="/internal-link">This is a link</a>
            <a href="/internal-links">This is a link</a>
            
            </body>
            </html>""";

    @BeforeAll
    static void setup(){
        browser = mock(Browser.class);

        queue = mock(URIQueue.class);
    }

    /**
     * Tests that when internal links are found within a returned webpage that
     * these are extracted correctly and then added to the queue.
     */
    @Test
    void testAddToQueue() throws WebBrowserException, VisitedURIException {
        WebWorker worker = new WebWorker(browser, uri, queue);
        when(browser.get(uri)).thenReturn(BODY);
        worker.run();
        verify(queue, times(1)).add(expected);
        verify(queue, times(1)).add(expected2);
    }
}
