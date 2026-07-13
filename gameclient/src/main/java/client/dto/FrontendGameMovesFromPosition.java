package client.dto;

import java.util.List;
import java.util.Objects;

import client.game.Position;



public record FrontendGameMovesFromPosition (
    Position position,
    List<FrontendGameMoveFromPosition> moves
) {
    public FrontendGameMovesFromPosition {
        Objects.requireNonNull(position);
        Objects.requireNonNull(moves);
    }
}
