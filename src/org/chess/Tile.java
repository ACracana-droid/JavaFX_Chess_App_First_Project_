package org.chess;

import java.util.ArrayList;
import java.util.List;

import static org.chess.ChessBoard.*;
import static org.chess.ChessBoard.resetVirtualBoardToGraphicState;
import static org.chess.ChessBoard.virtualBoard;
import static org.chess.ChessLoop.*;
import static org.chess.ChessLoop.getAlliedTeam;

public class Tile {

    Piece piece;

    static Tile getTileCopy(Tile tile, int i, int j) {
        Tile copy = new Tile();
        if (tile.hasPiece()) {
            copy.addNewPiece(tile.piece, new int[]{i, j});
        }
        return copy;
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
        if (this.piece.equals(getAlliedTeam().getKing())) {
            for (Move move : moves) {
                List<Piece> virtualEnemyList = ChessLoop.virtualMovePiece(origin, move, virtualBoard);
                if (isNotThreatToKing(virtualEnemyList, move.getCoOrds(), virtualBoard)) {
                    validMoves.add(move);
                }
                resetVirtualBoardToGraphicState();
            }

        } else {
            for (Move move : moves) {
                List<Piece> virtualEnemyList = ChessLoop.virtualMovePiece(origin, move, virtualBoard);
                if (isNotThreatToKing(virtualEnemyList, getAlliedTeam().getKing().coOrds, virtualBoard)) {
                    validMoves.add(move);
                }
                resetVirtualBoardToGraphicState();
            }
        }
        return validMoves;
    }


    public boolean hasPiece() {
        return piece != null;
    }

    public boolean hasAlliedTeam() {
        return hasPiece() && piece.team.equals(ChessLoop.getAlliedTeam());
    }

    /// DO NOT CHANGE FOOL!
    public boolean isPossibleMove(Team colour) {
        return !hasPiece() || !this.piece.team.equals(colour);
    }

    public boolean hasEnemyPiece() {
        return hasPiece() && !piece.team.equals(ChessLoop.getAlliedTeam());
    }

    public void deselect() {

    }

    public void deletePiece() {
        piece = null;
    }

}

