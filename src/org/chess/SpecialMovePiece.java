package org.chess;

public abstract class SpecialMovePiece extends Piece {
    /// / needed for castling and pawn rules
    public boolean firstMove;
    protected SpecialProperties property;

    public static enum SpecialProperties {
        DEFAULT,
        PROMOTABLE,
        PAWN_DOUBLE_STEP, //// this determines whether en passant is valid.
        EN_PASSANT,
        CHECK_AND_MATE,
        CASTLE
    }

    @Override
    public void updateNecessaryFirstMoveInfo() {
        firstMove = false;
    }


    @Override
    public boolean isSpecial() {
        return true;
    }

    SpecialMovePiece(TeamAttributes colour) {
        super(colour);
        firstMove = true;
    }
}
