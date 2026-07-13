package ai.helper;
import client.game.Content;
import client.game.Move;
import client.game.Player.Color;
import client.game.Position;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility class providing static helper methods for {@code Evaluator}.
 */
public final class EvaluatorHelper {

  private EvaluatorHelper() {
    throw new IllegalStateException("There should be no instance of this class");
  }
  private static final int [] knight_x ={1,1,-1,-1,2,2,-2,-2};
  private static final int [] knight_y = {2,-2,2,-2,1,-1,1,-1};
  private static final int [] queen_x = {0, 1, 1, 1, 0, -1, -1, -1};
  private static final int [] queen_y = {1, 1, 0, -1, -1, -1, 0, 1};



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
      field[move.start().x()][move.start().y()] = field[move.to().x()][move.to().y()];
      field[move.to().x()][move.to().y()] = Content.EMPTY;
      field[move.arrow().x()][move.arrow().y()] = Content.EMPTY;
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
   * @param field  the current position
   * @param playerColor the color of the player to move
   * @return a random valid move
   */
  public static Move getRandomMove(Content[][] field, Color playerColor) {
    int x = new Random().nextInt(countAllMovesAndArrows(field, playerColor));
    return getMoveAtIndex(field, playerColor, x);
  }

  /**
   * Gets all Current Positions for a side.
   *
   * @param askedPlayerColor the color of the player to be searches for
   * @param field       the position to evaluate
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

  /**
   * Gets all number of current positions for a side.
   *
   * @param from  the position to be searched for
   * @param field the field to evaluated
   * @return number of possible moves from input
   */
  public static int countPossibleMovesFromPosition(Position from, Content[][] field) {

    int out = countPossibleKnightMoves(from, field);
    out += countPossibleQueenMoves(from, field);
    return out;
  }

  /**
   * Counts all possible moves on the field for the given player.
   * @param field The field on which in played upon
   * @param askedPlayerColor The color of the player all the Moves are searched for.
   * @return a List<Move> with all possible Moves on the field.
   */
  public static int countAllPossibleMoves(Content[][] field, Color askedPlayerColor) {
    List<Position> positionList = getAllCurrentPositions(askedPlayerColor, field);
    int out = 0;

    for (Position piece : positionList) {
      out += countPossibleMovesFromPosition(piece, field);
    }

    return out;
  }


  /**
   * Will count all possible knight moves from 'from'.
   * @param field The field on which is played upon
   * @param from The Position the moves will start from.
   * @return int which counts all possible knight moves started on from.
   */
  public static int countPossibleKnightMoves(Position from, Content[][] field) {

    int out = 0;

    for (int i=0; i<8; i++) {
      int newX = knight_x[i] + from.x();
      int newY = knight_y[i] + from.y();

      if (newX >= 0 && newX < field.length && newY >= 0 && newY < field.length && field[newX][newY] == Content.EMPTY) {
        out++;
      }
    }

    return out;
  }


  /**
   * Will return number of possible queen moves from 'from'.
   *
   * @param from  The Position the moves will start from.
   * @param field The Field to be evaluated
   * @return int with count of all possible queen moves started on from.
   */
  public static int countPossibleQueenMoves(Position from, Content[][] field) {
    int out = 0;
    for (int i=0; i<8; i++) {
      int x = queen_x[i] + from.x();
      int y = queen_y[i] + from.y();
      while(x >= 0 && y >= 0 && x < field.length && y < field.length && field[x][y] == Content.EMPTY) {
        out += 1;
        x += queen_x[i];
        y += queen_y[i];
      }
    }
    return out;
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
      for (int i=0; i<8; i++) {
        int newX = queen_x[i] + from.x();
        int newY = queen_y[i] + from.y();
        // calculates initial move
        while (newX >= 0 && newX < field.length && newY >= 0 && newY < field.length && field[newX][newY] == Content.EMPTY) {
          for(int j=0; j<8; j++) {
            int arrX = queen_x[j] + newX;
            int arrY = queen_y[j] + newY;
            // initializes arrow position for  move
            while (arrX >= 0 && arrX < field.length && arrY >= 0 && arrY < field.length
                    && (field[arrX][arrY] == Content.EMPTY || (arrX == from.x() && arrY == from.y())))  {
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
        if (newX >= 0 && newX < field.length && newY >= 0 && newY < field.length && field[newX][newY] == Content.EMPTY) {
          for(int j=0; j<8; j++) {
            int arrX = queen_x[j] + newX;
            int arrY = queen_y[j] + newY;
            // initializes arrow position for  move
            while (arrX >= 0 && arrX < field.length && arrY >= 0 && arrY < field.length
                    && (field[arrX][arrY] == Content.EMPTY || (arrX == from.x() && arrY == from.y())) ) {
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
   * @param field The field on which is played
   * @param askedPlayerColor The color of the player all the Moves are searched for.
   * @return int with number of move/arrow combinations
   */
  public static int countAllMovesAndArrows(Content[][] field, Color askedPlayerColor) {
    List<Position> positionList = getAllCurrentPositions(askedPlayerColor, field);
    int out = 0;
    for (Position from : positionList){
      out+=countAllQueenMovesAndArrows(field, from);
      out+=countAllKnightMovesAndArrows(field, from);
    }
    return out;
  }

  /**
   * Counts all Queenmoves including Arrows
   * @param field The field on which is played
   * @param from Positin to be evaluated
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
   * @param field The field on which is played
   * @param from Positin to be evaluated
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
   * @param field The field on which is played.
   * @param askedPlayerColor either White or Back, side which moves
   * @param index Index of move to be returned, cannot be greater than number of possible Move/Arrow combinations
   * @return Move at index 'Index'
   */
  private static Move getMoveAtIndex(Content[][] field, Color askedPlayerColor, int index) {
    int current = 0;
    List<Position> positionList = getAllCurrentPositions(askedPlayerColor, field);
    for (Position from : positionList){
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
                    && ((field[arrX][arrY] == Content.EMPTY) || (from.x() == arrX && from.y() == arrY))) {
              if(current == index) {
                return new Move(from, new Position(newX, newY), new Position (arrX, arrY));
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
                    && ((field[arrX][arrY] == Content.EMPTY) || (from.x() == arrX && from.y() == arrY))) {
              if(current == index) {
                return new Move(from, new Position(newX, newY), new Position (arrX, arrY));
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
    eval += countAllPossibleMoves(field, Color.WHITE);
    eval -= countAllPossibleMoves(field, Color.BLACK);
    return eval;
  }
}
