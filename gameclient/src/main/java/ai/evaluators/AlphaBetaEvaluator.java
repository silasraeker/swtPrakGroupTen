package ai.evaluators;

import ai.engine.Evaluator;
import ai.interfaces.PositionEvaluator;
import client.game.Content;
import client.game.Move;
import ai.helper.EvaluatorHelper;
import client.game.Player.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SplittableRandom;

public class AlphaBetaEvaluator extends Evaluator {

  private static final int INF = Integer.MAX_VALUE / 2;
  private final int boardSize;

  public static final int[][] DIRS = {
    {0, 1}, {0, -1}, {1, 0}, {-1, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
  };

  public static final int[][] KNIGHTS = {
    {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
    {2, -1}, {2, 1}, {1, -2}, {1, 2}
  };

  private static final int TT_SIZE = 1 << 20;
  private final long[] ttKey = new long[TT_SIZE];
  private final int[] ttValue = new int[TT_SIZE];
  private final int[] ttDepth = new int[TT_SIZE];
  private final byte[] ttFlag = new byte[TT_SIZE];
  private static final byte EXACT = 0, LOWER = 1, UPPER = 2;

  // Deepest fully completed iterative-deepening level (for KIClient class)
  private volatile int lastCompletedDepth = 0;

  public int getSearchDepth() {
    return lastCompletedDepth;
  }

  private final long[][][] Z;

  // Root moves assigned to this thread used for prioritizing move ordering
  private final Set<Move> assignedRootMoves;

  private static long[][][] buildZobrist(int boardSize) {
    long[][][] z = new long[boardSize][boardSize][4];
    SplittableRandom rng = new SplittableRandom(0L);
    for (long[][] a : z) for (long[] b : a) for (int k = 0; k < 4; k++) b[k] = rng.nextLong();
    return z;
  }

  /**
   * @param field the position for which the moves are evaluated
   * @param playerColor the color of the player whose turn it is
   * @param moves the list of moves to evaluate
   */
  public AlphaBetaEvaluator(
      PositionEvaluator positionEvaluator, Content[][] field, Color playerColor, List<Move> moves) {
    super(positionEvaluator, field, playerColor, moves);
    this.boardSize = field.length;
    this.Z = buildZobrist(boardSize);
    this.assignedRootMoves = new HashSet<>(moves);
  }

  @Override
  public void run() {
    int depth = 1;
    while (!this.isInterrupted()) {
      searchDepth(depth);
      depth++;
    }
  }

  // search one fixed depth from root position
  private void searchDepth(int depth) {
    List<Move> allMoves = EvaluatorHelper.getAllPossibleMoves(this.field, this.playerColor);

    List<Move> threadSubset = new ArrayList<>();
    List<Move> remainder = new ArrayList<>();
    for (Move m : allMoves) {
      if (assignedRootMoves.contains(m)) threadSubset.add(m);
      else remainder.add(m);
    }
    sortMoves(threadSubset, this.field);
    sortMoves(remainder, this.field);

    List<Move> rootMoves = new ArrayList<>(threadSubset.size() + remainder.size());
    rootMoves.addAll(threadSubset);
    rootMoves.addAll(remainder);

    boolean isWhite = (this.playerColor == Color.WHITE);

    Move bestMove = null;
    int bestScore = isWhite ? -INF : INF;
    int alpha = -INF;
    int beta = INF;

    long rootHash = zobrist(this.field);

    for (Move move : rootMoves) {
      if (this.isInterrupted()) {
        return;
      }

      long childHash = incrementalHash(rootHash, this.field, move);
      Color opponentColor = isWhite ? Color.BLACK : Color.WHITE;

      EvaluatorHelper.makeMove(this.field, move);
      int score;
      try {
        score = alphaBeta(this.field, depth - 1, alpha, beta, opponentColor, childHash);
      } catch (InterruptedException e) {
        return;
      } finally {
        // Ensure the next root move in same depth pass (or the next depth pass entirely) starts
        // from correct root position
        EvaluatorHelper.reverseMove(this.field, move);
      }
      // discard partial result
      if (this.isInterrupted()) {
        return;
      }

      // update best root move
      if (isWhite ? score > bestScore : score < bestScore) {
        bestScore = score;
        bestMove = move;
      }

      // update alpha/beta window of root
      if (isWhite) alpha = Math.max(alpha, score);
      else beta = Math.min(beta, score);
    }
    if (bestMove != null) {
      this.commitBestResult(bestMove, bestScore);
      this.lastCompletedDepth = depth;
    }
  }

  public final void commitBestResult(final Move move, final double evaluation) {
    super.bestEval = (int) evaluation;
    super.bestMove = move;
  }

  private int alphaBeta(Content[][] board, int depth, int alpha, int beta, Color curr, long hash)
      throws InterruptedException {
    if (this.isInterrupted()) {
      throw new InterruptedException();
    }

    boolean maximize = (curr == Color.WHITE);
    int ttIdx = (int) (hash & (TT_SIZE - 1));
    if (ttKey[ttIdx] == hash && ttDepth[ttIdx] >= depth) {
      int v = ttValue[ttIdx];
      byte f = ttFlag[ttIdx];
      if (f == EXACT) return v;
      if (f == LOWER && v >= beta) return v;
      if (f == UPPER && v <= alpha) return v;
    }
    List<Move> moves = EvaluatorHelper.getAllPossibleMoves(board, curr);
    // Current Player loses if no moves are left
    if (moves.isEmpty()) {
      return maximize ? -INF : INF;
    }

    // evaluate position statically if leaf
    if (depth == 0) {
      return super.positionEvaluator.evaluatePosition(board);
    }

    sortMoves(moves, board);

    Color nextPlayerColor = (curr == Color.WHITE) ? Color.BLACK : Color.WHITE;
    int origAlpha = alpha;
    int origBeta = beta;
    int val = maximize ? -INF : INF;

    for (Move m : moves) {
      if (this.isInterrupted()) {
        throw new InterruptedException();
      }

      long childHash = incrementalHash(hash, board, m);

      EvaluatorHelper.makeMove(board, m);
      int v;
      try {
        v = alphaBeta(board, depth - 1, alpha, beta, nextPlayerColor, childHash);
      } finally {
        EvaluatorHelper.reverseMove(board, m);
      }

      if (maximize) {
        if (v > val) val = v;
        if (val > alpha) alpha = val;
      } else {
        if (v < val) val = v;
        if (val < beta) beta = val;
      }
      if (alpha >= beta) break; // Cut-Off
    }
    byte flag = (val <= origAlpha) ? UPPER : (val >= origBeta) ? LOWER : EXACT;
    ttKey[ttIdx] = hash;
    ttValue[ttIdx] = val;
    ttDepth[ttIdx] = depth;
    ttFlag[ttIdx] = flag;

    return val;
  }

  // Sorts in descending order ensuring that stronger candidates are searched first
  private void sortMoves(List<Move> moves, Content[][] field) {
    moves.sort((a, b) -> Integer.compare(mobility(field, b), mobility(field, a)));
  }

  // Counts how many empty / available squares are opened up from destination square
  // after accounting for origin of moved piece and fired arrow
  private int mobility(Content[][] field, Move move) {
    int toR = move.to().x();
    int toC = move.to().y();
    int frR = move.start().x();
    int frC = move.start().y();
    int arrR = move.arrow().x();
    int arrC = move.arrow().y();
    int count = 0;

    for (int[] dir : DIRS) {
      int r = toR + dir[0];
      int c = toC + dir[1];
      while (r >= 0 && r < boardSize && c >= 0 && c < boardSize) {
        boolean isOrigin = (r == frR && c == frC); // Treat as EMPTY
        boolean isArrow = (r == arrR && c == arrC); // blocked: stop here
        if (isArrow || (field[r][c] != Content.EMPTY && !isOrigin)) break;
        count++;
        r += dir[0];
        c += dir[1];
      }
    }
    for (int[] km : KNIGHTS) {
      int r = toR + km[0];
      int c = toC + km[1];
      if (r < 0 || r >= boardSize || c < 0 || c >= boardSize) continue;
      boolean isOrigin = (r == frR && c == frC);
      boolean isArrow = (r == arrR && c == arrC);
      if (!isArrow && (field[r][c] == Content.EMPTY || isOrigin)) count++;
    }
    return count;
  }

  // Compute zobrist hash of given board position once per depth path at root by XORing random
  // value assigned to each square's current content
  private long zobrist(Content[][] field) {
    long h = 0;
    for (int r = 0; r < boardSize; r++)
      for (int c = 0; c < boardSize; c++) h ^= Z[r][c][ordinal(field[r][c])];
    return h;
  }

  // Derives child hash from parent by XORing only the three squares changing when move is applied
  // to board
  private long incrementalHash(long hash, Content[][] field, Move move) {
    int startR = move.start().x();
    int startC = move.start().y();
    int toR = move.to().x();
    int toC = move.to().y();
    int arrR = move.arrow().x();
    int arrC = move.arrow().y();
    int pieceOrd = ordinal(field[startR][startC]);

    // True iff arrow fired back to origin
    boolean arrowAtStart = (arrR == startR && arrC == startC);

    // remove piece from start, place empty or arrow depending on edge case
    hash ^= Z[startR][startC][pieceOrd];
    hash ^= Z[startR][startC][arrowAtStart ? 3 : 0];

    // Destination was empty
    hash ^= Z[toR][toC][0];
    hash ^= Z[toR][toC][pieceOrd];

    // Arrow square if it differs from start
    if (!arrowAtStart) {
      hash ^= Z[arrR][arrC][0];
      hash ^= Z[arrR][arrC][3]; // set to ARROW
    }

    return hash;
  }

  // Maps square content to its respective index in zobrist table
  private static int ordinal(Content c) {
    if (c == Content.EMPTY) return 0;
    if (c == Content.WHITE_AMAZONE) return 1;
    if (c == Content.BLACK_AMAZONE) return 2;
    return 3; // if ARROW
  }
}
