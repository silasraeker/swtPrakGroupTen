package client.exception;


/**
 * Thrown when data cannot be processed into the expected target class.
 */
public class InvalidDataException extends IllegalArgumentException {

    private final String data;
    private final Class<?> targetClass;
    private final String reason;

    /**
     * Creates a new InvalidDataException for the given target class and reason.
     *
     * @param targetClass The class the data was supposed to be processed into
     * @param reason      The reason why the data is invalid
     */
    public InvalidDataException(Class<?> targetClass, String reason) {
        this.data = null;
        this.targetClass = targetClass;
        this.reason = reason;
    }

    /**
     * Creates a new InvalidDataException for the given target class, reason and cause.
     *
     * @param targetClass The class the data was supposed to be processed into
     * @param reason      The reason why the data is invalid
     * @param cause       The underlying cause of this exception
     */
    public InvalidDataException(Class<?> targetClass, String reason, Throwable cause) {

        super(reason, cause);
        this.data = null;
        this.targetClass = targetClass;
        this.reason = reason;
    }

    /**
     * Creates a new InvalidDataException for the given data, target class and reason.
     *
     * @param data        The invalid data
     * @param targetClass The class the data was supposed to be processed into
     * @param reason      The reason why the data is invalid
     */
    public InvalidDataException(String data, Class<?> targetClass, String reason) {
        this.data = data;
        this.targetClass = targetClass;
        this.reason = reason;
    }

    /**
     * Creates a new InvalidDataException for the given data, target class, reason and cause.
     *
     * @param data        The invalid data
     * @param targetClass The class the data was supposed to be processed into
     * @param reason      The reason why the data is invalid
     * @param cause       The underlying cause of this exception
     */
    public InvalidDataException(String data, Class<?> targetClass, String reason, Throwable cause) {

        super(reason, cause);
        this.data = data;
        this.targetClass = targetClass;
        this.reason = reason;
    }

    @Override
    public String toString() {
        return (data == null) ? String.format("exception = %s; targetClass = %s; reason = %s", this.getClass().getName(), targetClass.getName(), reason)
                : String.format("exception = %s; data = %s; targetClass = %s; reason = %s", this.getClass().getName(), data, targetClass.getName(), reason);
    }

    /**
     * @return The invalid data, or null if not set
     */
    public String data() { return data; }

    /**
     * @return The class the data was supposed to be processed into
     */
    public Class<?> targetClass() { return targetClass; }

    /**
     * @return The reason why the data is invalid
     */
    public String reason() { return reason; }
}