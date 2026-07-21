package ai.factories;

import ai.engine.Evaluator;
import ai.engine.Factory;
import ai.evaluators.MCTSEvaluator;
import ai.helper.EvaluatorHelper;
import ai.interfaces.DepthCalculator;
import ai.interfaces.PositionEvaluator;
import client.game.Content;
import client.game.Move;
import client.game.Player.Color;
import java.util.List;

/**
 * Factory for creating Monte Carlo Tree Search (MCTS) evaluators. Stores the depth calculator used
 * to determine the search depth.
 */
public class MCTSFactory extends Factory {

  /**
   * Calculator used to determine the MCTS search depth.
   */
  protected final DepthCalculator depthCalculator;

  /**
   * Creates a factory using the given position evaluator and search depth calculator.
   *
   * @param positionEvaluator the evaluator used to evaluate positions
   * @param depthCalculator   the calculator used to determine the search depth
   */
  public MCTSFactory(final PositionEvaluator positionEvaluator,
      final DepthCalculator depthCalculator) {
    super(positionEvaluator);

    if (depthCalculator == null) {
      throw new IllegalArgumentException("the parameter 'depthCalculator' is null");
    }
    this.depthCalculator = depthCalculator;
  }

  /**
   * Creates a factory using the given search depth calculator and Voronoi evaluation as default.
   *
   * @param depthCalculator the calculator used to determine the search depth
   */
  public MCTSFactory(final DepthCalculator depthCalculator) {
    this(EvaluatorHelper::voronoi, depthCalculator);
  }

  /**
   * Creates a factory with a default search depth of 5 and Voronoi evaluation.
   */
  public MCTSFactory() {
    this(emptyFields -> 5);
  }

  @Override
  public Evaluator createInstance(Color playerColor, Content[][] field, List<Move> moves) {
    return new MCTSEvaluator(super.positionEvaluator, this.depthCalculator, field, playerColor,
        moves);
  }

  @Override
  public String toString() {
    return "MCTS with " + this.depthCalculator;
  }
}
