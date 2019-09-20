package se.haleby.rps.domain.event;

import lombok.Builder;
import lombok.Data;
import se.haleby.rps.domain.model.GameCreatorId;
import se.haleby.rps.domain.model.GameId;

import java.util.Date;

@Data
@Builder
public class GameCreated implements DomainEvent {
    private final GameId gameId;
    private final GameCreatorId createdBy;
    private final int rounds;
    private final Date createdAt;
}