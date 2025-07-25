package core;

import exceptions.VisitedURIException;
import net.URIQueue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.mockito.Mockito.spy;

class URIQueueTests {
    private static final URI firstUri = URI.create("https://test.com");

    /**
     * Tests that when a URI is added to the URI queue that it is reflected within
     * the encapsulated blocking queue
     */
    @Test
    void testAdd() throws VisitedURIException {
        BlockingQueue<URI> blockingQueue = spy(new LinkedBlockingQueue<>());
        URIQueue uriQueue = new URIQueue(blockingQueue);

        uriQueue.add(firstUri);

        Assertions.assertEquals(1, blockingQueue.size());
    }

    /**
     * Tests that when a duplicated URI is added to the queue that a VisitedURIException
     * is thrown
     */
    @Test
    void testAddDuplicate() throws VisitedURIException {
        BlockingQueue<URI> blockingQueue = spy(new LinkedBlockingQueue<>());
        URIQueue uriQueue = new URIQueue(blockingQueue);

        uriQueue.add(firstUri);

        Assertions.assertEquals(1, blockingQueue.size());
        Assertions.assertThrows(VisitedURIException.class, () -> uriQueue.add(firstUri));
    }

    /**
     * Tests that the poll method works as expected. Allowing a client to add items to the queue,
     * and retrieve them on a first come first served basis
     */
    @Test
    void testPoll() throws VisitedURIException {
        BlockingQueue<URI> blockingQueue = spy(new LinkedBlockingQueue<>());
        URIQueue uriQueue = new URIQueue(blockingQueue);
        URI secondUri = URI.create("https://test2.com");

        uriQueue.add(firstUri);
        uriQueue.add(secondUri);
        Assertions.assertEquals(2, blockingQueue.size());
        Assertions.assertEquals(firstUri, uriQueue.poll());
        Assertions.assertEquals(1, blockingQueue.size());
        Assertions.assertEquals(secondUri, uriQueue.poll());
        Assertions.assertEquals(0, blockingQueue.size());
        Assertions.assertNull(uriQueue.poll());
        Assertions.assertEquals(0, blockingQueue.size());
    }
}
