package ai.engine;

import ai.evaluators.AlphaBetaEvaluator;
import ai.factories.MCTSFactory;
import ai.factories.MixedFactory;
import ai.factories.SimpleFactory;
import client.game.Content;
import client.game.Move;
import ai.helper.EvaluatorHelper;
import client.game.Player.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Coordinates parallel evaluator threads to find the best move for a given game position. A
 * calculation is started with {@code startCalculating}. While the calculation is running,
 * {@code getBestMove} can return the best move found so far. {@code stopCalculating} interrupts all
 * active evaluators, waits for them to terminate, and stores the final best move. A new calculation
 * cannot be started until the previous calculation has been stopped. This class is intended to
 * manage one active calculation at a time.
 */
public final class KIClient {

  /**
   * Difficulty levels for the AI.
   */
  public enum Difficulty {
    EASY, MEDIUM, HARD
  }

  /**
   * Logger for error messages.
   */
  private static final Logger logger = LoggerFactory.getLogger(KIClient.class);

  /**
   * The number of threads that can be used during the calculation process. Must be at least one.
   */
  private final int threadCount;

  /**
   * The color of the player whose turn it is.
   */
  private Color currentPlayerColor;

  /**
   * The factory used to create evaluators.
   */
  private final Factory evaluatorFactory;

  /**
   * List of evaluators that are either running or ready to run.
   */
  private List<Evaluator> activeEvaluators = new LinkedList<>();

  /**
   * The best move found so far according to the calculations.
   */
  private volatile Move bestMove;

  /**
   * Creates a new AI client.
   *
   * @param evaluatorFactory the factory used to create evaluators
   * @param threadCount      the number of threads to use
   * @throws IllegalArgumentException if {@code evaluatorFactory} is {@code null} or
   *                                  {@code threadCount} is less than 1
   */
  public KIClient(final Factory evaluatorFactory, final int threadCount) {
    super();

    if (threadCount < 1) {
      throw new IllegalArgumentException("Thread count must be at least 1, but was " + threadCount);
    } else if (evaluatorFactory == null) {
      throw new IllegalArgumentException("The parameter 'evaluatorFactory' is null");
    }
    this.evaluatorFactory = evaluatorFactory;
    this.threadCount = threadCount;
  }

  /**
   * Creates an AI client with the given difficulty level. The number of threads is set to the
   * number of available processors minus one.
   *
   * @param difficulty the difficulty level of the AI
   */
  public KIClient(Difficulty difficulty) {
    this(difficulty == Difficulty.EASY ? new SimpleFactory()
            : (difficulty == Difficulty.MEDIUM ? new MCTSFactory()
                : new MixedFactory(EvaluatorHelper::voronoi, emptyFieldCount -> 3, 15)),
        Math.max(1, Runtime.getRuntime().availableProcessors() - 1));
  }

  /**
   * Starts an asynchronous search for the best move in the given position. The legal moves are
   * distributed among multiple evaluator threads. Each evaluator receives its own copy of the game
   * board. A previous calculation must be stopped with {@code stopCalculating} before this method
   * is called again.
   *
   * @param field              the board position to evaluate
   * @param expectedMillis     the expected search duration in milliseconds
   * @param currentPlayerColor the color of the player for which a move is being calculated
   * @throws IllegalArgumentException if field or currentPlayerColor is null, or if no legal moves
   *                                  exist
   * @throws IllegalStateException    if another calculation is still active
   */
  public void startCalculating(final Content[][] field, final long expectedMillis,
      final Color currentPlayerColor) {
    if (field == null) {
      throw new IllegalArgumentException("The parameter 'field' is null");
    } else if (currentPlayerColor == null) {
      throw new IllegalArgumentException("The parameter 'currentPlayerColor' is null");
    }

    // A new search may only start after the previous evaluator threads have been stopped.
    if (!this.activeEvaluators.isEmpty()) {
      throw new IllegalStateException("The previous calculation has not been stopped yet");
    }

    this.currentPlayerColor = currentPlayerColor;
    this.bestMove = null;
    List<Move> moves = EvaluatorHelper.getAllPossibleMoves(field, currentPlayerColor);
    Iterator<Move> moveIterator = moves.iterator();

    // A search cannot be started when the current player has no legal moves.
    if (moves.isEmpty()) {
      throw new IllegalArgumentException("No legal moves available for evaluation");
    }

    // Creating more evaluators than legal moves would leave some evaluators without work.
    int numberOfEvaluators = this.threadCount;
    double movesPerEvaluator = moves.size() / (double) numberOfEvaluators;
    if (movesPerEvaluator < 1) {
      movesPerEvaluator = 1;
      numberOfEvaluators = moves.size();
    }
    // Create activeEvaluators, distribute moves among them, and start them.
    for (int i = 0; i < numberOfEvaluators; i++) {
      List<Move> evaluatorMoves = new ArrayList<>((int) (Math.ceil(movesPerEvaluator)));

      for (int j = (int) movesPerEvaluator * i; j < (int) movesPerEvaluator * (i + 1); j++) {
        evaluatorMoves.add(moveIterator.next());
      }

      // Assign moves left over from integer rounding to the final evaluator.
      if (i == numberOfEvaluators - 1) {
        while (moveIterator.hasNext()) {
          evaluatorMoves.add(moveIterator.next());
        }
      }

      Evaluator evaluator = this.evaluatorFactory.createInstance(currentPlayerColor,
          EvaluatorHelper.copyField(field), evaluatorMoves);
      this.activeEvaluators.add(evaluator);
      evaluator.start();
    }
  }

  /**
   * Stops the active calculation and stores the final best move. All evaluator threads are
   * interrupted and the method waits until they have terminated. The final result is selected from
   * the best results reported by the evaluators.
   *
   * @throws IllegalStateException if no calculation is currently active
   */
  public void stopCalculating() {
    if (this.activeEvaluators.isEmpty()) {
      throw new IllegalStateException(
          "There is no calculation process running that could be stopped");
    }

    // Move the active evaluator list into a local variable so that a new search can be started after
    // all previous evaluators have terminated.
    List<Evaluator> evaluatorsToStop = this.activeEvaluators;
    this.activeEvaluators = new LinkedList<>();

    // Signals all threads to stop.
    for (Evaluator evaluator : evaluatorsToStop) {
      evaluator.interrupt();
    }

    // Calculate the best move while stopping the calculation process to obtain the final results.
    int bestEvaluation =
        this.currentPlayerColor == Color.WHITE ? Integer.MIN_VALUE : Integer.MAX_VALUE;
    int bestDepth = 0;
    Move bestMove = evaluatorsToStop.getFirst().getBestMove();

    // Waits for the remaining active threads and clears the list. Ensures that all threads terminate correctly.
    while (!evaluatorsToStop.isEmpty()) {
      Evaluator evaluator = evaluatorsToStop.getFirst();
      while (evaluator.isAlive()) {
        try {
          evaluator.join(1000);
          if (evaluator.isAlive()) {
            logger.error("A thread took more than one seconds to terminate");
          }
        } catch (InterruptedException e) {
          logger.trace("Main thread was interrupted", e);
          throw new IllegalStateException("Main thread was interrupted");
        }
      }
      evaluatorsToStop.removeFirst();

      // Gets the best evaluation from the stopped evaluator and checks whether it is the best so far.
      int evaluation = evaluator.getBestEvaluation();
      boolean isBetterResult;
      if (evaluator instanceof AlphaBetaEvaluator alphaBetaEvaluator) {
        isBetterResult = isBetterResult(evaluation, alphaBetaEvaluator.getSearchDepth(),
            bestEvaluation, bestDepth);
        if (isBetterResult) {
          bestDepth = alphaBetaEvaluator.getSearchDepth();
        }
      } else {
        isBetterResult = ((this.currentPlayerColor == Color.WHITE && evaluation > bestEvaluation)
            || (this.currentPlayerColor == Color.BLACK && evaluation < bestEvaluation));
      }
      if (isBetterResult) {
        bestEvaluation = evaluation;
        bestMove = evaluator.getBestMove();
      }
    }
    this.bestMove = bestMove;
  }

  /**
   * Returns the best move found by the active evaluators so far. If the calculation has already
   * been stopped, this method returns the final move selected during stopCalculating.
   *
   * @return the best move currently available
   * @throws IllegalStateException if no calculation has been started
   */
  public Move getBestMove() {
    if (this.activeEvaluators.isEmpty()) {
      if (this.bestMove != null) {
        return this.bestMove;
      }
      throw new IllegalStateException("No calculation has been started yet");
    }

    // The best move and evaluation found so far.
    int bestEvaluation =
        this.currentPlayerColor == Color.WHITE ? Integer.MIN_VALUE : Integer.MAX_VALUE;
    int bestDepth = 0;
    Move bestMove = this.activeEvaluators.getFirst().getBestMove();

    // Iterates through all Threads and searches for the move with the best evaluation.
    for (Evaluator evaluator : this.activeEvaluators) {
      int evaluation = evaluator.getBestEvaluation();

      boolean better;
      if (evaluator instanceof AlphaBetaEvaluator alphaBetaEvaluator) {
        better = isBetterResult(evaluation, alphaBetaEvaluator.getSearchDepth(), bestEvaluation,
            bestDepth);
        if (better) {
          bestDepth = alphaBetaEvaluator.getSearchDepth();
        }
      } else {
        better = ((this.currentPlayerColor == Color.WHITE && evaluation > bestEvaluation) || (
            this.currentPlayerColor == Color.BLACK && evaluation < bestEvaluation));
      }
      if (better) {
        bestEvaluation = evaluation;
        bestMove = evaluator.getBestMove();
      }
    }

    this.bestMove = bestMove;
    return bestMove;
  }

  // Only used in case alpha-beta pruning is used as algorithm
  private boolean isBetterResult(int candidateEval, int candidateDepth, int bestEval,
      int bestDepth) {
    final int DECISIVE = Integer.MAX_VALUE / 2;
    boolean candidateDecisive = Math.abs(candidateEval) >= DECISIVE;
    boolean bestDecisive = Math.abs(bestEval) >= DECISIVE;

    if (candidateDecisive != bestDecisive) {
      if (!candidateDecisive) {
        return false;
      }
      return this.currentPlayerColor == Color.WHITE ? candidateEval > bestEval
          : candidateEval < bestEval;
    }
    if (candidateDepth != bestDepth) {
      return candidateDepth > bestDepth;
    }
    return this.currentPlayerColor == Color.WHITE ? candidateEval > bestEval
        : candidateEval < bestEval;
  }
}