package ai.simulation;

import ai.engine.Factory;
import ai.engine.KIClient;
import ai.factories.AlphaBetaFactory;
import ai.factories.MCTSFactory;
import ai.factories.RandomFactory;
import client.game.Content;
import client.game.Move;
import client.game.Player.Color;
import ai.helper.EvaluatorHelper;
import java.util.Arrays;
import java.util.Random;

/**
 * Evaluates different implementations of {@code Evaluator} by letting them play against each
 * other.
 */
public class Simulator {

  public static final int[] SECONDS = new int[]{5};
  public static final Factory[] TO_SIMULATE = new Factory[]{new MCTSFactory(),
      new AlphaBetaFactory()};

  public static final int MIN_SIMULATING_MINUTES = 5;
  public static final boolean VERBOSE = true;

  public static void main(String[] args) throws InterruptedException {
    final long startedMillis = System.currentTimeMillis();
    final int[][] wins = new int[TO_SIMULATE.length][TO_SIMULATE.length];
    final Random random = new Random();
    int numberOfThreads = (int) (Runtime.getRuntime().availableProcessors() * (5.0 / 6.0));
    if (numberOfThreads < 1) {
      numberOfThreads = 1;
    }
    System.out.println("Anzahl an verwendeten Threads wird " + numberOfThreads
        + " seien");//Just for debugging purpose

    for (int iteration = 1;
        (int) ((System.currentTimeMillis() - startedMillis) / 60000) < MIN_SIMULATING_MINUTES;
        iteration++) {
      for (int seconds_index = 0; seconds_index < SECONDS.length; seconds_index++) {
        final int seconds = SECONDS[seconds_index];

        for (int first_index = 0; first_index < TO_SIMULATE.length - 1; first_index++) {
          for (int second_index = first_index + 1; second_index < TO_SIMULATE.length;
              second_index++) {
            // Randomly choose which Factory is White and which is Black.
            boolean switchColor = random.nextInt(2) == 0;
            Factory whiteFactory =
                switchColor ? whiteFactory = TO_SIMULATE[second_index] : TO_SIMULATE[first_index];
            Factory blackFactory =
                switchColor ? blackFactory = TO_SIMULATE[first_index] : TO_SIMULATE[second_index];

            final KIClient whitePlayer = new KIClient(whiteFactory, numberOfThreads);
            final KIClient blackPlayer = new KIClient(blackFactory, numberOfThreads);

            // Simulating an entire game of Amazones
            Content[][] field = Simulator.createDefaultGame();
            Move last = null;
            Color currentPlayerColor = Color.WHITE;

            while (!Simulator.isGameOver(currentPlayerColor, field)) {
              if (VERBOSE) {
                // Prints the current Position with (simple) Evaluation
                Testbench.printGame(field, last);
                System.out.println(
                    "Bewertung der aktuellen Stellung: " + EvaluatorHelper.evaluatePosition(field));
              }

              KIClient currentKiClient = currentPlayerColor == Color.WHITE ? whitePlayer : blackPlayer;
              currentKiClient.startCalculating(field, 0, currentPlayerColor);

              if (VERBOSE) {
                System.out.println("Starts Calculating");
              }
              long startedCalculation = System.currentTimeMillis(); // For debugging purposes, verify that the time limit is respected.
              if ((currentPlayerColor == Color.WHITE ? whiteFactory
                  : blackFactory) instanceof RandomFactory) {
                Thread.sleep(10);
              } else {
                Thread.sleep(seconds * 1000L);
              }
              Move best = currentKiClient.getBestMove();

              int took = (int) ((System.currentTimeMillis() - startedCalculation) / 1000);
              currentKiClient.stopCalculating();
              if (VERBOSE) {
                System.out.println("Stops Calculating, took " + took + " Seconds");
              }

              EvaluatorHelper.makeMove(field, best);
              last = best;
              currentPlayerColor = currentPlayerColor == Color.WHITE ? Color.BLACK : Color.WHITE;
            }

            // Game is over!
            if ((currentPlayerColor == Color.WHITE && switchColor) || (currentPlayerColor == Color.BLACK
                && !switchColor)) {
              wins[first_index][second_index]++;
            } else {
              wins[second_index][first_index]++;
            }
            if (VERBOSE) {
              System.out.println(
                  "Game is over! " + (currentPlayerColor == Color.WHITE ? "Schwarz("+blackFactory.toString() : "Weiß("+whitePlayer.toString())
                      + ") hat gewonnen!");
            }

            // Print Simulation Results
            System.out.println("\nErgebnisse in der " + iteration + "-ten Iteration");
            String whiteSpace = "  ";

            for (Factory factory : TO_SIMULATE) {
              if (factory.toString().length() > whiteSpace.length()) {
                whiteSpace = " ".repeat(factory.toString().length());
              }
            }

            System.out.print(" " + whiteSpace);
            for (int x = 0; x < wins.length; x++) {
              System.out.print(TO_SIMULATE[x] + whiteSpace);
            }
            System.out.println();
            for (int y = 0; y < wins.length; y++) {
              System.out.print(TO_SIMULATE[y] + "  ");
              for (int x = 0; x < wins.length; x++) {
                String to_print = x == y ? "\\" : String.valueOf(wins[y][x]);
                int padding = ((int) ((whiteSpace.length() - to_print.length()) / 2));
                if (padding < 0) {
                  padding = 0;
                }
                System.out.print(" ".repeat(padding) + to_print + " ".repeat(padding) + whiteSpace);

              }
              System.out.println();
            }
          }
        }
      }
    }
  }

  /**
   * @return the 10x10 starting Grid
   */
  public static Content[][] createDefaultGame() {
    Content[][] field = new Content[10][10];
    for (Content[] line : field) {
      Arrays.fill(line, Content.EMPTY);
    }

    field[0][3] = Content.BLACK_AMAZONE;
    field[0][6] = Content.BLACK_AMAZONE;
    field[3][0] = Content.BLACK_AMAZONE;
    field[3][9] = Content.BLACK_AMAZONE;

    field[9][3] = Content.WHITE_AMAZONE;
    field[9][6] = Content.WHITE_AMAZONE;
    field[6][9] = Content.WHITE_AMAZONE;
    field[6][0] = Content.WHITE_AMAZONE;

    return field;
  }

  /**
   * @return whether the game is finished
   */
  public static boolean isGameOver(Color currentPlayerColor, Content[][] field) {
    return EvaluatorHelper.countAllPossibleMoves(field, currentPlayerColor) == 0;
  }
}
