package ai.simulation;

import ai.factories.MCTSFactory;
import ai.helper.EvaluatorHelper;
import ai.engine.KIClient;
import client.game.Content;
import client.game.Move;
import client.game.Player.Color;
import client.game.Position;
import java.util.Scanner;

/**
 * Provides a command-line test environment for playing Amazons against an AI. Each color can be
 * controlled either by a human player or by the MCTS-based AI. Human players enter their moves
 * through the console, while AI players receive a configurable thinking time per move.
 */
public class Testbench {

  /**
   * Starts an interactive Amazons game in the console.
   *
   * @throws InterruptedException if the current thread is interrupted while waiting for the AI
   */
  public static void main(String[] args) throws InterruptedException {
    // Reserve some CPU capacity for the operating system and other processes.
    int threadCount = Runtime.getRuntime().availableProcessors() - 1;
    if (threadCount < 1) {
      threadCount = 1;
    }
    System.out.println("Number of threads used: " + threadCount);

    // Create the AI client used for every computer-controlled player.
    KIClient aiClient = new KIClient(new MCTSFactory(), threadCount);

    // Create the initial Amazons board.
    Content[][] field = Simulator.createDefault8x8Game();

    Scanner scanner = new Scanner(System.in);

    // Configure whether White is controlled by a human or by the AI.
    boolean isWhiteHuman;
    int whiteThinkingSeconds = -1;
    System.out.println("Should the White player be human-controlled (y/n)?");
    do {
      String input = scanner.nextLine();

      if (input.trim().equalsIgnoreCase("y")) {
        isWhiteHuman = true;
        break;
      } else if (input.trim().equalsIgnoreCase("n")) {
        isWhiteHuman = false;

        // Ask for the AI thinking time until a valid integer is entered.
        do {
          System.out.println("How many seconds should the computer think per move?");
          input = scanner.nextLine();

          try {
            whiteThinkingSeconds = Integer.parseInt(input);
            break;
          } catch (NumberFormatException e) {
            System.out.println("The input could not be recognized.");
          }
        } while (true);

        break;
      } else {
        System.out.println("The input could not be recognized.");
      }
    } while (true);

    // Configure whether Black is controlled by a human or by the AI.
    boolean isBlackHuman;
    int blackThinkingSeconds = -1;
    System.out.println("Should the Black player be human-controlled (y/n)?");
    do {
      String input = scanner.nextLine();

      if (input.trim().equalsIgnoreCase("y")) {
        isBlackHuman = true;
        break;
      } else if (input.trim().equalsIgnoreCase("n")) {
        isBlackHuman = false;

        // Ask for the AI thinking time until a valid integer is entered.
        do {
          System.out.println("How many seconds should the computer think per move?");
          input = scanner.nextLine();

          try {
            blackThinkingSeconds = Integer.parseInt(input);
            break;
          } catch (NumberFormatException exception) {
            System.out.println("The input could not be recognized.");
          }
        } while (true);

        break;
      } else {
        System.out.println("The input could not be recognized.");
      }
    } while (true);

    // White always starts an Amazons game.
    Color currentPlayerColor = Color.WHITE;
    Move lastMove = null;

    // Continue until the application is manually terminated.
    while (true) {
      printGame(field, lastMove);
      System.out.println("Current position evaluation: " + EvaluatorHelper.evaluatePosition(field));

      boolean isCurrentPlayerHuman =
          (currentPlayerColor == Color.WHITE && isWhiteHuman) || (currentPlayerColor == Color.BLACK
              && isBlackHuman);

      if (isCurrentPlayerHuman) {
        // A move consists of six coordinates: start_y start_x target_y target_x arrow_y arrow_x
        System.out.println(
            "Enter your move (format: start_y start_x target_y target_x arrow_y arrow_x):");
        do {
          try {
            String[] inputParts = scanner.nextLine().split(" ");
            Move move = new Move(
                new Position(Integer.parseInt(inputParts[0]), Integer.parseInt(inputParts[1])),
                new Position(Integer.parseInt(inputParts[2]), Integer.parseInt(inputParts[3])),
                new Position(Integer.parseInt(inputParts[4]), Integer.parseInt(inputParts[5])));

            // Apply the entered move. Invalid moves are rejected by makeMove if applicable.
            EvaluatorHelper.makeMove(field, move);
            lastMove = move;
            break;
          } catch (Exception exception) {
            System.out.println("Invalid move: " + exception.getMessage());
          }
        } while (true);
      } else {
        long thinkingMillis =
            (currentPlayerColor == Color.WHITE ? whiteThinkingSeconds : blackThinkingSeconds)
                * 1000L;

        // Start asynchronous move calculation for the computer-controlled player.
        aiClient.startCalculating(field, thinkingMillis, currentPlayerColor);
        System.out.println("Started calculating.");

        long calculationStartMillis = System.currentTimeMillis();

        // Give the AI the configured amount of time to search for a move.
        Thread.sleep(thinkingMillis);

        Move best = aiClient.getBestMove();
        aiClient.stopCalculating();

        int calculationSeconds = (int) ((System.currentTimeMillis() - calculationStartMillis)
            / 1000);
        System.out.println("Stopped calculating after " + calculationSeconds + " seconds.");

        // Apply the best move found by the AI.
        EvaluatorHelper.makeMove(field, best);
        lastMove = best;
      }
      // Alternate turns between White and Black.
      currentPlayerColor = currentPlayerColor == Color.WHITE ? Color.BLACK : Color.WHITE;
    }
  }


  /**
   * Prints the current board state to the console using ANSI colors. The most recent move is
   * highlighted as follows: Red: the Amazon's starting position; Green: the Amazon's target
   * position and the arrow position
   *
   * @param field    the game board to print
   * @param lastMove the most recent move, or null if no move has been played yet
   */
  public static void printGame(Content[][] field, Move lastMove) {
    // Print column coordinates.
    System.out.print("   ");
    for (int column = 0; column < field.length; column++) {
      System.out.print(column + "  ");
    }
    System.out.println();

    // Print each row together with its row coordinate.
    for (int row = 0; row < field.length; row++) {
      System.out.print(row + "  ");

      for (int column = 0; column < field[row].length; column++) {
        String color = "";

        // Convert the board content into a printable symbol.
        String symbol = switch (field[row][column]) {
          case Content.WHITE_AMAZONE -> {
            color = "\u001B[36m";
            yield "W";
          }
          case Content.BLACK_AMAZONE -> {
            color = "\u001B[36m";
            yield "B";
          }
          case Content.ARROW -> "×";
          default -> {
            color = "\u001B[37m";
            yield "_";
          }
        };

        // Highlight the start, target, and arrow positions of the latest move.
        if (lastMove != null) {
          if (column == lastMove.arrow().y() && row == lastMove.arrow().x()) {
            color = "\u001B[32m";
          }

          if (column == lastMove.start().y() && row == lastMove.start().x()) {
            color = "\u001B[31m";
          }

          if (column == lastMove.to().y() && row == lastMove.to().x()) {
            color = "\u001B[32m";
          }
        }

        // Reset ANSI formatting after every board cell.
        System.out.print(color + symbol + "\u001B[0m" + "  ");
      }

      System.out.println();
    }
  }
}