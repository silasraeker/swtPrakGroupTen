package client.dto;

import java.net.URI;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;



@JsonInclude(JsonInclude.Include.NON_NULL)
public record FrontendConfig(

    @JsonProperty("server_adress")
    URI serverAddress,

    @JsonProperty("client_adress")
    URI clientAddress
) {
    public FrontendConfig {
        if (Objects.nonNull(serverAddress) && serverAddress.toString().isEmpty()) { serverAddress = null; }
        if (Objects.nonNull(clientAddress) && clientAddress.toString().isEmpty()) { clientAddress = null; }
    }
}
