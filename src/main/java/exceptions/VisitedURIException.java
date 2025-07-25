package exceptions;

/**
 * An exception thrown when a URI is visited more than once
 */
public class VisitedURIException extends Exception {
    public VisitedURIException(String message){
        super(message);
    }
}
