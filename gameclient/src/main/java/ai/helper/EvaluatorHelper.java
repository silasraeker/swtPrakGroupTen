package ai.helper;

import ai.evaluators.AlphaBetaEvaluator;
import client.game.Content;
import client.game.Move;
import client.game.Player.Color;
import client.game.Position;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Utility class providing static helper methods for {@code Evaluator}.
 */
public final class EvaluatorHelper {

  private EvaluatorHelper() {
    throw new IllegalStateException("There should be no instance of this class");
  }

  private static final int[] knight_x = {1, 1, -1, -1, 2, 2, -2, -2};
  private static final int[] knight_y = {2, -2, 2, -2, 1, -1, 1, -1};
  private static final int[] queen_x = {0, 1, 1, 1, 0, -1, -1, -1};
  private static final int[] queen_y = {1, 1, 0, -1, -1, -1, 0, 1};


  /**
   * Executes the given move and applies the corresponding changes to the field. This method
   * modifies the contents of the array and does not create a copy.
   *
   * @param field the field to modify
   * @param move  the move to execute
   */
  public static void makeMove(Content[][] field, Move move) {
    if (field[move.start().x()][move.start().y()] == Content.EMPTY
        || field[move.start().x()][move.start().y()] == Content.ARROW) {
      throw new IllegalArgumentException(
          "Invalid move: Content of 'start' cannot be arrow or empty.");
    }
    if (field[move.to().x()][move.to().y()] != Content.EMPTY) {
      throw new IllegalArgumentException("Invalid move: Content of 'to' must be empty.");
    }
    if ((field[move.arrow().x()][move.arrow().y()] != Content.EMPTY)
        && (move.arrow().x() != move.start().x() || move.arrow().y() != move.start().y())) {
      throw new IllegalArgumentException("Invalid move: Content of 'arrow' must be empty.");
    }
    // upper code may be removed, if we deem exceptions to be unnecessary (class 'Game' can gladly copy it tho :) )
    field[move.to().x()][move.to().y()] = field[move.start().x()][move.start().y()];
    field[move.start().x()][move.start().y()] = Content.EMPTY;
    field[move.arrow().x()][move.arrow().y()] = Content.ARROW;
  }

  /**
   * Reverses the given move and applies the corresponding changes to the field. This method
   * modifies the contents of the array and does not create a copy.
   *
   * @param field the field to modify
   * @param move  the move to reverse
   */
  public static void reverseMove(Content[][] field, Move move) {
    if (field[move.to().x()][move.to().y()] == Content.EMPTY
        || field[move.to().x()][move.to().y()] == Content.ARROW) {
      throw new IllegalArgumentException(
          "Invalid move: Content of 'to' cannot be arrow or empty.");
    }
    if (field[move.arrow().x()][move.arrow().y()] != Content.ARROW) {
      throw new IllegalArgumentException("Invalid move: Content of 'arrow' must be arrow.");
    }
    if ((field[move.start().x()][move.start().y()] != Content.EMPTY)
        && (move.arrow().x() != move.start().x() || move.arrow().y() != move.start().y())) {
      throw new IllegalArgumentException("Invalid move: Content of 'start' must be empty.");
    }

    field[move.arrow().x()][move.arrow().y()] = Content.EMPTY;
    field[move.start().x()][move.start().y()] = field[move.to().x()][move.to().y()];
    field[move.to().x()][move.to().y()] = Content.EMPTY;
  }

  /**
   * Creates a copy of the given field.
   *
   * @param field the field to copy
   * @return a copy of the given field
   */
  public static Content[][] copyField(Content[][] field) {
    Content[][] copy = new Content[field.length][field.length];
    for (int i = 0; i < field.length; i++) {
      System.arraycopy(field[i], 0, copy[i], 0, field.length);
    }
    return copy;
  }

  /**
   * Returns a random valid move for the given position and player.
   *
   * @param field       the current position
   * @param playerColor the color of the player to move
   * @return a random valid move
   */
  public static Move getRandomMove(Content[][] field, Color playerColor) {
    int moveCount = countAllMovesAndArrows(field, playerColor);
    if (moveCount < 1) {
      return null;
    }
    int x = new Random().nextInt(moveCount);
    return getMoveAtIndex(field, playerColor, x);
  }

  /**
   * Gets all Current Positions for a side.
   *
   * @param askedPlayerColor the color of the player to be searches for
   * @param field            the position to evaluate
   * @return the current positions of all Amazons from askedPlayer
   */
  public static List<Position> getAllCurrentPositions(Color askedPlayerColor, Content[][] field) {
    List<Position> positions = new ArrayList<>();
    Content searchedEntry =
        ((askedPlayerColor == Color.WHITE) ? Content.WHITE_AMAZONE : Content.BLACK_AMAZONE);

    for (int x = 0; x < field.length; x++) {
      for (int y = 0; y < field.length; y++) {
        if (field[x][y] == searchedEntry) {
          positions.add(new Position(x, y));
        }
      }
    }
    return positions;
  }

  public static List<Position> getAllArrowPositions(Content[][] field) {
    List<Position> positions = new ArrayList<>();
    for (int x = 0; x < field.length; x++) {
      for (int y = 0; y < field.length; y++) {
        if (field[x][y] == Content.ARROW) {
          positions.add(new Position(x, y));
        }
      }
    }
    return positions;

  }

  /**
   * Will give you a List with all possible moves on the field for the given player.
   *
   * @param askedPlayerColor The color of the player all the Moves are searched for.
   * @return a List<Move> with all possible Moves on the field.
   */
  public static List<Move> getAllPossibleMoves(Content[][] field, Color askedPlayerColor) {
    List<Position> positionList = getAllCurrentPositions(askedPlayerColor, field);
    List<Move> out = new ArrayList<>();
    for (Position from : positionList) {
      for (int i = 0; i < 8; i++) {
        int newX = queen_x[i] + from.x();
        int newY = queen_y[i] + from.y();
        // calculates initial move
        while (newX >= 0 && newX < field.length && newY >= 0 && newY < field.length
            && field[newX][newY] == Content.EMPTY) {
          for (int j = 0; j < 8; j++) {
            int arrX = queen_x[j] + newX;
            int arrY = queen_y[j] + newY;
            // initializes arrow position for  move
            while (arrX >= 0 && arrX < field.length && arrY >= 0 && arrY < field.length
                && (field[arrX][arrY] == Content.EMPTY || (arrX == from.x() && arrY == from.y()))) {
              out.add(new Move(from, new Position(newX, newY), new Position(arrX, arrY)));
              arrX += queen_x[j];
              arrY += queen_y[j];
              //calculates possible arrow positions (for each possible move)
            }
          }
          newX += queen_x[i];
          newY += queen_y[i];
        }
        //calculate possible Knight moves
        newX = knight_x[i] + from.x();
        newY = knight_y[i] + from.y();
        if (newX >= 0 && newX < field.length && newY >= 0 && newY < field.length
            && field[newX][newY] == Content.EMPTY) {
          for (int j = 0; j < 8; j++) {
            int arrX = queen_x[j] + newX;
            int arrY = queen_y[j] + newY;
            // initializes arrow position for  move
            while (arrX >= 0 && arrX < field.length && arrY >= 0 && arrY < field.length
                && (field[arrX][arrY] == Content.EMPTY || (arrX == from.x() && arrY == from.y()))) {
              out.add(new Move(from, new Position(newX, newY), new Position(arrX, arrY)));
              arrX += queen_x[j];
              arrY += queen_y[j];
              //calculates possible arrow positions (for each possible move)
            }
          }
        }
      }
    }

    return out;
  }


  /**
   * Counts all Moves including Arrows
   *
   * @param field            The field on which is played
   * @param askedPlayerColor The color of the player all the Moves are searched for.
   * @return int with number of move/arrow combinations
   */
  public static int countAllMovesAndArrows(Content[][] field, Color askedPlayerColor) {
    List<Position> positionList = getAllCurrentPositions(askedPlayerColor, field);
    int out = 0;
    for (Position from : positionList) {
      out += countAllQueenMovesAndArrows(field, from);
      out += countAllKnightMovesAndArrows(field, from);
    }
    return out;
  }

  /**
   * Counts all queen moves including arrows
   *
   * @param field The field on which is played
   * @param from  Position to be evaluated
   * @return int with number of move/arrow combinations
   */
  private static int countAllQueenMovesAndArrows(Content[][] field, Position from) {
    int out = 0;
    for (int i = 0; i < 8; i++) {
      int newX = queen_x[i] + from.x();
      int newY = queen_y[i] + from.y();
      // calculates initial move
      while (newX >= 0
          && newX < field.length
          && newY >= 0
          && newY < field.length
          && field[newX][newY] == Content.EMPTY) {
        for (int j = 0; j < 8; j++) {
          int arrX = queen_x[j] + newX;
          int arrY = queen_y[j] + newY;
          // initializes arrow position for move
          while (arrX >= 0
              && arrX < field.length
              && arrY >= 0
              && arrY < field.length
              && (field[arrX][arrY] == Content.EMPTY || (arrX == from.x() && arrY == from.y()))) {
            out++;
            arrX += queen_x[j];
            arrY += queen_y[j];
            // calculates possible arrow positions (for each possible move)
          }
        }
        newX += queen_x[i];
        newY += queen_y[i];
      }
    }
    return out;
  }

  /**
   * Counts all knight moves including Arrows
   *
   * @param field The field on which is played
   * @param from  Position to be evaluated
   * @return int with number of move/arrow combinations
   */
  private static int countAllKnightMovesAndArrows(Content[][] field, Position from) {
    int out = 0;
    for (int i = 0; i < 8; i++) {
      int newX = knight_x[i] + from.x();
      int newY = knight_y[i] + from.y();
      // calculates initial move
      if (newX >= 0
          && newX < field.length
          && newY >= 0
          && newY < field.length
          && field[newX][newY] == Content.EMPTY) {
        for (int j = 0; j < 8; j++) {
          int arrX = queen_x[j] + newX;
          int arrY = queen_y[j] + newY;
          // initializes arrow position for move
          while (arrX >= 0
              && arrX < field.length
              && arrY >= 0
              && arrY < field.length
              && (field[arrX][arrY] == Content.EMPTY || (arrX == from.x() && arrY == from.y()))) {
            out++;
            arrX += queen_x[j];
            arrY += queen_y[j];
            // calculates possible arrow positions (for each possible move)
          }
        }
      }
    }
    return out;
  }


  /**
   * Helper Method for getRandomMove, iterates to index-th move
   *
   * @param field            The field on which is played.
   * @param askedPlayerColor either White or Back, side which moves
   * @param index            Index of move to be returned, cannot be greater than number of possible
   *                         Move/Arrow combinations
   * @return Move at index 'Index'
   */
  private static Move getMoveAtIndex(Content[][] field, Color askedPlayerColor, int index) {
    int current = 0;
    List<Position> positionList = getAllCurrentPositions(askedPlayerColor, field);
    for (Position from : positionList) {
      for (int i = 0; i < 8; i++) {
        int newX = queen_x[i] + from.x();
        int newY = queen_y[i] + from.y();
        // calculates initial move
        while (newX >= 0
            && newX < field.length
            && newY >= 0
            && newY < field.length
            && field[newX][newY] == Content.EMPTY) {
          for (int j = 0; j < 8; j++) {
            int arrX = queen_x[j] + newX;
            int arrY = queen_y[j] + newY;
            // initializes arrow position for move
            while (arrX >= 0
                && arrX < field.length
                && arrY >= 0
                && arrY < field.length
                && ((field[arrX][arrY] == Content.EMPTY) || (from.x() == arrX
                && from.y() == arrY))) {
              if (current == index) {
                return new Move(from, new Position(newX, newY), new Position(arrX, arrY));
              }
              current++;
              arrX += queen_x[j];
              arrY += queen_y[j];
              // calculates possible arrow positions (for each possible move)
            }
          }
          newX += queen_x[i];
          newY += queen_y[i];
        }
      }
      for (int i = 0; i < 8; i++) {
        int newX = knight_x[i] + from.x();
        int newY = knight_y[i] + from.y();
        // calculates initial move
        if (newX >= 0
            && newX < field.length
            && newY >= 0
            && newY < field.length
            && field[newX][newY] == Content.EMPTY) {
          for (int j = 0; j < 8; j++) {
            int arrX = queen_x[j] + newX;
            int arrY = queen_y[j] + newY;
            // initializes arrow position for move
            while (arrX >= 0
                && arrX < field.length
                && arrY >= 0
                && arrY < field.length
                && ((field[arrX][arrY] == Content.EMPTY) || (from.x() == arrX
                && from.y() == arrY))) {
              if (current == index) {
                return new Move(from, new Position(newX, newY), new Position(arrX, arrY));
              }
              current++;
              arrX += queen_x[j];
              arrY += queen_y[j];
              // calculates possible arrow positions (for each possible move)
            }
          }
        }
      }
    }
    throw new IllegalArgumentException("Index cannot be greater than number of possible moves.");
  }

  /**
   * Evaluates a given position.
   *
   * @param field the position to evaluate
   * @return the evaluation of the given position positive value is advantage for white, negative
   * value is advantage for black
   */
  public static int evaluatePosition(final Content[][] field) {
    int eval = 0;
    int white = countAllMovesAndArrows(field, Color.WHITE);
    int black = countAllMovesAndArrows(field, Color.BLACK);
    if (white == 0) {
      eval -= 20;
    }
    if (black == 0) {
      eval += 20;
    }
    eval += white;
    eval -= black;
    return eval;
  }

  // Run bfs from both sides and award square to whichever side can reach it in fewer moves
// Score > 0 favors white; < 0 favors black
  public static int voronoi(Content[][] board) {
    int[][] wDist = bfs(board, Content.WHITE_AMAZONE); // min moves for white to reach each square
    int[][] bDist = bfs(board, Content.BLACK_AMAZONE); // min moves for black to reach each square

    int score = 0;
    for (int r = 0; r < board.length; r++) {
      for (int c = 0; c < board.length; c++) {
        if (board[r][c] == Content.EMPTY) {
          int w = wDist[r][c];
          int b = bDist[r][c];
          if (w < b) {
            score++; // white owns square
          } else if (b < w) {
            score--; // black owns square
          }
          // else equal distance,i.e. no one gets awarded
        }
      }
    }
    return score;
  }

  private static int[][] bfs(Content[][] board, Content piece) {
    int[][] dist = new int[board.length][board.length];
    for (int[] row : dist) {
      Arrays.fill(row, Integer.MAX_VALUE);
    }
    int[] queue = new int[board.length * board.length];
    int head = 0;
    int tail = 0;

    // seed bfs from all amazon positions of this color (i.e. dist == 0)
    for (int r = 0; r < board.length; r++) {
      for (int c = 0; c < board.length; c++) {
        if (board[r][c] == piece) {
          dist[r][c] = 0;
          queue[tail++] = r * board.length + c;
        }
      }
    }

    while (head < tail) {
      int curr = queue[head++];
      int cr = curr / board.length;
      int cc = curr % board.length;
      int d = dist[cr][cc] + 1;

      //expand via queen slides (blocked by non-empty squares)
      for (int[] dir : AlphaBetaEvaluator.DIRS) {
        int r = cr + dir[0];
        int c = cc + dir[1];
        while (r >= 0 && r < board.length && c >= 0 && c < board.length
            && board[r][c] == Content.EMPTY) {
          if (dist[r][c] > d) {
            dist[r][c] = d;
            queue[tail++] = r * board.length + c;
          }
          r += dir[0];
          c += dir[1];
        }
      }

      // expand via knight jumps (only landing square must be empty)
      for (int[] km : AlphaBetaEvaluator.KNIGHTS) {
        int r = cr + km[0];
        int c = cc + km[1];
        if (r >= 0
            && r < board.length
            && c >= 0
            && c < board.length
            && board[r][c] == Content.EMPTY
            && dist[r][c] > d) {
          dist[r][c] = d;
          queue[tail++] = r * board.length + c;
        }
      }
    }
    return dist;
  }
}
