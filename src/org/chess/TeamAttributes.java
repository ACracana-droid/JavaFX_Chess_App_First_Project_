package org.chess;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public enum TeamAttributes {
    WHITE_TEAM(new ArrayList<>(20), new ArrayList<>(20), ChessBoard.BOARD_SIZE - 1,
            Color.WHITESMOKE, "White's\nTurn") {
    },
    BLACK_TEAM(new ArrayList<>(20), new ArrayList<>(20), 0,
            Color.DARKGREEN, "Black's\nTurn") {
    };

    public final List<GraphicPiece> pieces;
    public final List<GraphicPiece> capturedPieces;
    public final int PROMOTION_RANK;
    public final Color paint;
    public final String turnLabelText;
    public King king;

    TeamAttributes(List<GraphicPiece> ls, List<GraphicPiece> ls1, int PROMOTION_RANK, Color paint, String turnLabelText) {
        king = null;
        this.pieces = new ArrayList<>();
        this.capturedPieces = new ArrayList<>();
        this.PROMOTION_RANK = PROMOTION_RANK;
        this.paint = paint;
        this.turnLabelText = turnLabelText;
    }
}
