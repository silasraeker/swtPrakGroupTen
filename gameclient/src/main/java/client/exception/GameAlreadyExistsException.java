package client.exception;



public class GameAlreadyExistsException extends RuntimeException {

  private final String gameId;

  public GameAlreadyExistsException(String gameId, String reason) {
    super(reason);
    this.gameId = gameId;
  }

  public GameAlreadyExistsException(String gameId, String reason, Throwable cause) {
    super(reason, cause);
    this.gameId = gameId;
  }

  public String getGameId() {
    return gameId;
  }

  @Override
  public String toString() {
    return super.getMessage();
  }
}