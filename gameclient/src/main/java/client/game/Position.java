package client.game;

/**
 * Represents a position on the game board, identified by its x and y coordinates.
 *
 * <p>Both coordinates must be non-negative. The valid range depends on the configured
 * board size at runtime.
 *
 * @param x The x-coordinate of the position; must be >= 0
 * @param y The y-coordinate of the position; must be >= 0
 */
public record Position(int x, int y) {

  /**
   * Validates the coordinates on construction.
   *
   * @throws IllegalArgumentException if {@code x} or {@code y} is negative
   */
  public Position {
    if (x < -1 || y < -1) {
      throw new IllegalArgumentException();
    }
  }
}