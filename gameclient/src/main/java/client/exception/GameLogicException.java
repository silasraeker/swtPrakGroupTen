package client.exception;

/**
 * Exception for errors in the game logic.
 */
public class GameLogicException extends GameClientException {

  /**
   * Creates a new GameLogicException.
   */
  public GameLogicException() {
    super();
  }

  /**
   * Creates a new GameLogicException with the given message.
   *
   * @param message The detail message
   */
  public GameLogicException(String message) {
    super(message);
  }

  /**
   * Creates a new GameLogicException with the given message and cause.
   *
   * @param message The detail message
   * @param cause   The underlying cause of this exception
   */
  public GameLogicException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Creates a new GameLogicException with the given cause.
   *
   * @param cause The underlying cause of this exception
   */
  public GameLogicException(Throwable cause) {
    super(cause);
  }
}