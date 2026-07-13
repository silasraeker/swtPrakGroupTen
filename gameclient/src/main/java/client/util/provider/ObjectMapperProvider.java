package client.util.provider;

import java.net.URI;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;

/**
 * A Singleton class to manage an instance of a {@link ObjectMapper}.
 * <p>
 * This class should neither be instantiated nor inherited.
 * <p>
 * Usage Example:
 * <pre>{@code
 * Classname object = ...;
 * ObjectMapper mapper = ObjectMapperProvider.mapper();
 * String data = mapper.writeValueAsString(object);
 * }</pre>
 */
public final class ObjectMapperProvider {

    /**
     * @throws InstantiationError always; this utility class must not be instantiated
     */
    private ObjectMapperProvider() { throw new InstantiationError(); }


    private final static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.coercionConfigFor(URI.class)
            .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull);
    }

    /**
     * Get-Method for the global ObjectMapper.
     * @return Reference to the global ObjectMapper instance.
     */
    public static ObjectMapper mapper() { return mapper; }
}