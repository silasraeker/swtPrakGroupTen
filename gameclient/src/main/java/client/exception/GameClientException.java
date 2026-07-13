package client.exception;

/**
 * Base exception for errors in the gameclient.
 */
public class GameClientException extends Exception {

  /**
   * Creates a new GameClientException.
   */
  public GameClientException() {
    super();
  }

  /**
   * Creates a new GameClientException with the given message.
   *
   * @param message The detail message
   */
  public GameClientException(String message) {
    super(message);
  }

  /**
   * Creates a new GameClientException with the given message and cause.
   *
   * @param message The detail message
   * @param cause   The underlying cause of this exception
   */
  public GameClientException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Creates a new GameClientException with the given cause.
   *
   * @param cause The underlying cause of this exception
   */
  public GameClientException(Throwable cause) {
    super(cause);
  }
}