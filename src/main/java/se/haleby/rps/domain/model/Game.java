package se.haleby.rps.domain.model;


import se.haleby.rps.domain.event.*;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static se.haleby.rps.domain.model.State.*;

public final class Game {

    private GameId gameId;
    private PlayerId playerId1;
    private PlayerId playerId2;
    private State state;
    private TreeSet<Round> rounds;
    private int numberOfRoundsInGame;

    private Game() {
    }

    public static List<DomainEvent> create(GameId gameId, GameCreatorId gameCreatorId, int numberOfRoundsInGame, Date creationDate) {
        requireNonNull(gameId, "Game id cannot be null");
        if (numberOfRoundsInGame < 1) {
            throw new IllegalArgumentException("Number of rounds in game must be at leat 1");
        }
        return Collections.singletonList(GameCreated.builder().gameId(gameId).createdBy(gameCreatorId).rounds(numberOfRoundsInGame).createdAt(creationDate).build());
    }

    public List<DomainEvent> play(PlayerId playerId, Move move) {
        assertThatGameIsPlayable();

        List<DomainEvent> events = new ArrayList<>();
        events.addAll(applyAndReturn(startNewRoundIfLastRoundHasEnded(rounds)));
        events.addAll(applyAndReturn(joinGameIfNotAlreadyPlaying(playerId)));

        Round round = rounds.last();
        events.addAll(applyAndReturn(round.play(gameId, playerId, move)));

        boolean roundEnded = events.stream().anyMatch(RoundEnded.class::isInstance);
        boolean gameEnded = roundEnded && isLastRound();
        if (gameEnded) {
            Map<PlayerId, List<Round>> wonRoundsPerPlayer = rounds.stream().collect(groupingBy(Round::winner));
            int wonRoundsPlayer1 = wonRoundsPerPlayer.getOrDefault(playerId1, emptyList()).size();
            int wonRoundsPlayer2 = wonRoundsPerPlayer.getOrDefault(playerId2, emptyList()).size();

            if (wonRoundsPlayer1 == wonRoundsPlayer2) {
                events.add(GameTied.withGameId(gameId));
            } else {
                final PlayerId winner;
                if (wonRoundsPlayer1 > wonRoundsPlayer2) {
                    winner = playerId1;
                } else {
                    winner = playerId2;
                }

                events.add(GameWon.builder().gameId(gameId).winnerId(winner).build());
                events.add(GameEnded.withGameId(gameId));
            }
            events.add(GameEnded.withGameId(gameId));
        }
        return Collections.unmodifiableList(events);
    }

    public static Game restoreFrom(List<DomainEvent> events) {
        return events.stream().collect(Game::new, Game::apply, (game, game2) -> {
        });
    }

    private boolean isLastRound() {
        return rounds.size() == numberOfRoundsInGame;
    }

    private void assertThatGameIsPlayable() {
        if (state != CREATED && state != STARTED) {
            throw new IllegalStateException("Cannot play since game is already " + state.name().toLowerCase());
        }
    }

    private List<DomainEvent> startNewRoundIfLastRoundHasEnded(TreeSet<Round> rounds) {
        if (rounds.isEmpty() || rounds.last().isEnded()) {
            int roundNumber = rounds.size() + 1;
            return Collections.singletonList(RoundStarted.builder().gameId(gameId).roundNumber(roundNumber).build());
        } else {
            return Collections.emptyList();
        }
    }

    private List<DomainEvent> joinGameIfNotAlreadyPlaying(PlayerId playerId) {
        if (playerId.equals(playerId1) || playerId.equals(playerId2)) {
            return Collections.emptyList();
        }

        List<DomainEvent> events = new ArrayList<>();
        if (playerId1 == null) {
            events.add(FirstPlayerJoinedGame.builder().gameId(gameId).playerId(playerId).build());
            events.add(GameStarted.builder().gameId(gameId).startedBy(playerId).startedAt(new Date()).build());
        } else if (playerId2 == null) {
            events.add(SecondPlayerJoinedGame.builder().gameId(gameId).playerId(playerId).build());
        } else {
            throw new IllegalStateException("Cannot have more than 2 players");
        }
        return Collections.unmodifiableList(events);
    }

    // Event handling
    private List<DomainEvent> applyAndReturn(List<DomainEvent> events) {
        return events.stream().peek(e -> apply(this, e)).collect(Collectors.toList());
    }

    private static void apply(Game game, DomainEvent evt) {
        eventHandlers.getOrDefault(evt.getClass(), (g, __) -> g).apply(game, evt);
    }

    private static final Map<Class<? extends DomainEvent>, BiFunction<Game, DomainEvent, Game>> eventHandlers = new HashMap<>() {{
        put(GameCreated.class, coerce(GameCreated.class, (g, e) -> {
            g.gameId = e.getGameId();
            g.numberOfRoundsInGame = e.getRounds();
            g.rounds = new TreeSet<>(comparing(Round::roundNumber));
            g.state = CREATED;
        }));
        put(GameStarted.class, coerce(GameStarted.class, (g, e) -> g.state = STARTED));
        put(FirstPlayerJoinedGame.class, coerce(FirstPlayerJoinedGame.class, (g, e) -> g.playerId1 = e.getPlayerId()));
        put(SecondPlayerJoinedGame.class, coerce(SecondPlayerJoinedGame.class, (g, e) -> g.playerId2 = e.getPlayerId()));
        put(RoundStarted.class, coerce(RoundStarted.class, (g, e) -> g.rounds.add(new Round(e.getRoundNumber()))));
        put(MoveMade.class, coerce(MoveMade.class, (g, e) -> g.rounds.stream().filter(round -> round.roundNumber() == e.getRound()).findFirst().ifPresent(round -> round.apply(e))));
        put(GameWon.class, coerce(GameWon.class, (g, e) -> g.state = WON));
        put(GameTied.class, coerce(GameTied.class, (g, e) -> g.state = TIED));
    }};

    // Ugly Java boiler plate hack to get types to align
    @SuppressWarnings({"unchecked", "unused"})
    private static <T extends DomainEvent> BiFunction<Game, DomainEvent, Game> coerce(Class<T> eventType, BiConsumer<Game, T> coercedConsumer) {
        return (Game game, DomainEvent e) -> {
            coercedConsumer.accept(game, (T) e);
            return game;
        };
    }
}