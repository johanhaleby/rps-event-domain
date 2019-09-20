package se.haleby.rps;

import org.junit.jupiter.api.*;
import se.haleby.rps.domain.event.DomainEvent;
import se.haleby.rps.domain.event.GameCreated;
import se.haleby.rps.domain.event.GameStarted;
import se.haleby.rps.domain.model.Game;
import se.haleby.rps.domain.model.GameCreatorId;
import se.haleby.rps.domain.model.GameId;
import se.haleby.rps.domain.model.PlayerId;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static se.haleby.rps.domain.model.Move.PAPER;

@SuppressWarnings("InnerClassMayBeStatic")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Game")
class GameTest {

    @Nested
    @DisplayName("is created")
    class IsCreated {

        @Test
        void when_create_command_is_invoked() {
            // Given
            Date creationDate = new Date();
            GameId gameId = GameId.random();
            GameCreatorId gameCreatorId = GameCreatorId.random();

            // When
            List<DomainEvent> events = Game.create(gameId, gameCreatorId, 3, creationDate);

            // Then
            assertThat(events).containsOnly(GameCreated.builder().createdAt(creationDate).createdBy(gameCreatorId).gameId(gameId).rounds(3).build());
        }
    }

    @Nested
    @DisplayName("is started")
    class IsStarted {

        @Test
        void when_first_move_is_made() {
            // Given
            Date creationDate = new Date();
            GameId gameId = GameId.random();
            GameCreatorId gameCreatorId = GameCreatorId.random();
            PlayerId player = PlayerId.random();

            List<DomainEvent> eventStream = Collections.singletonList(GameCreated.builder().createdAt(creationDate).createdBy(gameCreatorId).gameId(gameId).rounds(3).build());
            Game game = Game.restoreFrom(eventStream);

            // When
            List<DomainEvent> events = game.play(player, PAPER);

            // Then
            GameStarted gameStarted = extractFrom(events, GameStarted.class);
            assertAll(
                    () -> assertThat(gameStarted.getGameId()).isEqualTo(gameId),
                    () -> assertThat(gameStarted.getStartedAt()).isCloseTo(new Date(), 1000),
                    () -> assertThat(gameStarted.getStartedBy()).isEqualTo(player)
            );
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T extractFrom(List<DomainEvent> events, Class<T> type) {
        return (T) events.stream().filter(e -> e.getClass().isAssignableFrom(type)).findFirst().orElse(null);
    }
}