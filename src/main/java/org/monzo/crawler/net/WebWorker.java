package org.monzo.crawler.net;

import org.monzo.crawler.exceptions.VisitedURIException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.monzo.crawler.exceptions.WebBrowserFailure;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An implementation of the Runnable interface. Handles sending an HTTP request to
 * the given URI. Parsing the response using Jsoup and extracting all links from the
 * web page
 */
public class WebWorker implements Runnable {
    private final Browser browser;
    private final URI uri;
    private final URIQueue queue;
    private static final Logger logger = LogManager.getLogger(WebWorker.class);

    /**
     * Constructs a WebWorker instance.
     *
     * @param browser The Browser instance used to send HTTP requests and retrieve the HTML content of the URI.
     * @param uri The URI to handle, specifically for sending the HTTP request and parsing the response.
     * @param queue The URIQueue instance where extracted URIs from the response are added.
     */
    public WebWorker(Browser browser, URI uri, URIQueue queue){
        this.browser = browser;
        this.uri = uri;
        this.queue = queue;
    }

    /**
     * Added the given URL link to the queue after converting it to a URL object.
     * @param link The URL link to be added to the queue.
     */
    private void addToQueue(String link){
        // Localised match
        URI nextUri = null;
        try {
            nextUri = URI.create(uri.getScheme() + "://" + uri.getHost() + link);
            queue.add(nextUri);
            logger.debug("URI identified as internal, marked for crawl: {}", nextUri);
        } catch (VisitedURIException | IllegalArgumentException _) {
            logger.debug("Skipping previously visited URI: {}", nextUri);
        }
    }

    /**
     * Concrete implementation of the Runnable interface. Sends HTTP GET to the given URL.
     * Extracts links from the return webpage and filters for any internal links.
     * Any found will be added to the queue.
     */
    @Override
    public void run() {
        try {
            String body = browser.get(uri);
            Document doc = Jsoup.parse(body);
            Elements links = doc.select("a[href]");
            logger.info("URI: {} - Links: {}", uri, links.stream().map(e -> e.attr("href")).toList());

            for (Element el: links){
                String link = el.attr("href");
                logger.debug("Current URI: {}, Link: {}", uri, link);

                Pattern pattern = Pattern.compile("^/[a-zA-Z]");
                Matcher matcher = pattern.matcher(link);

                if (matcher.find()){
                    addToQueue(link);
                }
            }
            logger.debug("Located all {} links at URI: {}", links.size(), uri);
        } catch (WebBrowserFailure | WebBrowserException e) {
            logger.warn("Unable to crawl URL {}. Max retries encountered. Error: {}", uri, e.getMessage());
        }
    }
}
