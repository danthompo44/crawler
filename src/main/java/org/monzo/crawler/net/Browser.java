package org.monzo.crawler.net;

import java.net.URI;

public interface Browser {
    String get(URI uri) throws WebBrowserException;
}
