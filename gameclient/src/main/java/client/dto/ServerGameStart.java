package client.dto;

import java.util.Objects;


/**
 * Wrapper used to initiate a new game.
 *
 * @param game Configuration of the game to start; must not be null
 */
public record ServerGameStart(
        ServerGameData game
) {
    /**
     * @throws NullPointerException if game is null
     */
    public ServerGameStart {
        Objects.requireNonNull(game);
    }
}