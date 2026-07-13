package client.util;

import java.net.URI;
import java.util.Arrays;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.exception.InvalidURIException;



// expected uris: (scheme:)//hostname(:port)(path)
public class URIValidator {

    private static final Logger logger = LoggerFactory.getLogger(URIValidator.class);

    private static final URIValidator rootAdressValidator = URIValidator.builder()
            .requireHost()
            .allowedSchemes("http")
            .allowedPaths()
            .build();

    private final boolean requirePort;
    private final boolean requireHost;
    private final String[] allowedSchemes; // if null scheme does not matter, if empty list scheme has to be none (//localhost:...)
    private final String[] allowedPaths;   // if null path does not matter, if empty list path has to be none (//localhost:6070)



    private URIValidator(Builder builder) {
        this.requirePort = builder.requirePort;
        this.requireHost = builder.requireHost;
        this.allowedSchemes = builder.allowedSchemes;
        this.allowedPaths = builder.allowedPaths;
    }



    public void validate(URI uri)
        throws InvalidURIException, NullPointerException {
        
        Objects.requireNonNull(uri);


        logger.debug(
            "Validating: URI = {}; requirePort = {}; requireHost = {}; allowedSchemes = {}; allowedPaths = {}",
            uri, requirePort, requireHost, allowedSchemes, allowedPaths
        );


        if (allowedSchemes != null) {

            String scheme = uri.getScheme();

            try {
                validateStringInList(
                    scheme,
                    allowedSchemes
                );
            }
            catch (IllegalArgumentException iae) {
                throw new InvalidURIException(
                    uri,
                    String.format(
                        "scheme not allowed: scheme = %s; allowed schemes = %s",
                        scheme, Arrays.toString(allowedSchemes)
                    ),
                    iae
                );
            }

            logger.trace("Scheme accepted.");
        }

        if (allowedPaths != null) {

            String path = uri.getPath();

            try {
                validateStringInList(
                    path,
                    allowedPaths
                );
            }
            catch (IllegalArgumentException iae) {
                throw new InvalidURIException(
                    uri,
                    String.format(
                        "path not allowed: path = %s; allowed paths = %s",
                        path, Arrays.toString(allowedPaths)
                    ),
                    iae
                );
            }

            logger.trace("Path accepted.");
        }

        if (requirePort) {
            int port = uri.getPort();

            if (port < 0 || 65535 < port) {
                throw new InvalidURIException(uri, "Invalid port!");
            }

            logger.trace("Port accepted.");
        }

        if (requireHost) {
            String host = uri.getHost();

            if (!uri.toString().contains("//")) {
                throw new InvalidURIException(uri, "URI missing adress prefix '//'!");
            }

            if (host == null || host.isBlank()) {
                throw new InvalidURIException(uri, "Invalid hostname!");
            }
            
            logger.trace("Hostname accepted.");
        }

        logger.debug("URI accepted.");
    }

    private static void validateStringInList(String str, String[] list)
        throws IllegalArgumentException {

        if (list.length == 0) {

            if (str != null && !str.isEmpty()) {
                throw new IllegalArgumentException();
            }
        }
        else {

            boolean match = Arrays.stream(list)
                .anyMatch(s -> s.equalsIgnoreCase(str));

            if (!match) {
                throw new IllegalArgumentException();
            }
        } 
    }



    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        boolean requirePort = false;
        boolean requireHost = false;
        String[] allowedSchemes = null;
        String[] allowedPaths = null;

        private Builder() {}

        public Builder requirePort() {
            requirePort = true;
            return this;
        }

        public Builder requireHost() {
            requireHost = true;
            return this;
        }

        public Builder allowedSchemes(String... schemes) {
            if (Objects.isNull(schemes)) { return this; }
            allowedSchemes = schemes.clone();
            return this;
        }

        public Builder allowedPaths(String... paths) {
            if (Objects.isNull(paths)) { return this; }
            allowedPaths = paths.clone();
            return this;
        }

        public URIValidator build() {
            return new URIValidator(this);
        }
    }



    public static void validatePath(URI path)
        throws InvalidURIException {
        
        if (path.getAuthority() != null) {
            throw new InvalidURIException(path, "URI has to be a path!");
        }
    }

    public static void validateRoot(URI uri)
        throws InvalidURIException {

        rootAdressValidator.validate(uri);
    }
}
