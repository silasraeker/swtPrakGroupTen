package client.dto;

import java.net.URI;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;



public record FrontendGamePlayerHuman(
        @JsonProperty("player_id")
        String playerId,
        String name,
        URI url
) implements FrontendGamePlayer {
    public FrontendGamePlayerHuman {
        Objects.requireNonNull(playerId);
    }
}
