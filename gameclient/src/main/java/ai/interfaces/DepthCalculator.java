package ai.interfaces;

/**
 * Provides a method for dynamically determining the search depth for MCTS based on the number of
 * empty fields in the current position.
 */
public interface DepthCalculator {

  /**
   * Returns the search depth for the given number of empty fields.
   *
   * @param emptyFields the number of empty fields in the current position
   * @return the search depth
   */
  public int getDepth(final int emptyFields);
}
