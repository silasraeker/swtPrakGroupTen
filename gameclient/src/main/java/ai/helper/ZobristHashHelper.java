package ai.helper;

import client.game.Content;
import client.game.Move;
import client.game.Player;
import client.game.Player.Color;
import client.game.Position;
import java.util.List;
import java.util.SplittableRandom;

public class ZobristHashHelper {
  /*
   * This Class is only applicable for board size 10x10 or less
   */

  private static long[][] zobristTable;
  private static long blackTurn = 0;


  private ZobristHashHelper() {
  }

  static {
    SplittableRandom random = new SplittableRandom();
    /* piece=0->white
     * piece=1->black
     * piece=2->arrow
     */
    for (int piece = 0; piece < 3; piece++) {
      for (int square = 0; square < 100; square++) {
        zobristTable[piece][square] = random.nextLong();
      }
    }
    blackTurn = random.nextLong();
  }

  /**
   * Return an initial Hash for any given field, assumes starting player to be white
   *
   * @param field to be hashed with size smaller or equal than 10;
   * @return long which contains hashed field
   */
  public long getInitialHash(Content[][] field) {
    if (field.length > 10) {
      throw new IllegalArgumentException("The HashingTable is only possible for smaller fields");
    }
    long initialHash = 0;
    List<Position> blackPositions = EvaluatorHelper.getAllCurrentPositions(Color.BLACK, field);
    List<Position> whitePositions = EvaluatorHelper.getAllCurrentPositions(Color.WHITE, field);
    for (Position position : blackPositions) {
      initialHash = initialHash ^ zobristTable[1][position.x() * 10 + position.y()];
    }
    for (Position position : whitePositions) {
      initialHash = initialHash ^ zobristTable[0][position.x() * 10 + position.y()];
    }
    return initialHash;
  }

  /**
   * Updates Hash after move
   *
   * @param hash  to be updated
   * @param move  which changed state of field
   * @param color which just moved
   * @return updated hash (still as long)
   */
  public long updateHash(long hash, Move move, Player.Color color) {
    int colorVal = (color == Color.WHITE) ? 0 : 1;
    hash = hash ^ zobristTable[colorVal][move.start().x() * 10 + move.start().y()];
    hash = hash ^ zobristTable[colorVal][move.to().x() * 10 + move.to().y()];
    hash = hash ^ zobristTable[2][move.arrow().x() * 10 + move.arrow().y()];
    hash = hash ^ blackTurn;
    return hash;
  }

  /**
   * Method hashes field, on which was already played Use method only in rare cases, since other
   * methods are far more efficient
   *
   * @param field field on which was played
   * @param color whose turn it is
   * @return long which is hash of field
   */
  public long hashField(Content[][] field, Player.Color color) {
    long hash = 0;
    List<Position> blackPositions = EvaluatorHelper.getAllCurrentPositions(Color.BLACK, field);
    List<Position> whitePositions = EvaluatorHelper.getAllCurrentPositions(Color.WHITE, field);
    List<Position> arrowPositions = EvaluatorHelper.getAllArrowPositions(field);
    for (Position position : blackPositions) {
      hash = hash ^ zobristTable[1][position.x() * 10 + position.y()];
    }
    for (Position position : whitePositions) {
      hash = hash ^ zobristTable[0][position.x() * 10 + position.y()];
    }
    for (Position position : arrowPositions) {
      hash = hash ^ zobristTable[2][position.x() * 10 + position.y()];
    }
    hash = (color == Color.WHITE) ? hash : hash ^ blackTurn;
    return hash;
  }
}
