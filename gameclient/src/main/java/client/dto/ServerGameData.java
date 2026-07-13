package client.dto;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Configuration data of a game, used for serialization from gameclient to gameserver.
 *
 * @param players     The participating players; must contain exactly 2 entries
 * @param maxTurnTime Maximum time per turn in seconds; if set, must be >= 0
 * @param boardSize   Board edge length; if set, must be >= 0
 * @param turnDelay   Delay between turns in seconds; if set, must be >= 0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties({"moves", "winningPlayerEntity", "gameId", "winningPlayer"})
public record ServerGameData(
        ServerGamePlayer[] players,

        Long maxTurnTime, // in seconds

        @JsonProperty("board_size")
        @JsonAlias("boardSize")
        Byte boardSize,

        @JsonProperty("turn_delay")
        Long turnDelay // in seconds
) {
    /**
     * @throws NullPointerException     if players is null
     * @throws IllegalArgumentException if players is not exactly length 2,
     *                                  or if any non-null numeric value is negative
     */
    public ServerGameData {
        Objects.requireNonNull(players);
        if (players.length != 2) throw new IllegalArgumentException();
        
        if (maxTurnTime != null && maxTurnTime < 0) throw new IllegalArgumentException();
        if (boardSize != null && boardSize < 0) throw new IllegalArgumentException();
        if (turnDelay != null && turnDelay < 0) throw new IllegalArgumentException();

        if (Objects.isNull(turnDelay)) { turnDelay = 0L; }
    }
}