import exceptions.VisitedURIException;
import net.WebBrowser;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class App {
    private static final Logger logger = LogManager.getLogger(App.class);
    private static final String urlCliLong = "url";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final WebBrowser browser = new WebBrowser(client);
    private static final URIQueue uriQueue = new URIQueue(new LinkedBlockingQueue<>());
    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 5, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.AbortPolicy());

    private static URI getUri(String[] args) {
        Options options = new Options();
        Option input = new Option("u", urlCliLong, true, "starting url");
        input.setRequired(true);
        options.addOption(input);

        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);
            String url = cmd.getOptionValue(urlCliLong);
            logger.info("CRAWL REQUESTED, BASE URL: {}", url);
            return URI.create(url);
        }
        catch(ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(App.class.getName(), options);
            throw new RuntimeException(e);
        }
    }

    private static void startUrIWorker(){
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

    public static void main(String[] args){
        URI uri = getUri(args);
        try {
            uriQueue.add(uri);
            logger.info("URI added {}", uri);
        } catch (VisitedURIException e) {
            throw new RuntimeException(e);
        }
        startUrIWorker();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        while(!executor.getQueue().isEmpty() || executor.getActiveCount() > 0){
            logger.debug("Executor: {} active tasks", executor.getActiveCount());

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        logger.info("CRAWl COMPLETE");
    }
}
