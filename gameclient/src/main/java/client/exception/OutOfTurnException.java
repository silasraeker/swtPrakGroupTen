package client.exception;

/**
 * Thrown when a player tries to make a move out of turn.
 */
public class OutOfTurnException extends GameLogicException {

  /**
   * Creates a new OutOfTurnException.
   */
  public OutOfTurnException() {
    super();
  }

  /**
   * Creates a new OutOfTurnException with the given message.
   *
   * @param message The detail message
   */
  public OutOfTurnException(String message) {
    super(message);
  }

  /**
   * Creates a new OutOfTurnException with the given message and cause.
   *
   * @param message The detail message
   * @param cause   The underlying cause of this exception
   */
  public OutOfTurnException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Creates a new OutOfTurnException with the given cause.
   *
   * @param cause The underlying cause of this exception
   */
  public OutOfTurnException(Throwable cause) {
    super(cause);
  }
}
