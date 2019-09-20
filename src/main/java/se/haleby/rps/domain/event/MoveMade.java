package se.haleby.rps.domain.event;

import lombok.Builder;
import lombok.Data;
import se.haleby.rps.domain.model.GameId;
import se.haleby.rps.domain.model.Move;
import se.haleby.rps.domain.model.PlayerId;

@Data
@Builder
public class MoveMade implements DomainEvent {
    private final GameId gameId;
    private final int round;
    private final PlayerId playerId;
    private final Move move;
}
