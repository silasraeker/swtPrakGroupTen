package ai.engine;

import ai.interfaces.PositionEvaluator;
import client.game.Move;
import client.game.Content;
import client.game.Player.Color;
import java.util.List;

/**
 * Base class for worker threads that evaluate a subset of legal moves. Each evaluator receives a
 * game board, the color of the player for which moves are evaluated, and a list of candidate moves.
 * Subclasses implement the evaluation algorithm in {@code run} and update the current best result
 * through {@code updateEvaluation}. Evaluation scores use a White-centric convention: positive
 * values favor White, negative values favor Black, and zero represents an equal position. Larger
 * absolute values represent a greater advantage for the corresponding side. Instances are intended
 * to be managed by KIClient. The best move and its evaluation can be read while the evaluator is
 * running.
 */
public abstract class Evaluator extends Thread {

  /**
   * Evaluates a given position.
   */
  protected final PositionEvaluator positionEvaluator;

  /**
   * The color of the player whose turn it is.
   */
  protected final Color playerColor;

  /**
   * The position for which the moves are evaluated.
   */
  protected final Content[][] field;

  /**
   * The list of moves to evaluate.
   */
  protected final List<Move> moves;

  /**
   * The current leading evaluation.
   */
  protected volatile int bestEval;

  /**
   * The current best move.
   */
  protected volatile Move bestMove;

  /**
   * @param positionEvaluator evaluates a given position
   * @param field             the position for which the moves are evaluated
   * @param playerColor       the color of the player whose turn it is
   * @param moves             the list of moves to evaluate
   */
  public Evaluator(PositionEvaluator positionEvaluator, Content[][] field, Color playerColor,
      final List<Move> moves) {
    super();

    // Validate constructor arguments before initializing the evaluator.
    if (field == null) {
      throw new IllegalArgumentException("the parameter 'field' is null");
    } else if (playerColor == null) {
      throw new IllegalArgumentException("the parameter 'playerColor' is null");
    } else if (moves == null) {
      throw new IllegalArgumentException("the parameter 'moves' is null");
    } else if (positionEvaluator == null) {
      throw new IllegalArgumentException("the parameter 'positionEvaluator' is null");
    }

    this.positionEvaluator = positionEvaluator;
    this.playerColor = playerColor;
    this.field = field;
    this.moves = moves;
    this.bestEval = playerColor == Color.WHITE ? Integer.MIN_VALUE + 1 : Integer.MAX_VALUE - 1;
    this.bestMove = moves.getFirst();
  }

  /**
   * Evaluates the assigned candidate moves. Implementations should regularly check
   * {@code isInterrupted} and stop promptly when interruption is requested. Whenever a move
   * receives a new evaluation, implementations should call {@code updateEvaluation} to publish a
   * potentially improved result.
   */
  @Override
  public abstract void run();

  /**
   * Checks whether the given evaluation is better than any evaluation found so far. Should be
   * called whenever an evaluation is updated.
   *
   * @param move       the move whose evaluation has changed
   * @param evaluation the new evaluation of the given move
   */
  public final void updateEvaluation(final Move move, final int evaluation) {
    if (this.bestMove == move || (this.playerColor == Color.WHITE && evaluation > this.bestEval)
        || (this.playerColor == Color.BLACK && evaluation < this.bestEval)) {
      this.bestEval = evaluation;
      this.bestMove = move;
    }
  }

  /**
   * @return the best move found so far according to the calculations
   */
  public final Move getBestMove() {
    return this.bestMove;
  }

  /**
   * @return the evaluation of the best move found so far according to the calculations
   */
  public final int getBestEvaluation() {
    return this.bestEval;
  }
}