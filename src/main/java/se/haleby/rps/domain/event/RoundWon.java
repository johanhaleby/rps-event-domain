package se.haleby.rps.domain.event;

import lombok.Builder;
import lombok.Data;
import se.haleby.rps.domain.model.GameId;
import se.haleby.rps.domain.model.PlayerId;

@Data
@Builder
public class RoundWon implements DomainEvent {
    private final GameId gameId;
    private final int roundNumber;
    private final PlayerId winnerId;
}
