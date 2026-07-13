package client.dto;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import client.game.Position;



public record FrontendGameMoveFromPosition(

    @JsonProperty("target_move")
    Position targetMove,

    @JsonProperty("arrow_target")
    Position arrowTarget
) {
    public FrontendGameMoveFromPosition {
        Objects.requireNonNull(targetMove);
        Objects.requireNonNull(arrowTarget);
    }
}