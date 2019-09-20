package se.haleby.rps.domain.model;

import lombok.Data;

import java.util.UUID;

@Data(staticConstructor = "fromUUID")
public class GameId {
    public final UUID uuid;

    public static GameId random() {
        return GameId.fromUUID(UUID.randomUUID());
    }
}
