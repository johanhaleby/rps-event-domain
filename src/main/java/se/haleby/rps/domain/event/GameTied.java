package se.haleby.rps.domain.event;

import lombok.Data;
import se.haleby.rps.domain.model.GameId;

@Data(staticConstructor = "withGameId")
public class GameTied implements DomainEvent {
    private final GameId gameId;
}
