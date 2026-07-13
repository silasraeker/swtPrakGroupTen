package ai.evaluators;

import ai.engine.Evaluator;
import client.game.Content;
import client.game.Move;
import ai.helper.EvaluatorHelper;
import client.game.Player.Color;
import java.util.List;

public class AlphaBetaEvaluator extends Evaluator {

  private static final int INF = Integer.MAX_VALUE / 2;

  /**
   * @param field the position for which the moves are evaluated
   * @param playerColor the color of the player whose turn it is
   * @param moves the list of moves to evaluate
   */
  public AlphaBetaEvaluator(Content[][] field, Color playerColor, List<Move> moves) {
    super(field, playerColor, moves);
  }

  @Override
  public void run() {
    int depth = 1;
    while (!this.isInterrupted()) {
      searchDepth(depth);
      depth++;
    }
  }

  private void searchDepth(int depth) {
    List<Move> rootMoves = EvaluatorHelper.getAllPossibleMoves(this.field, this.playerColor);
    boolean isWhite = (this.playerColor == Color.WHITE);
    int alpha = -INF;
    int beta = INF;
    int score;
    for (Move move : rootMoves) {
      if (this.isInterrupted()) {
        return;
      }

      Content[][] child = EvaluatorHelper.copyField(this.field);
      EvaluatorHelper.makeMove(child, move);
      Color opponentColor = isWhite ? Color.BLACK : Color.WHITE;
      try {
        score = alphaBeta(child, depth - 1, alpha, beta, opponentColor);
      } catch (InterruptedException e) {
        return;
      }
      // discard partial result
      if (this.isInterrupted()) {
        return;
      }
      this.updateEvaluation(move, score);
      if (isWhite) {
        if (score > alpha) alpha = score;
      } else {
        if (score < beta) beta = score;
      }
    }
  }

  private int alphaBeta(Content[][] board, int depth, int alpha, int beta, Color curr)
      throws InterruptedException {
    if (this.isInterrupted()) {
      throw new InterruptedException();
    }
    boolean maximize = (curr == Color.WHITE);
    List<Move> moves = EvaluatorHelper.getAllPossibleMoves(board, curr);
    // Current Player loses if no moves are left
    if (moves.isEmpty()) {
      return maximize ? -INF : INF;
    }
    if (depth == 0) {
      return EvaluatorHelper.evaluatePosition(board);
    }

    Color nextPlayerColor = (curr == Color.WHITE) ? Color.BLACK : Color.WHITE;

    if (maximize) {
      int value = -INF;
      for (Move m : moves) {
        if (this.isInterrupted()) {
          return value;
        }
        Content[][] child = EvaluatorHelper.copyField(board);
        EvaluatorHelper.makeMove(child, m);
        value = Math.max(value, alphaBeta(child, depth - 1, alpha, beta, nextPlayerColor));
        alpha = Math.max(alpha, value);
        if (alpha >= beta) {
          break; // Beta Cut-Off
        }
      }
      return value;
    } else {
      int value = INF;
      for (Move m : moves) {
        if (this.isInterrupted()) {
          return value;
        }
        Content[][] child = EvaluatorHelper.copyField(board);
        EvaluatorHelper.makeMove(child, m);
        value = Math.min(value, alphaBeta(child, depth - 1, alpha, beta, nextPlayerColor));
        beta = Math.min(beta, value);
        if (beta <= alpha) {
          break; // Alpha Cut-Off
        }
      }
      return value;
    }
  }
}
