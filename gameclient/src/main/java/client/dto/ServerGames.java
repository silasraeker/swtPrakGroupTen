package client.dto;

import java.util.List;
import java.util.Objects;



public record ServerGames(
    List<ServerGame> games
) {
    public ServerGames {
        Objects.requireNonNull(games);
    }
}
