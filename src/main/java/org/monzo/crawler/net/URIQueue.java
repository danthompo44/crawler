package org.monzo.crawler.net;

import org.monzo.crawler.exceptions.VisitedURIException;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * A wrapper class for a blocking queue, exposes two methods only.
 * Add and poll, for attempting to add a URI to the queue, and polling the queue
 */
public class URIQueue {
    private final List<URI> visitedUris = new ArrayList<>(); // TRADE OFF - Memory overload here if URL count grows
    
    private final BlockingQueue<URI> queue;
    
    public URIQueue(BlockingQueue<URI> queue){
        this.queue = queue;
    }

    public synchronized void add(URI uri) throws VisitedURIException {
        if(visitedUris.contains(uri)){
            throw new VisitedURIException("URI: {} already visited");
        }

        queue.add(uri);
        visitedUris.add(uri);
    }

    public synchronized URI poll(){
        // Decrement the pending tasks
        return queue.poll();
    }

    public synchronized int totalVisitedUris() {
        return visitedUris.size();
    }
}
