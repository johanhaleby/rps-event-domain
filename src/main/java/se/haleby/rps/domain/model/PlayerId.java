package se.haleby.rps.domain.model;

import lombok.Data;

import java.util.UUID;

@Data(staticConstructor = "fromUUID")
public class PlayerId {
    public final UUID uuid;

    public static PlayerId random() {
        return PlayerId.fromUUID(UUID.randomUUID());
    }
}