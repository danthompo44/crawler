package org.monzo.crawler.net;

import org.monzo.crawler.exceptions.WebBrowserException;

import java.net.URI;

public interface Browser {
    String get(URI uri) throws WebBrowserException;
}
