package ai.evaluators;

import ai.engine.Evaluator;
import client.game.Content;
import client.game.Move;
import client.game.Player.Color;
import java.util.List;
import java.util.Random;

/**
 * Evaluates the given moves randomly. For comparison purposes only.
 */
public class RandomEvaluator extends Evaluator {

  /**
   * @param field  the position for which the moves are evaluated
   * @param playerColor the color of the player whose turn it is
   * @param moves  the list of moves to evaluate
   */
  public RandomEvaluator(Content[][] field, Color playerColor,
      List<Move> moves) {
    super(field, playerColor, moves);
  }

  @Override
  public void run() {
    Random random = new Random();

    for (Move move : this.moves) {
      super.updateEvaluation(move, random.nextInt());
    }
  }
}
