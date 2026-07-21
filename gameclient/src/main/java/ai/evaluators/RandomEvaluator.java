package ai.evaluators;

import ai.engine.Evaluator;
import ai.helper.EvaluatorHelper;
import client.game.Content;
import client.game.Move;
import client.game.Player.Color;
import java.util.List;
import java.util.Random;

/**
 * Evaluator implementation that assigns random evaluations to the given moves. Used for comparison
 * purposes only.
 */
public class RandomEvaluator extends Evaluator {

  /**
   * Creates a random evaluator.
   *
   * @param field       the position for which the moves are evaluated
   * @param playerColor the color of the player whose turn it is
   * @param moves       the list of moves to evaluate
   */
  public RandomEvaluator(Content[][] field, Color playerColor,
      List<Move> moves) {
    super(EvaluatorHelper::evaluatePosition, field, playerColor, moves);
  }

  @Override
  public void run() {
    Random random = new Random();

    for (Move move : this.moves) {
      super.updateEvaluation(move, random.nextInt());
    }
  }
}
