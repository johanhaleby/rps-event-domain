package se.haleby.rps.domain.event;

import lombok.Builder;
import lombok.Data;
import se.haleby.rps.domain.model.GameId;
import se.haleby.rps.domain.model.PlayerId;

import java.util.Date;

@Data
@Builder
public class GameStarted implements DomainEvent {
    private final GameId gameId;
    private final PlayerId startedBy;
    private final Date startedAt;
}