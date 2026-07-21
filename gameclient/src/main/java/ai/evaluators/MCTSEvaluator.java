package ai.evaluators;

import ai.engine.Evaluator;
import ai.interfaces.DepthCalculator;
import ai.interfaces.PositionEvaluator;
import client.game.Content;
import client.game.Move;
import client.game.Player.Color;
import ai.helper.EvaluatorHelper;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Evaluator implementation using Monte Carlo Tree Search (MCTS). Evaluates moves by applying them
 * and simulating a number of random moves. The score is calculated as the average evaluation of the
 * resulting positions.
 */
public class MCTSEvaluator extends Evaluator {

  /**
   * Calculator used to determine the search depth.
   */
  private final DepthCalculator depthCalculator;

  /**
   * Creates an MCTS evaluator.
   *
   * @param positionEvaluator evaluates a given position
   * @param depthCalculator   calculator used to determine the search depth
   * @param field             the position for which the moves are evaluated
   * @param playerColor       the color of the player whose turn it is
   * @param moves             the list of moves to evaluate
   */
  public MCTSEvaluator(PositionEvaluator positionEvaluator, DepthCalculator depthCalculator,
      Content[][] field, Color playerColor, List<Move> moves) {
    super(positionEvaluator, field, playerColor, moves);

    if (depthCalculator == null) {
      throw new IllegalArgumentException("the parameter 'depthCalculator' is null");
    }
    this.depthCalculator = depthCalculator;
  }

  @Override
  public void run() {
    double[] averageEvaluations = new double[moves.size()];

    // Initialize evaluations with the evaluation of the position after executing each move.
    for (int i = 0; i < averageEvaluations.length && !this.isInterrupted(); i++) {
      EvaluatorHelper.makeMove(this.field, this.moves.get(i));
      averageEvaluations[i] = super.positionEvaluator.evaluatePosition(this.field);
      this.updateEvaluation(this.moves.get(i), (int) averageEvaluations[i]);
      EvaluatorHelper.reverseMove(this.field, this.moves.get(i));
    }

    // Count the empty fields to determine the search depth.
    int emptyFieldCount = 0;
    for (Content[] row : field) {
      for (Content content : row) {
        if (content == Content.EMPTY) {
          emptyFieldCount++;
        }
      }
    }
    int depth = this.depthCalculator.getDepth(emptyFieldCount);

    // Monte Carlo Tree Search.
    double counter = 2;
    while (!this.isInterrupted()) {
      for (int i = 0; i < averageEvaluations.length && !this.isInterrupted(); i++) {
        // Make the move to be evaluated.
        EvaluatorHelper.makeMove(this.field, this.moves.get(i));
        Color currentPlayerColor = (this.playerColor == Color.WHITE) ? Color.BLACK : Color.WHITE;

        LinkedList<Move> appliedMoves = new LinkedList<>();
        appliedMoves.add(this.moves.get(i));

        // Executes a number of random moves according to the search depth.
        for (int j = 0; j < depth && !this.isInterrupted(); j++) {
          Move randomMove = EvaluatorHelper.getRandomMove(this.field, currentPlayerColor);

          // If there are no more legal moves, end the current calculation branch.
          if (randomMove == null) {
            break;
          }
          EvaluatorHelper.makeMove(this.field, randomMove);
          appliedMoves.add(randomMove);
          currentPlayerColor = (currentPlayerColor == Color.WHITE) ? Color.BLACK : Color.WHITE;
        }

        // Evaluate the resulting position and update the evaluation of the corresponding move.
        int eval = super.positionEvaluator.evaluatePosition(field);
        averageEvaluations[i] = averageEvaluations[i] * ((counter - 1) / counter) + eval * (1 / counter);
        this.updateEvaluation(this.moves.get(i), (int) averageEvaluations[i]);

        // Undo all executed moves.
        Iterator<Move> reverseMoveIterator = appliedMoves.descendingIterator();
        while (reverseMoveIterator.hasNext()) {
          EvaluatorHelper.reverseMove(this.field, reverseMoveIterator.next());
        }
      }
      counter++;
    }
  }
}
