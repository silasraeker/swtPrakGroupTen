package ai.factories;

import ai.engine.Evaluator;
import ai.evaluators.AlphaBetaEvaluator;
import ai.evaluators.MCTSEvaluator;
import ai.evaluators.SimpleEvaluator;
import ai.interfaces.DepthCalculator;
import ai.interfaces.PositionEvaluator;
import client.game.Content;
import client.game.Move;
import client.game.Player.Color;
import java.util.List;

/**
 * Factory that creates different evaluator implementations depending on the number of possible
 * moves. Uses AlphaBeta for few moves, MCTS for many moves, and SimpleEvaluator for very large move
 * sets.
 */
public class MixedFactory extends MCTSFactory {

  /**
   * Number of available moves above which this factory uses MCTS.
   */
  private final int mctsMoveCountThreshold;

  /**
   * Creates a factory using the given position evaluator, search depth calculator, and MCTS
   * threshold.
   *
   * @param positionEvaluator      the evaluator used to evaluate positions
   * @param depthCalculator        the calculator used to determine the search depth for MCTS
   * @param mctsMoveCountThreshold the number of moves above which MCTS is used; must be at least 1
   */
  public MixedFactory(final PositionEvaluator positionEvaluator,
      final DepthCalculator depthCalculator, final int mctsMoveCountThreshold) {
    super(positionEvaluator, depthCalculator);

    if (mctsMoveCountThreshold < 1) {
      throw new IllegalArgumentException(
          "The MCTS move count threshold (" + mctsMoveCountThreshold + ") must be at least 1");
    }
    this.mctsMoveCountThreshold = mctsMoveCountThreshold;
  }

  @Override
  public Evaluator createInstance(Color playerColor, Content[][] field, List<Move> moves) {
    // If there are more than 2700 possible moves, use SimpleEvaluator because MCTS and AlphaBeta
    // become too expensive for such large move sets.
    if (moves.size() > 2700) {
      return new SimpleEvaluator(super.positionEvaluator, field, playerColor, moves);
    } else if (moves.size() > this.mctsMoveCountThreshold) {
      return new MCTSEvaluator(super.positionEvaluator, super.depthCalculator, field, playerColor,
          moves);
    } else {
      return new AlphaBetaEvaluator(super.positionEvaluator, field, playerColor, moves);
    }
  }

  @Override
  public String toString() {
    return "Mixed(Threshold=" + this.mctsMoveCountThreshold + ",Depth=" + super.depthCalculator
        + ")";
  }
}
