package se.haleby.rps.domain.model;

public enum Move {
    ROCK, PAPER, SCISSORS;

    public boolean beats(Move otherMove) {
        switch (this) {
            case ROCK:
                return otherMove == SCISSORS;
            case PAPER:
                return otherMove == ROCK;
            case SCISSORS:
                return otherMove == PAPER;
            default:
                return false;
        }
    }
}