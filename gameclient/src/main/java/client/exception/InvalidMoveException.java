package client.exception;

/**
 * Thrown when a move is invalid.
 */
public class InvalidMoveException extends GameLogicException {

  /**
   * Creates a new InvalidMoveException.
   */
  public InvalidMoveException() {
    super();
  }

  /**
   * Creates a new InvalidMoveException with the given message.
   *
   * @param message The detail message
   */
  public InvalidMoveException(String message) {
    super(message);
  }

  /**
   * Creates a new InvalidMoveException with the given message and cause.
   *
   * @param message The detail message
   * @param cause   The underlying cause of this exception
   */
  public InvalidMoveException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Creates a new InvalidMoveException with the given cause.
   *
   * @param cause The underlying cause of this exception
   */
  public InvalidMoveException(Throwable cause) {
    super(cause);
  }
}