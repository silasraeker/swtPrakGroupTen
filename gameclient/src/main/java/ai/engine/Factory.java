package ai.engine;

import ai.helper.EvaluatorHelper;
import ai.interfaces.PositionEvaluator;
import client.game.Content;
import client.game.Move;
import client.game.Player.Color;
import java.util.List;

/**
 * Factory for creating evaluators. Subclasses may store additional parameters and pass them to the
 * evaluators they create.
 */
public abstract class Factory {

  /**
   * Evaluates a given position.
   */
  protected final PositionEvaluator positionEvaluator;

  /**
   * Creates a factory using the given position evaluator.
   *
   * @param positionEvaluator the evaluator used to evaluate positions
   */
  public Factory(PositionEvaluator positionEvaluator) {
    super();

    if (positionEvaluator == null) {
      throw new IllegalArgumentException("The parameter 'positionEvaluator' is null");
    }
    this.positionEvaluator = positionEvaluator;
  }

  /**
   * Creates a factory with a default position evaluator.
   */
  public Factory() {
    this(EvaluatorHelper::evaluatePosition);
  }

  /**
   * Creates an evaluator instance.
   *
   * @param playerColor the color of the player whose turn it is
   * @param field       the position for which the moves are evaluated
   * @param moves       the list of moves to evaluate
   * @return an evaluator ready to perform the evaluation
   */
  public abstract Evaluator createInstance(Color playerColor, Content[][] field, List<Move> moves);
}
