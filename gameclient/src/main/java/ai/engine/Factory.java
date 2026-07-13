package ai.engine;

import client.game.Content;
import client.game.Move;
import client.game.Player.Color;
import java.util.List;

/**
 * Factory for creating evaluators. Subclasses may store additional
 * parameters and pass them to the evaluators they create.
 */
public abstract class Factory {
  // TODO: add option for single-threaded evaluators
  /**
   * Creates an evaluator instance.
   *
   * @param playerColor the color of the player whose turn it is
   * @param field  the position for which the moves are evaluated
   * @param moves  the list of moves to evaluate
   * @return an evaluator ready to perform the evaluation
   */
  public abstract Evaluator createInstance(Color playerColor, Content[][] field, List<Move> moves);
}
