
package client.exception;

/**
 * Thrown when communication with the gameserver fails.
 */
public class ServerCommunicationException extends GameClientException {

  /**
   * Creates a new ServerCommunicationException.
   */
  public ServerCommunicationException() {
    super();
  }

  /**
   * Creates a new ServerCommunicationException with the given message.
   *
   * @param message The detail message
   */
  public ServerCommunicationException(String message) {
    super(message);
  }

  /**
   * Creates a new ServerCommunicationException with the given message and cause.
   *
   * @param message The detail message
   * @param cause   The underlying cause of this exception
   */
  public ServerCommunicationException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Creates a new ServerCommunicationException with the given cause.
   *
   * @param cause The underlying cause of this exception
   */
  public ServerCommunicationException(Throwable cause) {
    super(cause);
  }
}