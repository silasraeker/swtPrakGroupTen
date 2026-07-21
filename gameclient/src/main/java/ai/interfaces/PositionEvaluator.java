package ai.interfaces;

import client.game.Content;

/**
 * Provides a method for evaluating a given position.
 */
public interface PositionEvaluator {

  /**
   * Evaluates a given position.
   *
   * @param field the position to evaluate
   * @return the evaluation of the given position positive value is advantage for white, negative
   * value is advantage for black
   */
  public int evaluatePosition(Content[][] field);
}
