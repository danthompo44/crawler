package org.monzo.crawler;

import org.monzo.crawler.exceptions.VisitedURIException;
import org.monzo.crawler.exceptions.WebCrawlException;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.monzo.crawler.net.BrowserResponseHandler;
import org.monzo.crawler.net.URIQueue;
import org.monzo.crawler.net.WebBrowser;
import org.monzo.crawler.net.WebWorker;
import org.apache.commons.cli.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class App {
    private static final Logger logger = LogManager.getLogger(App.class);
    private static final String URI_CLI_LONG = "url";
    private static final CloseableHttpClient client = HttpClients.createDefault();
    private static final URIQueue uriQueue = new URIQueue(new LinkedBlockingQueue<>());
    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(11, 11, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.AbortPolicy());

    /**
     * Parses CLI arguments and retrieves the given URL
     * @param args The CLI args passed to the java program
     * @return The URI given by the user in the CLI
     */
    private static URI getUri(String[] args) {
        Options options = new Options();
        Option input = new Option("u", URI_CLI_LONG, true, "starting url");
        input.setRequired(true);
        options.addOption(input);

        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);
            String url = cmd.getOptionValue(URI_CLI_LONG);
            logger.info("CRAWL REQUESTED, BASE URL: {}", url);
            URI uri = URI.create(url);
            return cleanseUri(uri);
        }
        catch(ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(App.class.getName(), options);
            throw new WebCrawlException(e);
        }
    }

    /**
     * Checks to see whether the URI has a scheme, if it does not it adds HTTPS
     * @param uri The URI to be checked and cleansed
     * @return A cleansed URI
     */
    private static URI cleanseUri(URI uri) {
        if (uri.getScheme() == null) {
            return URI.create("https://" + uri);
        }
        return uri;
    }

    /**
     * Creates the Retry config to be passed in to the WebBrowser.
     * The retry will be wrapped around http requests by the browser to handle failures
     * @return The applications retry config
     */
    private static Retry getRetryConfig() {
        RetryConfig config = RetryConfig.<String>custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(2))
                // Retry if an IO exception or 500 error
                .retryOnException(e -> {
                    if (e instanceof RuntimeException) {
                        return true;
                    }
                    if (e instanceof HttpException) {
                        return (e).getMessage().contains("500");
                    }
                    return false;
                })
                .build();

        return Retry.of("webBrowserRetry", config);
    }

    /**
     * Creates and starts a daemon thread, this thread monitors the uriQueue
     * Once a URI is retrieved from the queue, the daemon passes a WebWorker to
     * the thread pool executor. The thread pool executor handles executing
     * the tasks in threads.
     */
    private static void startURIWorker(){
        final WebBrowser browser = new WebBrowser(client, new BrowserResponseHandler(), getRetryConfig());

        Thread thread = new Thread(() -> {
            while(true){
                URI uri = uriQueue.poll();

                if(uri != null){
                    logger.debug("URI: {}, found in queue", uri);
                    executor.execute(new WebWorker(browser, uri, uriQueue));
                }
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Retrieves the URI given by the user and adds it to the URI Queue
     * Starts the URI worker then monitors the executor until no more tasks remain
     * @param args CLI args given by the user executing this program
     */
    public static void main(String[] args){
        LocalDateTime start = LocalDateTime.now();
        startURIWorker();
        URI uri = getUri(args);

        try {
            uriQueue.add(uri);
            logger.info("URI added {}", uri);
        } catch (VisitedURIException e) {
            throw new WebCrawlException(e);
        }

        try {
            // Give the queue time to populate before we check the executor queue/active count
            Thread.sleep(2000);
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }

        while(!executor.getQueue().isEmpty() || executor.getActiveCount() > 0){
            logger.debug("Executor: {} active tasks", executor.getActiveCount());

            try {
                Thread.sleep(3000);
            } catch (InterruptedException _) {
                Thread.currentThread().interrupt();
            }
        }

        LocalDateTime end = LocalDateTime.now();
        logger.info("CRAWl COMPLETE. URL Count {}. Total Time {}", uriQueue.totalVisitedUris(), Duration.between(start, end));
        executor.shutdown();
        try {
            client.close();
        } catch (IOException e) {
            throw new WebCrawlException(e);
        }
    }
}
