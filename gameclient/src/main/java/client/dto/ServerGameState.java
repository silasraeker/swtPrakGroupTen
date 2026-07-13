package client.dto;


/**
 * Represents the current state of a game at a given point in time.
 *
 * @param gameId Numeric identifier of the game
 * @param player The player whose turn it is; must be 0 or 1
 * @param board  The current board state as a 2D array; each field must be in range [-2, 1]
 */
public record ServerGameState (
    String gameId,
    byte player,
    byte[][] board
) {
    /**
     * @throws IllegalArgumentException if player is not 0 or 1, or if any field
     *                                  in the board is outside the range [-2, 1]
     */
    public ServerGameState {
        if (player != 0 && player != 1) throw new IllegalArgumentException();

        for (byte[] line : board) {
            for (byte field : line) {
                if (field < -2 || 1 < field) throw new IllegalArgumentException();
            }
        }
    }
}
