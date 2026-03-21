package org.chess;


import java.util.ArrayList;
import java.util.List;

public enum Team {
    WHITE_TEAM(ChessBoard.BOARD_SIZE - 1, "White's\nTurn") {
    },
    BLACK_TEAM(0, "Black's\nTurn") {
    };

    public final List<Piece> pieces;
    public final List<Piece> capturedPieces;
    public final int PROMOTION_RANK;
    public final String turnLabelText;
    private King king;

    Team(int PROMOTION_RANK, String turnLabelText) {
        setKing(null);
        this.pieces = new ArrayList<>();
        this.capturedPieces = new ArrayList<>();
        this.PROMOTION_RANK = PROMOTION_RANK;
        this.turnLabelText = turnLabelText;
    }

    public King getKing() {
        return king;
    }

    public void setKing(King king) {
        this.king = king;
    }
}
