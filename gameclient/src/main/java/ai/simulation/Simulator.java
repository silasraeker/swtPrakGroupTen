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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulates games between different AI implementations and records their results. The configured
 * factories play against each other repeatedly. Colors are assigned randomly for every game to
 * avoid giving one implementation a permanent advantage.
 */
public class Simulator {

  /**
   * Logger used for recording simulation results.
   */
  private static final Logger logger = LoggerFactory.getLogger(Simulator.class);

  // Static parameters to configure
  /**
   * Calculation times in seconds used for each simulation.
   */
  public static final int[] SECONDS = new int[]{5};

  /**
   * AI factories to compare during the simulation.
   */
  public static final Factory[] TO_SIMULATE = new Factory[]{new MCTSFactory(),
      new AlphaBetaFactory()};

  /**
   * Duration of the simulation in minutes. After reaching this duration, the current game will
   * finish before the simulation stops.
   */
  public static final int SIMULATING_MINUTES = 5;

  /**
   * Whether additional information about the simulation should be printed.
   */
  public static final boolean VERBOSE = true;

  /**
   * Starts the simulation of games between the configured AI implementations.
   *
   * @throws InterruptedException if the simulation thread is interrupted
   */
  public static void main(String[] args) throws InterruptedException {
    final long simulationStartMillis = System.currentTimeMillis();
    /*
     * winMatrix[winner][loser] contains the number of games won by the factory at
     * index "winner" against the factory at index "loser".
     */
    final int[][] winMatrix = new int[TO_SIMULATE.length][TO_SIMULATE.length];
    final Random random = new Random();

    // Keep some CPU capacity available for the operating system and other processes.
    int threadCount = Runtime.getRuntime().availableProcessors() - 1;
    if (threadCount < 1) {
      threadCount = 1;
    }

    // Repeat complete comparison rounds until the simulation time is reached.
    for (int iteration = 1;
        (int) ((System.currentTimeMillis() - simulationStartMillis) / 60000) < SIMULATING_MINUTES;
        iteration++) {

      // Run games for every configured thinking time.
      for (final int seconds : SECONDS) {
        // Compare every factory with every other factory exactly once per iteration.
        for (int firstFactoryIndex = 0; firstFactoryIndex < TO_SIMULATE.length - 1;
            firstFactoryIndex++) {
          for (int secondFactoryIndex = firstFactoryIndex + 1;
              secondFactoryIndex < TO_SIMULATE.length; secondFactoryIndex++) {

            // Randomly decide which factory plays White and which one plays Black.
            boolean colorsSwitched = random.nextInt(2) == 0;
            Factory whiteFactory =
                colorsSwitched ? TO_SIMULATE[secondFactoryIndex] : TO_SIMULATE[firstFactoryIndex];
            Factory blackFactory =
                colorsSwitched ? TO_SIMULATE[firstFactoryIndex] : TO_SIMULATE[secondFactoryIndex];

            // Create one AI client for each player.
            final KIClient whitePlayer = new KIClient(whiteFactory, threadCount);
            final KIClient blackPlayer = new KIClient(blackFactory, threadCount);

            // Initialize a new standard Amazons game.
            Content[][] field = Simulator.createDefault8x8Game();
            Move lastMove = null;
            Color currentPlayerColor = Color.WHITE;

            if (VERBOSE) {
              System.out.println("Starting game: " + whiteFactory + " is White and " + blackFactory
                  + " is Black. Thinking time: " + seconds + " seconds.");
            }

            // Continue until the current player has no legal move left.
            while (!Simulator.isGameOver(currentPlayerColor, field)) {
              if (VERBOSE) {
                // Print the current board state and a simple position evaluation.
                Testbench.printGame(field, lastMove);
                System.out.println(
                    "Current position evaluation: " + EvaluatorHelper.evaluatePosition(field));
              }

              // Select the AI client belonging to the player whose turn it is.
              KIClient currentKiClient =
                  currentPlayerColor == Color.WHITE ? whitePlayer : blackPlayer;

              // Start asynchronous move calculation.
              currentKiClient.startCalculating(field, seconds * 1000L, currentPlayerColor);
              if (VERBOSE) {
                System.out.println("Started calculating.");
              }
              // Store the time to verify that the thinking time is respected.
              long calculationStartMillis = System.currentTimeMillis();

              // A random AI does not need the full configured thinking time. It only receives a short delay so it has enough time to generate a move.
              Factory currentFactory =
                  currentPlayerColor == Color.WHITE ? whiteFactory : blackFactory;
              if (currentFactory instanceof RandomFactory) {
                Thread.sleep(10);
              } else {
                Thread.sleep(seconds * 1000L);
              }

              // Retrieve the best move found so far and stop the searching threads.
              Move bestMove = currentKiClient.getBestMove();
              currentKiClient.stopCalculating();
              int calculationSeconds = (int) ((System.currentTimeMillis() - calculationStartMillis)
                  / 1000);

              if (VERBOSE) {
                System.out.println("Stopped calculating after " + calculationSeconds + " seconds.");
              }

              // Apply the move and switch to the other player.
              EvaluatorHelper.makeMove(field, bestMove);
              lastMove = bestMove;
              currentPlayerColor = currentPlayerColor == Color.WHITE ? Color.BLACK : Color.WHITE;
            }

            //The player whose turn it currently is has lost, because that player has no legal move left.
            // colorsSwitched is used to map the winner back to the original factory indices in TO_SIMULATE.
            if ((currentPlayerColor == Color.WHITE && colorsSwitched) || (
                currentPlayerColor == Color.BLACK && !colorsSwitched)) {
              winMatrix[firstFactoryIndex][secondFactoryIndex]++;
            } else {
              winMatrix[secondFactoryIndex][firstFactoryIndex]++;
            }
            if (VERBOSE) {
              Testbench.printGame(field, lastMove);
              String winner = currentPlayerColor == Color.WHITE
                  ? "Black (" + blackFactory + ")"
                  : "White (" + whiteFactory + ")";
              System.out.println("Game over! " + winner + " has won.");
            }

            // Format the current win matrix as a table.
            StringBuilder result = new StringBuilder(
                "\nResults after iteration " + iteration + "\n");
            String columnWidth = "  ";

            // Determine the required width based on the longest factory name.
            for (Factory factory : TO_SIMULATE) {
              if (factory.toString().length() > columnWidth.length()) {
                columnWidth = " ".repeat(factory.toString().length());
              }
            }

            columnWidth += " ";

            // Create the table header.
            result.append(columnWidth);
            for (int columnIndex = 0; columnIndex < winMatrix.length; columnIndex++) {
              int padding =
                  Math.max(0, columnWidth.length() - TO_SIMULATE[columnIndex].toString().length())
                      / 2;

              result.repeat(" ", padding)
                  .append(TO_SIMULATE[columnIndex])
                  .repeat(" ", padding);
            }
            result.append("\n");

            // Create every row of the win matrix.
            for (int rowIndex = 0; rowIndex < winMatrix.length; rowIndex++) {
              int leftPadding =
                  Math.max(0, columnWidth.length() - TO_SIMULATE[rowIndex].toString().length()) / 2;
              int rightPadding = (int) Math.max(0, Math.ceil(
                  (columnWidth.length() - TO_SIMULATE[rowIndex].toString().length()) / 2.0));

              result.repeat(" ", leftPadding)
                  .append(TO_SIMULATE[rowIndex])
                  .repeat(" ", rightPadding);

              for (int columnIndex = 0; columnIndex < winMatrix.length; columnIndex++) {
                // A factory is not simulated against itself, so the diagonal is marked with '\'.
                String valueToPrint = columnIndex == rowIndex ? "\\"
                    : String.valueOf(winMatrix[rowIndex][columnIndex]);
                int padding = (columnWidth.length() - valueToPrint.length()) / 2;
                if (padding < 0) {
                  padding = 0;
                }
                result.repeat(" ", padding).append(valueToPrint).repeat(" ", padding);
              }
              result.append("\n");
            }

            // Print & Log Simulation Results
            System.out.println(result);
            logger.info(result.toString());
          }
        }
      }
    }
  }

  /**
   * Creates the standard Amazons starting position on a 10x10 board.
   *
   * @return a new 10x10 game board containing the initial Amazon positions
   */
  public static Content[][] createDefault10x10Game() {
    Content[][] field = new Content[10][10];

    // Initialize every board cell as empty.
    for (Content[] line : field) {
      Arrays.fill(line, Content.EMPTY);
    }

    // Place the black Amazons.
    field[0][3] = Content.BLACK_AMAZONE;
    field[0][6] = Content.BLACK_AMAZONE;
    field[3][0] = Content.BLACK_AMAZONE;
    field[3][9] = Content.BLACK_AMAZONE;

    // Place the white Amazons.
    field[9][3] = Content.WHITE_AMAZONE;
    field[9][6] = Content.WHITE_AMAZONE;
    field[6][9] = Content.WHITE_AMAZONE;
    field[6][0] = Content.WHITE_AMAZONE;

    return field;
  }

  /**
   * Creates the standard Amazons starting position on an 8x8 board.
   *
   * @return a new 8x8 game board containing the initial Amazon positions
   */
  public static Content[][] createDefault8x8Game() {
    Content[][] field = new Content[8][8];

    // Initialize every board cell as empty.
    for (Content[] line : field) {
      Arrays.fill(line, Content.EMPTY);
    }

    // Place the black Amazons.
    field[0][2] = Content.BLACK_AMAZONE;
    field[0][5] = Content.BLACK_AMAZONE;
    field[2][0] = Content.BLACK_AMAZONE;
    field[2][7] = Content.BLACK_AMAZONE;

    // Place the white Amazons.
    field[7][2] = Content.WHITE_AMAZONE;
    field[7][5] = Content.WHITE_AMAZONE;
    field[5][7] = Content.WHITE_AMAZONE;
    field[5][0] = Content.WHITE_AMAZONE;

    return field;
  }

  /**
   * Checks whether the game is over.
   *
   * @param currentPlayerColor the color of the player whose turn it is
   * @param field              the current game board
   * @return {@code true} if the current player has no legal moves left
   */
  public static boolean isGameOver(Color currentPlayerColor, Content[][] field) {
    return EvaluatorHelper.countAllMovesAndArrows(field, currentPlayerColor) == 0;
  }
}
