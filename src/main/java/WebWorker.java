import exceptions.VisitedURIException;
import net.Browser;
import net.WebBrowserException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebWorker implements Runnable{
    private final Browser browser;
    private final URI uri;
    private final URIQueue queue;
    private final static Logger logger = LogManager.getLogger(WebWorker.class);

    public WebWorker(Browser browser, URI uri, URIQueue queue){
        this.browser = browser;
        this.uri = uri;
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            String body = browser.get(uri);
            Document doc = Jsoup.parse(body);
            Elements links = doc.select("a[href]");
            logger.info("URI: {} - {} links", uri, links.size());

            for (Element el: links){
                String link = el.attr("href");
                logger.debug("Current URI: {}, Link: {}", uri, link);

                Pattern pattern = Pattern.compile("^/[a-zA-Z]");
                Matcher matcher = pattern.matcher(link);

                if (matcher.find()){
                    // Localised match
                    URI nextUri = null;
                    try {
                        nextUri = URI.create(uri.getScheme() + "://" + uri.getHost() + link);
                        queue.add(nextUri);
                        logger.info("URI identified as internal, marked for crawl: {}", nextUri);
                    } catch (VisitedURIException | IllegalArgumentException e) {
                        logger.debug("Skipping previously visited URI: {}", nextUri);
                    }
                }
            }
            logger.debug("Located all {} links at URI: {}", links.size(), uri);
        } catch (WebBrowserException e) {
            logger.warn("Unable to crawl URL {}. Max retries encountered. Error: {}", uri, e.getMessage());
        }
    }
}
