package org.chess;

import java.util.ArrayList;
import java.util.List;

import static org.chess.ChessBoard.*;
import static org.chess.ChessBoard.resetVirtualBoardToGraphicState;
import static org.chess.ChessBoard.virtualBoard;
import static org.chess.GameLoop.*;
import static org.chess.GameLoop.getAlliedTeam;

public class Tile {

    Piece piece;
    SpecialProperties property;

//    Tile() {
//        property = SpecialProperties.DEFAULT;
//    }


    public void overridePiece(Piece override) {
        deletePiece();
        if (override != null) addPiece(override, override.coOrds);
    }

    static Tile getTileCopy(Tile tile, int i, int j) {
        Tile copy = new Tile();
        if (tile.hasPiece()) {
            copy.addNewPiece(tile.piece, new int[]{i, j});
            if (tile.property != null) {
                copy.setProperty(tile.property);
            }
        }
        return copy;
    }

    public void addPiece(Piece newPiece, int[] coOrds) {
        this.piece = newPiece;
        this.piece.setCoOrds(coOrds);

//        pane.getChildren().add(piece.sprite);
    }

    public void addNewPiece(Piece newPiece, int[] coOrds) {
        this.piece = newPiece;
        this.piece.setCoOrds(coOrds);
    }

    public void addNewPiece(Piece newPiece, Move move) {
        this.piece = newPiece;
        this.piece.setCoOrds(move.getCoOrds());
    }

    public List<Move> getValidMoves(int[] origin) {
        List<Move> moves = this.piece.moves(graphicBoard);
        List<Move> validMoves = new ArrayList<>();
        if (this.piece.equals(getAlliedTeam().king)) {
            for (Move move : moves) {
                List<Piece> virtualEnemyList = GameLoop.virtualMovePiece(origin, move, virtualBoard);
                if (isNotThreatToKing(virtualEnemyList, move.getCoOrds(), virtualBoard)) {
                    validMoves.add(move);
                }
                resetVirtualBoardToGraphicState();
            }

        } else {
            for (Move move : moves) {
                List<Piece> virtualEnemyList = GameLoop.virtualMovePiece(origin, move, virtualBoard);
                if (isNotThreatToKing(virtualEnemyList, getAlliedTeam().king.coOrds, virtualBoard)) {
                    validMoves.add(move);
                }
                resetVirtualBoardToGraphicState();
            }
        }
        return validMoves;
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

