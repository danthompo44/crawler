package net;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WebBrowser implements Browser{
    private final HttpClient client;
    private final Logger logger = LogManager.getLogger();

    public WebBrowser(HttpClient client){
        this.client = client;
    }

    public String get(URI uri) {
        HttpRequest req = HttpRequest.newBuilder().GET().uri(uri).build();

        try {
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            logger.info("HTTP {} {} {}", res.request().method(), res.statusCode(), res.uri());
            return res.body();

        }
        catch(IOException | InterruptedException e){
            throw new RuntimeException(e);
        }
    }
}
