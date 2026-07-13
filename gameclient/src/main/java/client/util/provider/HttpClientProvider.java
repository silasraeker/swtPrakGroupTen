package client.util.provider;

import java.net.http.HttpClient;

/**
 * A Singleton class to manage an instance of a {@link HttpClient}.
 * <p>
 * This class should neither be instantiated nor inherited.
 * <p>
 * Usage Example:
 * <pre>{@code
 * HttpRequest request = ...;
 * HttpClient client = HttpClientProvider.client();
 * HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
 * }</pre>
 */
public final class HttpClientProvider {

    /**
     * @throws InstantiationError always; this utility class must not be instantiated
     */
    private HttpClientProvider() { throw new InstantiationError(); }


    private final static HttpClient client = HttpClient.newHttpClient();

    /**
     * Get-Method for the global HttpClient.
     * @return Reference to the global HttpClient instance.
     */
    public static HttpClient client() { return client; }
}