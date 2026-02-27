package org.chess;

public enum SpecialProperties {
    DEFAULT,
    PROMOTABLE,
    PAWN_DOUBLE_STEP,
    /// / this determines whether en passant is valid.
    EN_PASSANT,
    CHECK_AND_MATE,
    CASTLE
}