package client.dto;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import client.game.Position;



public record FrontendGameMove (

    @JsonProperty("start_move")
    Position startMove,

    @JsonProperty("target_move")
    Position targetMove,

    @JsonProperty("arrow_target")
    Position arrowTarget
) {
    public FrontendGameMove {
        Objects.requireNonNull(startMove);
        Objects.requireNonNull(targetMove);
        Objects.requireNonNull(arrowTarget);
    }
}
