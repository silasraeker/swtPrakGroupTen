package client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;



public record ServerGameMoveReceived (

    @JsonInclude(JsonInclude.Include.NON_NULL)
    String id,

    byte player,

    @JsonProperty("queenStartPositions")
    int[] startMove,

    @JsonProperty("queenEndPositions")
    int[] targetMove,

    @JsonProperty("arrowPositions")
    int[] arrowTarget
) {
    public ServerGameMoveReceived {
        Objects.requireNonNull(startMove);
        Objects.requireNonNull(targetMove);
        Objects.requireNonNull(arrowTarget);

        if (startMove.length != 2) throw new IllegalArgumentException();
        if (targetMove.length != 2) throw new IllegalArgumentException();
        if (arrowTarget.length != 2) throw new IllegalArgumentException();

        if (player < 0 || 1 < player) throw new IllegalArgumentException();
    }
}
