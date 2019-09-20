package se.haleby.rps.domain.model;

import lombok.Data;

import java.util.UUID;

@Data(staticConstructor = "fromUUID")
public class GameCreatorId {
    public final UUID uuid;

    public static GameCreatorId random() {
        return GameCreatorId.fromUUID(UUID.randomUUID());
    }
}