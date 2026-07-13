package client.exception;

import java.net.URI;

/**
 * Thrown when a URI is invalid.
 */
public class InvalidURIException extends IllegalArgumentException {

    private final URI uri;
    private final String reason;

    /**
     * Creates a new InvalidURIException for the given URI and reason.
     *
     * @param uri    The invalid URI
     * @param reason The reason why the URI is invalid
     */
    public InvalidURIException(URI uri, String reason) {
        this.uri = uri;
        this.reason = reason;
    }

    /**
     * Creates a new InvalidURIException for the given URI, reason and cause.
     *
     * @param uri    The invalid URI
     * @param reason The reason why the URI is invalid
     * @param cause  The underlying cause of this exception
     */
    public InvalidURIException(URI uri, String reason, Throwable cause) {

        super(reason, cause);
        this.uri = uri;
        this.reason = reason;
    }

    @Override
    public String toString() {
        return String.format(
                "Exception = %s; URI = %s; reason = %s",
                this.getClass().getName(), uri, reason
        );
    }

    /**
     * @return The invalid URI
     */
    public URI uri() { return uri; }

    /**
     * @return The reason why the URI is invalid
     */
    public String reason() { return reason; }
}