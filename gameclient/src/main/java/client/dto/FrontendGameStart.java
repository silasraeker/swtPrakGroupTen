package client.dto;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Configuration data of a game, used for deserialization from frontend to gameclient.
 *
 * @param players     The participating players; must contain exactly 2 entries
 * @param maxTurnTime Maximum time per turn in seconds; if set, must be >= 0
 * @param boardSize   Board edge length; if set, must be >= 0
 * @param turnDelay   Delay between turns in seconds; if set, must be >= 0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FrontendGameStart(

        FrontendGamePlayer player0,
        FrontendGamePlayer player1,

        @JsonProperty("max_turn_time")
        Long maxTurnTime, // in seconds

        @JsonProperty("board_size")
        Byte boardSize,

        @JsonProperty("turn_delay")
        Long turnDelay // in seconds
) {
    /**
     * @throws NullPointerException     if players is null
     * @throws IllegalArgumentException if players is not exactly length 2,
     *                                  or if any non-null numeric value is negative
     */
    public FrontendGameStart {
        Objects.requireNonNull(player0);
        Objects.requireNonNull(player1);

        if (maxTurnTime != null && maxTurnTime < 0) throw new IllegalArgumentException();
        if (boardSize != null && boardSize < 0) throw new IllegalArgumentException();
        if (turnDelay != null && turnDelay < 0) throw new IllegalArgumentException();
    }
}