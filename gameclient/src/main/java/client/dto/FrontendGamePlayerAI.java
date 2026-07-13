package client.dto;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import ai.engine.KIClient;



public record FrontendGamePlayerAI(
        @JsonProperty("ai_id")
        String aiId, // need at least one non-optional field for jackson deduction to work
        String name,
        KIClient.Difficulty difficulty
) implements FrontendGamePlayer {
    public FrontendGamePlayerAI {
        Objects.requireNonNull(aiId);
    }
}
