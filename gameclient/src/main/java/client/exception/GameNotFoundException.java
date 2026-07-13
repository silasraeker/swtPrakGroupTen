package client.exception;



/**
 * Thrown when a game with a given ID cannot be found.
 */
public class GameNotFoundException extends RuntimeException {

  private final String gameId;

  /**
   * Creates a new GameNotFoundException for the given game ID.
   *
   * @param gameId The ID of the game that was not found
   */
  public GameNotFoundException(String gameId) {
    super("Game not found: " + gameId);
    this.gameId = gameId;
  }

  /**
   * Creates a new GameNotFoundException for the given game ID and cause.
   *
   * @param gameId The ID of the game that was not found
   * @param cause  The underlying cause of this exception
   */
  public GameNotFoundException(String gameId, Throwable cause) {
    super("Game not found: " + gameId, cause);
    this.gameId = gameId;
  }

  /**
   * @return The ID of the game that was not found
   */
  public String getGameId() {
    return gameId;
  }

  @Override
  public String toString() {
    return super.getMessage();
  }
}