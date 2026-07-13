package client.dto;

import java.net.URI;
import java.util.Objects;


/**
 * Represents a player participating in a game.
 *
 * @param playerId Numeric identifier of the player; must be >= 0
 * @param name     Display name of the player; must not be null
 * @param url      URL associated with this player; must not be null
 */
public record ServerGamePlayer(
        String playerId,
        String name,
        URI url
) {
    /**
     * @throws NullPointerException     if name or url is null
     * @throws IllegalArgumentException if playerId is negative
     */
    public ServerGamePlayer {
        Objects.requireNonNull(playerId);
        Objects.requireNonNull(name);
        Objects.requireNonNull(url);
    }
}