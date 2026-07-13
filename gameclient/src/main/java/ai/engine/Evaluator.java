package ai.engine;

import client.game.Move;
import client.game.Content;
import client.game.Player.Color;
import java.util.List;

/**
 * Abstract base class for evaluators. Provides helper methods and handles multithreading.
 * Subclasses evaluate a set of candidate moves from the current position and assign a score to each
 * move. The evaluation follows the convention commonly used in chess engines: positive values
 * indicate an advantage for White, negative values indicate an advantage for Black, and zero
 * represents an equal position. The larger the absolute value, the greater the advantage for the
 * respective side.
 */
public abstract class Evaluator extends Thread {

  /**
   * the color of the player whose turn it is
   */
  protected final Color playerColor;

  /**
   * the position for which the moves are evaluated
   */
  protected final Content[][] field;

  /**
   * the list of moves to evaluate
   */
  protected final List<Move> moves;

  /**
   * The current leading evaluation
   */
  private volatile int best_eval;

  /**
   * The current best move
   */
  private volatile Move best_move;

  /**
   * @param field  the position for which the moves are evaluated
   * @param playerColor the color of the player whose turn it is
   * @param moves  the list of moves to evaluate
   */
  public Evaluator(Content[][] field, Color playerColor, final List<Move> moves) {
    super();

    //Test Parameters
    if (field == null) {
      throw new IllegalArgumentException("the parameter 'field' is null");
    } else if (playerColor == null) {
      throw new IllegalArgumentException("the parameter 'playerColor' is null");
    } else if (moves == null) {
      throw new IllegalArgumentException("the parameter 'moves' is null");
    }

    this.playerColor = playerColor;
    this.field = field;
    this.moves = moves;
    this.best_eval = playerColor == Color.WHITE ? Integer.MIN_VALUE + 1 : Integer.MAX_VALUE - 1;
    this.best_move = moves.getFirst();
  }

  /**
   * The Process of evaluating the given moves.
   */
  @Override
  public abstract void run();

  /**
   * Checks whether the given evaluation is better than any evaluation found so far. Should be
   * called whenever an evaluation is updated.
   *
   * @param move       the move whose evaluation has changed
   * @param evaluation the new evaluation of the given move
   */ //TODO Maybe change from an update-based System to an Get-On-Demand System
  public final void updateEvaluation(final Move move, final double evaluation) {
    if (this.best_move == move || (this.playerColor == Color.WHITE && evaluation > this.best_eval)
        || (this.playerColor == Color.BLACK && evaluation < this.best_eval)) {
      this.best_eval = (int) evaluation;
      this.best_move = move;
    }
  }

  /**
   * @return the best move found so far according to the calculations
   */
  public final Move getBestMove() {
    return this.best_move;
  }

  /**
   * @return the evaluation of the best move found so far according to the calculations
   */
  public final int getBestEvaluation() {
    return (int) this.best_eval;
  }


}
