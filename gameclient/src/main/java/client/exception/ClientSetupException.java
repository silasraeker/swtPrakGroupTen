package client.exception;

/**
 * Thrown when the gameclient fails to set up correctly.
 */
public class ClientSetupException extends GameClientException {

  /**
   * Creates a new ClientSetupException.
   */
  public ClientSetupException() {
    super();
  }

  /**
   * Creates a new ClientSetupException with the given message.
   *
   * @param message The detail message
   */
  public ClientSetupException(String message) {
    super(message);
  }

  /**
   * Creates a new ClientSetupException with the given message and cause.
   *
   * @param message The detail message
   * @param cause   The underlying cause of this exception
   */
  public ClientSetupException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Creates a new ClientSetupException with the given cause.
   *
   * @param cause The underlying cause of this exception
   */
  public ClientSetupException(Throwable cause) {
    super(cause);
  }
}