package client.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;



@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
    @JsonSubTypes.Type(value = FrontendGamePlayerHuman.class),
    @JsonSubTypes.Type(value = FrontendGamePlayerAI.class)
})
public interface FrontendGamePlayer {}
