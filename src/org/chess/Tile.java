package org.chess;

import org.chess.SpecialProperties;

public class Tile {

    GraphicPiece piece;
    SpecialProperties property;

    Tile() {
        property = SpecialProperties.DEFAULT;
    }


    public void overridePiece(GraphicPiece override) {
        deletePiece();
        if (override != null) addPiece(override, override.coOrds);
    }

    public void addPiece(GraphicPiece newPiece, int[] coOrds) {
        this.piece = newPiece;
        this.piece.setCoOrds(coOrds);

//        pane.getChildren().add(piece.sprite);
    }

    public void addNewPiece(GraphicPiece newPiece, int[] coOrds) {
        this.piece = newPiece;
        this.piece.setCoOrds(coOrds);
    }

    public void superficialAddPiece(GraphicPiece newPiece, int[] coOrds) {
        this.piece = newPiece;
//        this.piece.setCoOrds(coOrds);
    }


    public void superficialDeletePiece() {
        this.piece = null;
    }


    public boolean hasPiece() {
        return piece != null;
    }

    public boolean hasAlliedTeam() {
        return hasPiece() && piece.teamColour.equals(GameLoop.getAlliedTeam());
    }

    public boolean hasTeamOfThisColour(TeamAttributes colour) {
        return hasPiece() && this.piece.teamColour.equals(colour);
    }

    public boolean hasEnemyPiece() {
        return hasPiece() && !piece.teamColour.equals(GameLoop.getAlliedTeam());
    }


    public boolean isEnPassant() {
        return property.equals(SpecialProperties.EN_PASSANT);
    }

    public void setIsEnPassant() {
        property = SpecialProperties.EN_PASSANT;
    }

    public boolean isProperty(SpecialProperties property) {
        return this.property.equals(property);
    }

    public void setProperty(SpecialProperties property) {
        this.property = property;
    }

    public void deselect() {

    }

    public void deletePiece() {
        piece = null;
    }

}

