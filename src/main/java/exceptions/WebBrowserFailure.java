package exceptions;

/**
 * Represents a runtime exception that indicates a failure in the WebBrowser
 */
public class WebBrowserFailure extends RuntimeException{
    public WebBrowserFailure(Throwable cause) {
        super(cause);
    }
}
