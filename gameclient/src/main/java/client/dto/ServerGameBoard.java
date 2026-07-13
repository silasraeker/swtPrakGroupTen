package client.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ServerGameBoard (

    @JsonProperty("game_id")
    String gameId,

    @JsonProperty("field_values")
    byte[][] board,

    @JsonProperty("game_state")
    String gameState
)
{
  public ServerGameBoard {
    
    Objects.requireNonNull(gameId);
    Objects.requireNonNull(board);
    Objects.requireNonNull(gameState);

    if (gameId.isBlank()) throw new IllegalArgumentException();
  }
}
