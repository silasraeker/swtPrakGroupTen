package client.dto;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import client.game.Content;




public record FrontendGameState (
    
    @JsonProperty("remaining_move_time")
    long remainingMoveTime,

    Content[][] board,

    @JsonProperty("possible_moves")
    List<FrontendGameMovesFromPosition> possibleMoves
) {
    public FrontendGameState {
        Objects.requireNonNull(board);
        Objects.requireNonNull(possibleMoves);

        if (possibleMoves.size() != 4) throw new IllegalArgumentException();
    }
}