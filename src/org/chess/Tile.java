package org.chess;

import javafx.scene.paint.Color;
import org.chess.SpecialMovePiece.SpecialProperties;

public class Tile {


    public enum ChessTileColours {
        WHITE_BOARD(Color.ANTIQUEWHITE, Color.ORANGERED, Color.BLUE) {
        },
        BLACK_BOARD(Color.DARKGREEN, Color.ORANGERED, Color.BLUE) {
        };

        public final Color paint;
        public final Color kill;
        public final Color move;

        ChessTileColours(Color colour, Color kill, Color move) {
            this.paint = colour;
            this.kill = kill;
            this.move = move;
        }
    }


    Piece piece;

    SpecialProperties property;


    Tile() {
        property = SpecialProperties.DEFAULT;
    }


//    public void overridePiece(Piece override) {
//        deleteSprite();
//        deletePiece();
//        if (override != null) addPiece(override, override.coOrds);
//    }

    public void addPiece(Piece newPiece, int[] coOrds) {
        this.piece = newPiece;
        this.piece.setCoOrds(coOrds);

//        pane.getChildren().add(piece.sprite);
    }

    public void addNewPiece(Piece newPiece, int[] coOrds) {
        this.piece = newPiece;
        this.piece.setCoOrds(coOrds);
    }

    public void superficialAddPiece(Piece newPiece, int[] coOrds) {
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

    }

}

