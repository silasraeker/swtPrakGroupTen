package ai.evaluators;

import ai.engine.Evaluator;
import client.game.Content;
import client.game.Move;
import client.game.Player.Color;
import ai.helper.EvaluatorHelper;
import java.util.List;

public class MCTSEvaluator extends Evaluator {

  /**
   * @param field  the position for which the moves are evaluated
   * @param playerColor the color of the player whose turn it is
   * @param moves  the list of moves to evaluate
   */
  public MCTSEvaluator(Content[][] field, Color playerColor, List<Move> moves) {
    super(field, playerColor, moves);
  }

  @Override
  public void run() {
    double[] evals = new double[moves.size()];

    // Initial Evaluation
    for (int i = 0; i < evals.length && !this.isInterrupted(); i++) {
      EvaluatorHelper.makeMove(this.field, this.moves.get(i));
      evals[i] = EvaluatorHelper.evaluatePosition(this.field);
      EvaluatorHelper.reverseMove(this.field, this.moves.get(i));
    }

    //Monte Carlo Tree Search
    double counter = 2;
    int empty_fields = 0;
    for (int i = 0; i < field.length; i++) {
      for (int j = 0; j < field[0].length; j++) {
        if (field[i][j] == Content.EMPTY) {
          empty_fields++;
        }
      }
    }
    int depth = empty_fields > 40 ? 6 : 10;
    // TODO: experiment with different parameters for depth and empty_fields
    while (!this.isInterrupted()) {
      for (int i = 0; i < evals.length && !this.isInterrupted(); i++) {
        // TODO test about Performance in contrast to using 'reverseMove' instead of 'copyField'
        Content[][] field = EvaluatorHelper.copyField(this.field);
        Color currentPlayerColor = (this.playerColor == Color.WHITE) ? Color.BLACK : Color.WHITE;
        EvaluatorHelper.makeMove(field, this.moves.get(i));

        for (int j = 0; j < depth && !this.isInterrupted(); j++) {
          Move randomMove = EvaluatorHelper.getRandomMove(field, currentPlayerColor);

          if (randomMove == null) {
            break;
          }
          EvaluatorHelper.makeMove(field, randomMove);
          currentPlayerColor = (currentPlayerColor == Color.WHITE) ? Color.BLACK : Color.WHITE;
        }

        int eval = EvaluatorHelper.evaluatePosition(field);
        evals[i] = evals[i] * ((counter - 1) / counter) + eval * (1 / counter);
        this.updateEvaluation(this.moves.get(i), evals[i]);
      }

      counter++;
    }
  }
}
