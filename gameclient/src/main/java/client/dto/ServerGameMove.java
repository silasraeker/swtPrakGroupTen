package client.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;

	
	
public record ServerGameMove (

    byte player,

    @JsonProperty("start_move")
    int[] startMove,

    @JsonProperty("target_move")
    int[] targetMove,

    @JsonProperty("arrow_target")
    int[] arrowTarget
) {
    public ServerGameMove {
        Objects.requireNonNull(startMove);
        Objects.requireNonNull(targetMove);
        Objects.requireNonNull(arrowTarget);

        if (startMove.length != 2) throw new IllegalArgumentException();
        if (targetMove.length != 2) throw new IllegalArgumentException();
        if (arrowTarget.length != 2) throw new IllegalArgumentException();
        if (player < 0 || 1 < player) throw new IllegalArgumentException();
    }
}