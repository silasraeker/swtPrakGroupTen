package client.dto;

import java.util.List;
import java.util.Objects;

/*
takes:
{"moves":[
{"id":1,"player":0,"queenStartPositions":[3,4],"arrowPositions":[3,1],"queenEndPositions":[4,2]}
],
"winningPlayerEntity":null,
"gameId":168969246,
"winningPlayer":null,
"maxTurnTime":1000,
"boardSize":5,
"players":[
{"playerId":0,"name":"player0","url":"http://localhost:6070/game/state/4c19f9dd-0a49-459c-a5d1-ea4af721eabf/0"},
{"playerId":1,"name":"player1","url":"http://localhost:6070/game/state/4c19f9dd-0a49-459c-a5d1-ea4af721eabf/1"}
]
}
*/

public record ServerGame(

    List<ServerGameMoveReceived> moves,
    ServerGamePlayer winningPlayerEntity,
    long gameId,
    Integer winningPlayer,
    long maxTurnTime,
    int boardSize,
    List<ServerGamePlayer> players
) {
  public ServerGame {
    Objects.requireNonNull(moves, "moves must not be null");
    Objects.requireNonNull(players, "players must not be null");

    if (gameId < 0)
      throw new IllegalArgumentException("gameId must not be negative");

    if (maxTurnTime <= 0)
      throw new IllegalArgumentException("maxTurnTime must be positive");

    if (boardSize <= 0)
      throw new IllegalArgumentException("boardSize must be positive");

    if (players.size() != 2)
      throw new IllegalArgumentException("players must contain exactly 2 entries");
  }
}
