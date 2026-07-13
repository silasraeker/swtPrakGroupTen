package client.game;



/**
 * Represents a complete move in the game, consisting of an amazon's movement and its arrow shot.
 *
 * @param start The starting position of the amazon
 * @param to The target position of the amazon after the movement
 * @param arrow The position the arrow is shot to
 */
public record Move(Position start, Position to, Position arrow) {

  /**
   * @throws DebugException if the movement or the arrow shot does not follow a valid movement
   *     pattern
   */
  /**
   * @throws DebugException if the movement or the arrow shot does not follow a valid movement
   *     pattern
   */
  public Move {
    boolean isValidQueen = validationQueenHelper(start, to) && validationQueenHelper(to, arrow);
    boolean isValidKnight = validationKnightHelper(start, to) && validationQueenHelper(to, arrow);

    if (!isValidQueen && !isValidKnight) {
      throw new IllegalArgumentException("Attempted to create a Move object with invalid directions.");
    }
  }

  public boolean validateDirections() {
    return validateDirectionsQueen() || validateDirectionsKnight();
  }

  /**
   * Validates whether both the movement and the shot of the move are in Queens movement pattern.
   *
   * @return whether both the movement and the shot of the move are in Queens movement pattern.
   */
  public boolean validateDirectionsQueen() {
    boolean move = validationQueenHelper(start, to);
    boolean shoot = validationQueenHelper(to, arrow);

    return move && shoot;
  }

  /**
   * Validates whether the move consists of first a Knight move and then a Queen shot.
   *
   * @return whether the move consists of first a Knight move and then a Queen shot.
   */
  public boolean validateDirectionsKnight() {
    boolean move = validationKnightHelper(start, to);
    boolean shoot = validationQueenHelper(to, arrow);

    return move && shoot;
  }

  /**
   * Validates whether a move from 'from' to 'to' is in the pattern a queen can move.
   *
   * @param from Field where the move starts.
   * @param to Field where the move ends.
   * @return whether it is in the Queens movement pattern.
   */
  private boolean validationQueenHelper(Position from, Position to) {
    int moveX = to.x() - from.x();
    int moveY = to.y() - from.y();

    if (moveX * moveY == 0 || moveX == moveY || moveX == -1 * moveY) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Validates whether a move from 'from' to 'to' is in the pattern a knight can move.
   *
   * @param from Field where the move starts.
   * @param to Field where the move ends.
   * @return whether it is in the knights movement pattern.
   */
  private boolean validationKnightHelper(Position from, Position to) {
    int moveX = to.x() - from.x();
    int moveY = to.y() - from.y();

    int absMoveX = Math.abs(moveX);
    int absMoveY = Math.abs(moveY);

    if (absMoveX == 1 && absMoveY == 2 || absMoveX == 2 && absMoveY == 1) {
      return true;
    }
    return false;
  }
}
