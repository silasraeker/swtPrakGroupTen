package client.game;

import java.util.Arrays;
import java.util.Objects;



public enum Content {

  WHITE_AMAZONE,
  BLACK_AMAZONE,
  ARROW,
  EMPTY;

  public static String formatBoard(Content[][] board) {

    Objects.requireNonNull(board);
    
    StringBuilder sb = new StringBuilder();
    for (Content[] line : board) {
      sb.append(Arrays.toString(line));
      sb.append('\n');
    }

    return sb.toString();
  }
}
