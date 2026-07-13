package client.exception;

/**
 * Signals that a requested resource was not found.
 */
public class NotFoundException extends GameClientException {

  /**
   * Creates a new NotFoundException with the given message.
   *
   * @param message The detail message
   */
  public NotFoundException(String message) {
    super(message);
  }

  /**
   * Creates a new NotFoundException with the given message and cause.
   *
   * @param message The detail message
   * @param cause   The underlying cause of this exception
   */
  public NotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}