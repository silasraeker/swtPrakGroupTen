package ai.engine;

import ai.factories.AlphaBetaFactory;
import client.game.Content;
import client.game.Move;
import client.game.Player;
import ai.helper.EvaluatorHelper;
import client.game.Player.Color;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents an engine whose calculation process can be started and stopped. At any point in time,
 * it can provide the best move found so far.
 */
public final class KIClient {

  public enum Difficulty { EASY, MEDIUM, HARD }

  /**
   * The number of threads that can be used during the calculation process. Must be at least one.
   */
  private final int numberOfThreads;

  /**
   * the color of the player whose turn it is
   */
  private Color playerColor;

  /**
   * The factory used to create evaluators.
   */
  private final Factory factory;

  /**
   * A List of Evaluators, which are either running or ready to run.
   */
  private List<Evaluator> evaluators = new LinkedList<>();

  /**
   * the best move found so far according to the calculations
   */
  private volatile Move best_move;

  public KIClient(final Factory factory, final int numberOfThreads) {
    super();

    if (numberOfThreads < 1) {
      throw new IllegalArgumentException(
          "The number of Threads (" + numberOfThreads + ") can't be less than 1!");
    } else if (factory == null) {
      throw new IllegalArgumentException("The parameter 'factory' is null");
    }
    this.factory = factory;
    this.numberOfThreads = numberOfThreads;
  }

  public KIClient(Difficulty difficulty){
    this(new AlphaBetaFactory(), (int) (Runtime.getRuntime().availableProcessors() * (5.0 / 6.0)));
  }

  /**
   * Starts the calculation process.
   *
   * @param field          the field for which the best move is to be calculated
   * @param expectedMillis the amount of time the AI client is expected to calculate before being
   *                       stopped.
   * @param playerColor    the color of the player whose turn it is
   */
  public void startCalculating(final Content[][] field, final long expectedMillis,
      final Color playerColor) {
    if (field == null) {
      throw new IllegalArgumentException("The parameter 'field' is null");
    } else if (playerColor == null) {
      throw new IllegalArgumentException("The parameter 'playerColor' is null");
    }

    // Make sure that all Threads were stopped correctly
    if (!this.evaluators.isEmpty()) {
      throw new IllegalStateException("The last Calculation Process hasn't been stopped yet");
    }

    this.playerColor = playerColor;
    this.best_move = null;
    List<Move> moves = EvaluatorHelper.getAllPossibleMoves(field, playerColor);
    Iterator<Move> iter = moves.iterator();

    // Make sure that there are moves to evaluate
    if (moves.isEmpty()) {
      throw new IllegalArgumentException("No legal moves available for evaluation");
    }

    // Distributes all possibles moves to different Threads
    int numberOfEvaluators = this.numberOfThreads;
    double moves_per_threads = moves.size() / (double) numberOfEvaluators;
    if (moves_per_threads < 1) {
      moves_per_threads = 1;
      numberOfEvaluators = moves.size();
    }
    int moveCounter = 0;
    for (int i = 0; i < numberOfEvaluators; i++) {
      List<Move> evaluator_moves = new LinkedList<>();

      for (int j = (int) moves_per_threads * i; j < (int) moves_per_threads * (i + 1); j++) {
        evaluator_moves.add(iter.next());
      }

      //Adds the remaining moves to the last Thread
      if (i == numberOfEvaluators - 1) {
        while (iter.hasNext()) {
          evaluator_moves.add(iter.next());
        }
      }

      moveCounter += evaluator_moves.size();
      Evaluator evaluator = this.factory.createInstance(playerColor, EvaluatorHelper.copyField(field),
          evaluator_moves);
      this.evaluators.add(evaluator);
      evaluator.start();
    }

    if (moveCounter != moves.size()) {
      throw new IllegalStateException("Not all moves have been distributed");
    }
    if (evaluators.isEmpty()) {
      throw new IllegalStateException("No Evaluators were created");
    }
  }

  /**
   * Stops the calculation process and all running threads.
   */
  public void stopCalculating() {
    // Copies the running Threads into a local copy in order to make room for the start of a new calculation process
    List<Evaluator> to_be_stopped = this.evaluators;
    this.evaluators = new LinkedList<>();

    // Signals all Threads to stop
    for (Evaluator evaluator : to_be_stopped) {
      evaluator.interrupt();
    }

    // Waits for the remaining active Threads and empties the List. Makes sure all Threads are ending correctly
    while (!to_be_stopped.isEmpty()) {
      Evaluator first = to_be_stopped.getFirst();
      while (first.isAlive()) {
        try {
          first.join(2000);
          //TODO add Log entry, a thread shouldn't take this long to die
        } catch (InterruptedException e) {
          throw new IllegalStateException("sollte nd passieren");
        }
      }
      to_be_stopped.removeFirst();
    }
  }

  /**
   * Searches all threads for the move with the best evaluation. Due to multithreading, there may be
   * a slight delay. Under normal circumstances this is negligible, but in case of a multithreading
   * issue (e.g. a deadlock), this method may take longer than expected.
   *
   * @return the best move found so far according to the calculations
   */
  public Move getBestMove() {
    int best_evaluation = this.playerColor == Color.WHITE ? Integer.MIN_VALUE : Integer.MAX_VALUE;
    Move best_move = null;

    if (this.evaluators.isEmpty()) {
      if (this.best_move != null) {
        return this.best_move;
      }
      throw new IllegalStateException("There hasn't been done calculations yet");
    }

    //Iterates through all Threads and searches for the move with the best evaluation
    for (Evaluator evaluator : this.evaluators) {
      int evaluation = evaluator.getBestEvaluation();

      if ((this.playerColor == Color.WHITE && evaluation > best_evaluation) || (
          this.playerColor == Color.BLACK && evaluation < best_evaluation)) {
        best_evaluation = evaluation;
        //There is a small Chance, that between getting the evaluation and the best move, the Thread found a better Move.
        // In that Case that move now is associated with a (most likely slight) worse evaluation
        best_move = evaluator.getBestMove();
      }
    }

    this.best_move = best_move;
    return best_move;
  }
}
