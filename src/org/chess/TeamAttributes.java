package org.chess;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public enum TeamAttributes {
    WHITE_TEAM(new ArrayList<>(20), new ArrayList<>(20), ChessBoard.BOARD_SIZE - 1, "White's\nTurn") {
    },
    BLACK_TEAM(new ArrayList<>(20), new ArrayList<>(20), 0, "Black's\nTurn") {
    };
    // legacy: darkgreen and whitesmoke

    public final List<Piece> pieces;
    public final List<Piece> capturedPieces;
    public final int PROMOTION_RANK;
    public final String turnLabelText;
    public King king;

    TeamAttributes(List<Piece> ls, List<Piece> ls1, int PROMOTION_RANK, String turnLabelText) {
        king = null;
        this.pieces = new ArrayList<>();
        this.capturedPieces = new ArrayList<>();
        this.PROMOTION_RANK = PROMOTION_RANK;
        this.turnLabelText = turnLabelText;
    }
}
