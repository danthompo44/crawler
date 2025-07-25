package net;

import java.io.IOException;
import java.net.URI;

public interface Browser {
    String get(URI uri) throws WebBrowserException;
}
