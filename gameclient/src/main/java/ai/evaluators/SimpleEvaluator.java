package ai.evaluators;

import ai.engine.Evaluator;
import ai.helper.EvaluatorHelper;
import ai.interfaces.PositionEvaluator;
import client.game.Content;
import client.game.Move;
import client.game.Player;
import java.util.Iterator;
import java.util.List;

/**
 * Evaluator implementation that evaluates each move by calculating the resulting position. The
 * evaluation of the resulting position is used as the heuristic value for the move.
 */
public class SimpleEvaluator extends Evaluator {

  /**
   * Creates a simple evaluator.
   *
   * @param positionEvaluator evaluates a given position
   * @param field             the position for which the moves are evaluated
   * @param playerColor       the color of the player whose turn it is
   * @param moves             the list of moves to evaluate
   */
  public SimpleEvaluator(PositionEvaluator positionEvaluator, Content[][] field,
      Player.Color playerColor, List<Move> moves) {
    super(positionEvaluator, field, playerColor, moves);
  }

  @Override
  public void run() {
    Iterator<Move> iter = this.moves.iterator();

    while (iter.hasNext() && !this.isInterrupted()) {
      Move move = iter.next();
      EvaluatorHelper.makeMove(this.field, move);
      this.updateEvaluation(move, super.positionEvaluator.evaluatePosition(this.field));
      EvaluatorHelper.reverseMove(this.field, move);
    }
  }
}
