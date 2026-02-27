package org.chess;

public interface SpecialMovePiece {
    /// / needed for castling and pawn rules
    boolean firstMove = true;

    void updateNecessaryFirstMoveInfo();

}



