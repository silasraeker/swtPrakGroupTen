package ai.simulation;

import ai.factories.MCTSFactory;
import ai.helper.EvaluatorHelper;
import ai.engine.KIClient;
import client.game.Content;
import client.game.Move;
import client.game.Player.Color;
import client.game.Position;
import java.util.Scanner;

public class Testbench {

  public static void main(String[] args) throws InterruptedException {
    int size = 10, numberOfThreads = (int) (Runtime.getRuntime().availableProcessors() * (5.0
        / 6.0));
    if (numberOfThreads < 1) {
      numberOfThreads = 1;
    }
    System.out.println("Anzahl an verwendeten Threads wird " + numberOfThreads
        + " seien");//Just for debugging purpose
    KIClient ki = new KIClient(new MCTSFactory(), numberOfThreads);
    Content[][] field = Simulator.createDefaultGame();

    Scanner scanner = new Scanner(System.in);

    // Determine whether the players are controlled by a human or the AI.
    boolean isWhiteHuman;
    int whiteSeconds = -1;
    System.out.println("Soll der weiße Spieler menschlich seien (y/n)?");
    do {
      String input = scanner.nextLine();
      if (input.trim().equalsIgnoreCase("y")) {
        isWhiteHuman = true;
        break;
      } else if (input.trim().equalsIgnoreCase("n")) {
        isWhiteHuman = false;
        do {
          System.out.println("Wie viele Sekunden Bedenkzeit soll der Computer haben?");
          input = scanner.nextLine();
          try {
            whiteSeconds = Integer.parseInt(input);
            break;
          } catch (Exception e) {
            System.out.println("Die Eingabe konnte nicht erkannt werden");
          }
        } while (true);
        break;
      } else {
        System.out.println("Eingabe konnte nicht erkannt werden");
      }
    } while (true);

    boolean isBlackHuman;
    int blackSeconds = -1;
    System.out.println("Soll der schwarze Spieler menschlich seien (y/n)?");
    do {
      String input = scanner.nextLine();
      if (input.trim().equalsIgnoreCase("y")) {
        isBlackHuman = true;
        break;
      } else if (input.trim().equalsIgnoreCase("n")) {
        isBlackHuman = false;
        System.out.println("Wie viele Sekunden Bedenkzeit soll der Computer haben?");
        input = scanner.nextLine();
        try {
          blackSeconds = Integer.parseInt(input);
          break;
        } catch (Exception e) {
          System.out.println("Die Eingabe konnte nicht erkannt werden");
        }
        break;
      } else {
        System.out.println("Eingabe konnte nicht erkannt werden, input");
      }
    } while (true);

    //Plays the Game
    Color currentPlayerColor = Color.WHITE;
    Move last = null;
    while (true) {
      printGame(field, last);
      System.out.println(
          "Bewertung der aktuellen Stellung: " + EvaluatorHelper.evaluatePosition(field));

      if ((currentPlayerColor == Color.WHITE && isWhiteHuman) || (currentPlayerColor == Color.BLACK
          && isBlackHuman)) {
        System.out.println(
            "Gebe deinen Zug ein (Format: start_y start_x to_y to_x arrow_y arrow_x)");
        do {
          try {
            String[] userinput = scanner.nextLine().split(" ");
            Move move = new Move(
                new Position(Integer.parseInt(userinput[0]), Integer.parseInt(userinput[1])),
                new Position(Integer.parseInt(userinput[2]), Integer.parseInt(userinput[3])),
                new Position(Integer.parseInt(userinput[4]), Integer.parseInt(userinput[5])));

            EvaluatorHelper.makeMove(field, move);
            last = move;
          } catch (Exception e) {
            System.out.println(e);
            continue;
          }
          break;
        } while (true);
      } else {
        ki.startCalculating(field, 0 , currentPlayerColor);
        System.out.println("Starts Calculating");
        long startedMillis = System.currentTimeMillis(); // For debugging purposes, verify that the time limit is respected.

        Thread.sleep((currentPlayerColor == Color.WHITE ? whiteSeconds : blackSeconds) * 1000L);
        Move best = ki.getBestMove();

        int took = (int) ((System.currentTimeMillis() - startedMillis) / 1000);
        System.out.println("Stops Calculating, took " + took + " Seconds");
        ki.stopCalculating();

        EvaluatorHelper.makeMove(field, best);
        last = best;
      }
      currentPlayerColor = currentPlayerColor == Color.WHITE ? Color.BLACK : Color.WHITE;
    }
  }


  /**
   * Prints a position.
   * @param field to be printed
   * @param last_move to be highlighted
   */
  public static void printGame(Content[][] field, Move last_move) {
    System.out.print("   ");
    for (int x = 0; x < field.length; x++) {
      System.out.print(x + "  ");
    }
    System.out.println();
    for (int y = 0; y < field.length; y++) {
      System.out.print(y + "  ");
      for (int x = 0; x < field[y].length; x++) {
        String print;
        String color = "";
        print = switch (field[y][x]) {
          case Content.WHITE_AMAZONE -> {
            color = "\u001B[36m";
            yield "W";
          }
          case Content.BLACK_AMAZONE -> {
            color = "\u001B[36m";
            yield "B";
          }
          case Content.ARROW -> {
            //color = "\u001B[37m";
            yield "×";
          }
          default -> {
            color = "\u001B[37m";
            yield "_";
          }
        };

        if (last_move != null) {
          if (x == last_move.arrow().y() && y == last_move.arrow().x()) {
            color = "\u001B[32m";
          }
          if (x == last_move.start().y() && y == last_move.start().x()) {
            color = "\u001B[31m";
          }
          if (x == last_move.to().y() && y == last_move.to().x()) {
            color = "\u001B[32m";
          }
        }

        System.out.print(color + print + "\u001B[0m" + "  ");
      }
      System.out.println();
    }
  }
}