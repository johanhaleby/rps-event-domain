package se.haleby.rps.domain.model;

import lombok.Data;
import lombok.experimental.Accessors;
import se.haleby.rps.domain.event.DomainEvent;
import se.haleby.rps.domain.event.MoveMade;
import se.haleby.rps.domain.event.RoundEnded;
import se.haleby.rps.domain.event.RoundTied;
import se.haleby.rps.domain.event.RoundWon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@Accessors(fluent = true)
class Round {
    private static final int NUMBER_OF_MOVES_PER_ROUND = 2;
    private final int roundNumber;
    private List<PlayerMove> moves = new ArrayList<>(NUMBER_OF_MOVES_PER_ROUND);

    boolean isEnded() {
        return moves.size() == NUMBER_OF_MOVES_PER_ROUND;
    }

    PlayerId winner() {
        return isEnded() ? null : determineWinnerFromMoves(moves.get(0), moves.get(1));
    }

    List<DomainEvent> play(GameId gameId, PlayerId playerId, Move move) {
        if (hasPlayed(playerId)) {
            throw new IllegalArgumentException("Player " + playerId.uuid + " has already played on this round");
        } else if (isEnded()) {
            throw new IllegalArgumentException("Round has already ended");
        }

        PlayerMove playerMove = PlayerMove.make(playerId, move);
        List<DomainEvent> events = new ArrayList<>();
        events.add(MoveMade.builder().gameId(gameId).playerId(playerMove.playerId).move(playerMove.move).round(roundNumber).build());

        int numberOfMovesWhenThisMovesHasBeenMade = moves.size() + 1;
        if (numberOfMovesWhenThisMovesHasBeenMade == NUMBER_OF_MOVES_PER_ROUND) {
            PlayerId winnerId = determineWinnerFromMoves(moves.get(0), playerMove);
            if (winnerId != null) {
                events.add(RoundWon.builder().gameId(gameId).roundNumber(roundNumber).winnerId(winnerId).build());
            } else {
                events.add(RoundTied.builder().gameId(gameId).roundNumber(roundNumber).build());
            }
            events.add(RoundEnded.builder().gameId(gameId).roundNumber(roundNumber).build());
        }

        return Collections.unmodifiableList(events);
    }


    void apply(MoveMade move) {
        PlayerMove playerMove = PlayerMove.make(move.getPlayerId(), move.getMove());
        moves.add(playerMove);
    }

    private boolean hasPlayed(PlayerId player) {
        return moves.stream().anyMatch(playerMove -> playerMove.isMadeBy(player));
    }

    private PlayerId determineWinnerFromMoves(PlayerMove firstMoveInRound, PlayerMove secondMoveInRound) {
        if (firstMoveInRound.move() == secondMoveInRound.move()) {
            return null;
        } else if (firstMoveInRound.beats(secondMoveInRound)) {
            return firstMoveInRound.playerId;
        } else {
            return secondMoveInRound.playerId;
        }
    }

    @Accessors(fluent = true)
    @Data
    private static class PlayerMove {
        private PlayerId playerId;
        private Move move;

        private static PlayerMove make(PlayerId playerId, Move move) {
            return new PlayerMove().move(move).playerId(playerId);
        }

        private boolean isMadeBy(PlayerId playerId) {
            return this.playerId == playerId;
        }

        private boolean beats(PlayerMove other) {
            return this.move.beats(other.move);
        }
    }
}