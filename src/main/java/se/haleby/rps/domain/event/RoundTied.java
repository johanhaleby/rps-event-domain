package se.haleby.rps.domain.event;

import lombok.Builder;
import lombok.Data;
import se.haleby.rps.domain.model.GameId;

@Data
@Builder
public class RoundTied implements DomainEvent {
    private final GameId gameId;
    private final int roundNumber;
}
