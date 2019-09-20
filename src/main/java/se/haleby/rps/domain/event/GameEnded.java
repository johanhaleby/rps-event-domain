package se.haleby.rps.domain.event;

import lombok.Data;
import se.haleby.rps.domain.model.GameId;

@Data(staticConstructor = "withGameId")
public class GameEnded implements DomainEvent {
    private final GameId gameId;
}
