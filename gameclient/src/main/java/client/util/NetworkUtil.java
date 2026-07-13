package client.util;

import java.util.Objects;

import client.exception.InvalidDataException;
import client.exception.InvalidURIException;
import client.exception.ServerCommunicationException;
import client.util.provider.HttpClientProvider;
import client.util.provider.ObjectMapperProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;


/**
 * Utility class for HTTP requests and JSON (de)serialization in the gameclient.
 * Provides static helpers for sending DTOs, handling responses and validating URIs.
 */
public final class NetworkUtil {

    private static final Logger logger = LoggerFactory.getLogger(NetworkUtil.class);

    private static URIValidator validator = URIValidator.builder()
            .requireHost()
            .allowedSchemes("http")
            .build();


    /**
     * @throws InstantiationError always; this utility class must not be instantiated
     */
    private NetworkUtil() { throw new InstantiationError(); }

    

    public static List<URI> clientIPAddresses()
        throws SocketException {

        List<URI> res = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        
        for (NetworkInterface netint : Collections.list(interfaces)) {
            if (netint.isLoopback() || !netint.isUp()) {continue;}

            Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
            for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                if (inetAddress instanceof Inet4Address) {
                    res.add(URI.create(inetAddress.getHostAddress()));
                }
            }
        }

        return res;
    }

    /**
     * Validates the given URI and attempts a TCP connection to verify reachability.
     *
     * @param remoteURI The URI to check
     * @param timeoutMs Connection timeout in milliseconds
     * @throws IllegalArgumentException if the URI fails validation – either rejected by the local
     *                                  validator or re-thrown from the validator's internal checks
     * @throws ServerCommunicationException if the remote endpoint is not reachable within the timeout
     */
    public static boolean checkConnection(URI remoteURI, int timeoutMs)
        throws InvalidURIException {

        Objects.requireNonNull(remoteURI);
        URIValidator.validateRoot(remoteURI);

        try (Socket socket = new Socket()) {

            InetSocketAddress serverSocketAddress = new InetSocketAddress( remoteURI.getHost(), remoteURI.getPort() );
            socket.connect(serverSocketAddress, timeoutMs);

            return true;
        }
        catch (IOException ioe) { return false; }
    }


    /**
     * Validates that the given HTTP status code is in the 2xx success range.
     *
     * @param statusCode The HTTP status code to validate
     * @throws IllegalArgumentException if statusCode is not in the range [200, 300)
     */
    public static void validateStatusCode(int statusCode)
        throws IllegalArgumentException {
        if (statusCode < 200 || 300 <= statusCode) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Serializes the given object to JSON and sends it via POST to the given URI.
     *
     * @param obj The object to send
     * @param uri The target URI
     * @throws IOException          if serialization fails, the request fails, or the response status is not 2xx
     * @throws InterruptedException if the request is interrupted
     */
    public static <T> void sendDTO(T obj, URI uri) throws IOException, InterruptedException {
        
        String data = serializeObject(obj);
        sendJson(data, uri);
    }

    /**
     * Serializes the given object to JSON, sends it via POST to the given URI and
     * deserializes the response into the given class.
     *
     * @param obj      The object to send
     * @param resClass The class of the expected response
     * @param uri      The target URI
     * @return The deserialized response
     * @throws IOException          if serialization, the request, or response deserialization fails
     * @throws InterruptedException if the request is interrupted
     */

    public static <T, R> R sendDTO(T obj, Class<R> resClass, URI uri) throws IOException, InterruptedException {
        
        String data = serializeObject(obj);
        return sendJson(data, resClass, uri);
    }

    public static <T> T getDTO(URI uri, Class<T> resClass) throws IOException, InterruptedException {

        validator.validate(uri);
        HttpClient client = HttpClientProvider.client();

        logger.trace("Sending GET request: uri = {}", uri);

        HttpRequest req = HttpRequest.newBuilder()
            .uri(uri)
            .header("Accept", "application/json")
            .GET()
            .build();

        HttpResponse<InputStream> res = client.send(req, HttpResponse.BodyHandlers.ofInputStream());
        return deserializeStream(res.body(), resClass);
    }


    /**
     * Sends the given JSON string via POST to the given URI.
     *
     * @param json The JSON string to send
     * @param uri  The target URI
     * @throws IOException          if the request fails or the response status is not 2xx
     * @throws InterruptedException if the request is interrupted
     */
    public static void sendJson(String json, URI uri) throws IOException, InterruptedException {

        validator.validate(uri);
        HttpClient client = HttpClientProvider.client();
        
        logger.trace("Sending json: json = {}; uri = {}", json, uri);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        
        HttpResponse<Void> response = client.send(req, HttpResponse.BodyHandlers.discarding());
        validateStatusCode(response.statusCode());

        logger.debug("Json sent successfully.");
    }

    /**
     * Sends the given JSON string via POST to the given URI and deserializes the response.
     *
     * @param json     The JSON string to send
     * @param resClass The class of the expected response
     * @param uri      The target URI
     * @return The deserialized response
     * @throws IOException          if the request fails, the response status is not 2xx,
     *                              or response deserialization fails
     * @throws InterruptedException if the request is interrupted
     */
    public static <T> T sendJson(String json, Class<T> resClass, URI uri) throws IOException, InterruptedException {

        validator.validate(uri);
        HttpClient client = HttpClientProvider.client();

        logger.trace("Sending json: json = {}; uri = {}", json, uri);
        
        HttpRequest req = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        
        HttpResponse<InputStream> response = client.send(req, HttpResponse.BodyHandlers.ofInputStream());

        try (InputStream stream = response.body()) {
            validateStatusCode(response.statusCode());
            logger.debug("Json sent successfully.");

            T res = deserializeStream(stream, resClass);
            logger.trace("Response parsed successfully: response = {}", res);

            return res;
        }
    }

    /**
     * Reads and deserializes the JSON body of the given HTTP exchange into an instance of the given class.
     *
     * @param req      The HTTP exchange containing the request body
     * @param resClass The target class for deserialization
     * @return The deserialized object
     * @throws IOException       if reading the request body fails
     * @throws DatabindException if the parsed JSON cannot be bound to the target class
     */
    public static <T> T deserializeBody(HttpExchange req, Class<T> resClass) throws InvalidDataException, IOException {

        try (InputStream requestBody = req.getRequestBody()) {
            return deserializeStream(requestBody, resClass);
        }
    }

    /**
     * Deserializes JSON content from the given InputStream into an instance of the given class.
     *
     * @param stream   The InputStream containing JSON data
     * @param resClass The target class for deserialization
     * @return The deserialized object
     * @throws IOException       if the stream cannot be read
     * @throws DatabindException if the parsed JSON cannot be bound to the target class
     */
    public static <T> T deserializeStream(InputStream stream, Class<T> resClass) throws InvalidDataException, IOException {

        try {
            T res = ObjectMapperProvider.mapper()
                .readValue(stream, resClass);
            logger.trace("Stream deserialized successfully: result = {}", res);
            return res;
        }
        catch (StreamReadException sre) {
            logger.warn("sre, resClass= {}, sre= {} stream = {}", resClass.getName(), sre.getStackTrace(), stream);
            // received broken json
            throw new InvalidDataException(
                resClass,
                "Received broken .json, unable to deserialize!",
                sre
            );
        }
        catch (DatabindException dbe) {
            logger.warn("dbe, resClass= {}, sre= {}", resClass.getName(), dbe.getStackTrace());
            // can not bind the json variables to the class fields
            throw new InvalidDataException(
                resClass,
                "Not able to bind .json variables to class fields, unable to deserialize!",
                dbe
            );
        }
        catch (IOException ioe) {
            // network/software error, stream broken
            logger.warn("Network or Software error while deserealizing to {}.", resClass);
            throw ioe;
        }
    }

    /**
     * Serializes the given object to a JSON string.
     *
     * @param obj The object to serialize
     * @return The JSON representation of the object
     * @throws JsonProcessingException if the object cannot be serialized
     */
    public static <T> String serializeObject(T obj) throws InvalidDataException {

        ObjectMapper mapper = ObjectMapperProvider.mapper();

        try {
            String res = mapper.writeValueAsString(obj);
            logger.trace("Object serialized successfully: result = {}", res);
            return res;
        }
        catch (JsonProcessingException jpe) {
            // circular references between two classes or no access to attributes
            throw new InvalidDataException(
                (obj != null) ? obj.getClass() : Object.class,
                "Circular references between two classes or no access to attributes, unable to serialize!",
                jpe
            );
        }
    }

    /**
     * Sends an HTTP response with the given status code and response body.
     *
     * @param req      The HTTP exchange to respond to
     * @param code     The HTTP status code to send
     * @param response The response body
     * @throws IOException if sending the response fails
     */
    public static void respond(HttpExchange req, int code, String response) throws IOException {

        logger.trace("Sending response: targetURI = {}; statusCode = {}; responseBody = {}", req.getRequestURI(), code, response);

        byte[] body = response.getBytes(StandardCharsets.UTF_8);
        req.sendResponseHeaders(code, body.length);

        try (OutputStream os = req.getResponseBody()) {
            os.write(response.getBytes());
            logger.trace("Sent response.");
        }
    }

    public static <T> void respond(HttpExchange req, int code, T response) throws IOException {

        String responseJson = NetworkUtil.serializeObject(response);
        logger.trace("Sending response: targetURI = {}; statusCode = {}; responseBody = {}", req.getRequestURI(), code, responseJson);
        NetworkUtil.respond(req, code, responseJson);
        logger.trace("Sent response.");
    }

    public static void respond(HttpExchange req, int code) throws IOException {

        logger.trace("Sending response: targetURI = {}; statusCode = {}", req.getRequestURI(), code);
        req.sendResponseHeaders(code, 0);
        logger.trace("Sent response.");
    }



    // Getter

    // Setter

    /**
     * Replaces the global URIValidator used by this utility for outgoing requests.
     *
     * @param uriValidator The new validator to use; must not be null
     * @throws NullPointerException if uriValidator is null
     */
    public static void setURIValidator(URIValidator uriValidator)
        throws NullPointerException {
        Objects.requireNonNull(uriValidator);
        validator = uriValidator;
        logger.info("Changed URI-Validator.");
    }
}
