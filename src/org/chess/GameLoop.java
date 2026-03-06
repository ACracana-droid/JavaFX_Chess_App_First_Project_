package org.chess;

import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.chess.Pawn.getDiagonalCaptures;
import static org.chess.TeamAttributes.*;

public class GameLoop extends ChessBoard {

    static private TeamAttributes alliedTeam;
    static private TeamAttributes enemyTeam;
    static private Piece lastMovedEnemyPiece = null;
    static private int turnCount;
    static private List<Move> shownMoves = null;
    static public StackPane hidden;


    enum BoardState {
        DEFAULT, CHECK, CHECKMATE
    }

    GameLoop() {
        turnCount = 0;
        hidden = new StackPane();
        alliedTeam = WHITE_TEAM;
        enemyTeam = BLACK_TEAM;

    }

    static public Move isMove(Tile clicked) {
        if (shownMoves == null) throw new UnsupportedOperationException("NO. Should not happen!!!!");
        for (Move move : shownMoves) {
            if (clicked.equals(graphicBoard[move.getRow()][move.getCol()])) {
                return move;
            }
        }
        return null;
    }


    // modern stuff
    private static Piece orig_piece;
    private static Piece dest_piece;

    public static List<Piece> virtualMovePiece(int[] origin, Move move, Tile[][] board) {

        List<Piece> virtualEnemyPieceList = new ArrayList<>(enemyTeam.pieces);

        Tile originTile = getTile(origin, board);
        Tile destTile = getTile(move, board);
        orig_piece = originTile.piece;
        dest_piece = destTile.piece;

        originTile.deletePiece();
        originTile.deselect();
//        orig_piece.updateNecessaryFirstMoveInfo();

        if (destTile.hasPiece()) { //// must be a capture
//            getAlliedTeam().capturedPieces.add(destTile.piece); // can add to display.
            virtualEnemyPieceList.remove(destTile.piece);
            destTile.deletePiece();
        }
        destTile.addNewPiece(orig_piece, move);
        if (orig_piece.isPawn()) {

//            if (destTile.isProperty(SpecialProperties.EN_PASSANT))
            if (move.property != null && dest_piece != null && dest_piece.matchesProperty(SpecialProperties.EN_PASSANT)) {
                Piece passedPiece = getTile(lastMovedEnemyPiece.coOrds, board).piece;
//                getAlliedTeam().capturedPieces.add(passedPiece);
                virtualEnemyPieceList.remove(passedPiece);
                getTile(lastMovedEnemyPiece.coOrds, board).deletePiece();
            }
        }
        return virtualEnemyPieceList;
    }

    public static void undoMovePiece(Tile[][] board) { // requires work if to be used.
        getTile(orig_piece.coOrds, board).addPiece(orig_piece, orig_piece.coOrds);

        if (orig_piece.isPawn() && dest_piece != null && getTile(dest_piece.coOrds, board) != null) {
            getTile(lastMovedEnemyPiece.coOrds, board).addPiece(lastMovedEnemyPiece, lastMovedEnemyPiece.coOrds);
        } else if (dest_piece != null) getTile(dest_piece.coOrds, board).addPiece(dest_piece, dest_piece.coOrds);
    }


    static public void movePiece(Tile originalLocation, Tile nextLocation, Move move) {
        System.out.println("MOVE!");
        Piece piece = originalLocation.piece;
        originalLocation.deletePiece();
        originalLocation.deselect();


        if (nextLocation.hasPiece()) { //// must be a capture
            System.out.println("KILL!");
            getAlliedTeam().capturedPieces.add(nextLocation.piece); // can add to display.
            enemyTeam.pieces.remove(nextLocation.piece);
            nextLocation.deletePiece();
        }

        nextLocation.addNewPiece(piece, move.getCoOrds());


        if (piece.isPawn()) {
            if (move.getRow() == alliedTeam.PROMOTION_RANK) {
                System.out.println("PROMOTE!!");
                new Promotion(piece, root);
                //// function will call updateGameLoop() - it must in order to compensate for how java runs threads.
                lastMovedEnemyPiece = piece;
                return;
            }
            if (piece.property == SpecialProperties.EN_PASSANT) {
                piece.property = SpecialProperties.DEFAULT;
            }
            if (move.property != null) {
                switch (move.property) {
                    case PAWN_DOUBLE_STEP -> piece.property = SpecialProperties.EN_PASSANT;
                    case EN_PASSANT -> {
                        System.out.println("EN PASSANT!!!!");
                        Piece passedPiece = getVisualTile(lastMovedEnemyPiece.coOrds).piece;
                        getAlliedTeam().capturedPieces.add(passedPiece);
                        enemyTeam.pieces.remove(passedPiece);

                        System.out.println(Arrays.toString(lastMovedEnemyPiece.coOrds));
                        getVisualTile(lastMovedEnemyPiece.coOrds).deletePiece();
                        piece.property = SpecialProperties.DEFAULT;
                    }
                }
            }


        }
        piece.updateNecessaryFirstMoveInfo();
        lastMovedEnemyPiece = piece;
        updateGameLoop();
    }


    /// shouldn't be graphic but alas.
    public static boolean isNotThreatToKing(List<Piece> enemyTeam, int[] kingCoOrds, Tile[][] board) {
        for (Piece piece : enemyTeam) {
            if (piece.isPawn()) {
                for (Move move : getDiagonalCaptures((Pawn) piece, board)) {
                    if (move.getRow() == kingCoOrds[0] && move.getCol() == kingCoOrds[1]) {
                        return false;
                    }
                }
            } else {
                for (Move move : piece.moves(board)) {
                    if (move.getRow() == kingCoOrds[0] && move.getCol() == kingCoOrds[1]) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    static public void showMoves(List<Move> moves) {
        System.out.println("showing moves: ");

        for (Move move : moves) {
            if (move.property == SpecialProperties.EN_PASSANT) {  // splitting the decisions...
                move.getGraphicTile(graphicBoard).showCapture();
            } else {
                move.getGraphicTile(graphicBoard).showStandardLocationHighlight();
            }
            System.out.println(move);
        }
        shownMoves = moves;
        System.out.println();
        //// List must be tracked so moves can be hidden.
    }

//    static public void showMoves(List<int[]> moves, SpecialProperties property) {
//        switch (property) {
//            case PROMOTABLE:
//                for (int[] move : moves) {
//                    if (move[0] == alliedTeam.PROMOTION_RANK) {
//                        graphicBoard[move[0]][move[1]].showPromotionHighlight();
//                    } else {
//                        graphicBoard[move[0]][move[1]].showStandardLocationHighlight();
//                    }
//                    System.out.println(Arrays.toString(move));
//                }
//                shownMoves = moves;
//                break;
//            case CHECK_AND_MATE:
//
//                break;
//            default:
//                showMoves(moves);
//        }
//    }


    static public void hideMoves() {
        if (shownMoves == null) return; //// no visible moves to hide
        for (Move move : shownMoves) {
            move.getGraphicTile(graphicBoard).clearTileToNormalState();
        }
    }

    static public void updateGameLoop() {
        turnCount++;
        turnProperty.set(!turnProperty.getValue());
        TeamAttributes swap = alliedTeam;
        alliedTeam = enemyTeam;
        enemyTeam = swap;

        switch (getBoardState(alliedTeam.pieces)) {
            case CHECKMATE -> {
                checkLabel.setText("CHECKMATE!");
                endChessGame();
                return;
            }
            case CHECK -> {
                checkLabel.setText("CHECK!");
            }
            case DEFAULT -> {
                checkLabel.setText("");
            }
        }
//        checkLabel.setStyle("""
//                fx-font: 100px "Arial";
//                fx-fill: light blue;
//                """);


        turnLabel.setText(alliedTeam.turnLabelText);
        turnCountLabel.setText("Turn: " + turnCount);
    }

    private static void endChessGame() {
        System.out.println("END GAME!");

    }

    private static BoardState getBoardState(List<Piece> pieceList) {
        List<Move> validMoves = new ArrayList<>();
        resetVirtualBoardToGraphicState();


        for (Piece piece : pieceList) {
            validMoves.addAll(getTile(piece.coOrds, virtualBoard).getValidMoves(piece.coOrds));
        }


        if (validMoves.isEmpty()) {
            return BoardState.CHECKMATE;
        }
        if (!isNotThreatToKing(enemyTeam.pieces, getAlliedTeam().king.coOrds, virtualBoard)) {
            return BoardState.CHECK;
        }

        return BoardState.DEFAULT;
    }


    static void setCheckFlag() {
        //useless, can get rid.
    }

    static public TeamAttributes getAlliedTeam() {
        return alliedTeam;
    }

    static public TeamAttributes getEnemyTeam() {
        return enemyTeam;
    }

    public static Piece getPrevEnemyPiece() {
        return lastMovedEnemyPiece;
    }


//        if (lastMovedEnemyPiece == null) return false;
//        Tile current = getPrevSelected();
//        if (current == null || !current.hasPiece() || !current.piece.isPawn()) return false;
//        System.out.println("FIRST PART OF EN PASSANT PASSED!!");
//
//        //// EN PASSANT!
//        return lastMovedEnemyPiece.getVulnerableToEnPassant()
//                && current.piece.coOrds[0] == lastMovedEnemyPiece.coOrds[0]
//                && Math.abs(current.piece.coOrds[1] - lastMovedEnemyPiece.coOrds[1]) == 1; // when this happens, the condition PAWN_DOUBLE_STEP is permanently impossible to get. removed.


    public static boolean isEnPassantValid(int[] coOrds, Tile[][] board) {
        if (lastMovedEnemyPiece == null) return false;
        Tile current = getTile(coOrds, board);
        if (current == null || !current.hasPiece() || !current.piece.isPawn()) return false;

        //// EN PASSANT!
        return current.piece.coOrds[0] == lastMovedEnemyPiece.coOrds[0]
                && lastMovedEnemyPiece.matchesProperty(SpecialProperties.EN_PASSANT)
                && Math.abs(current.piece.coOrds[1] - lastMovedEnemyPiece.coOrds[1]) == 1;
    }

    public static GraphicTile getVisualTile(int[] coOrds) {
        return graphicBoard[coOrds[0]][coOrds[1]];
    }

    public static GraphicTile getVisualTile(Piece piece) {
        return graphicBoard[piece.coOrds[0]][piece.coOrds[1]];
    }

    public static Tile getTile(int[] coOrds, Tile[][] board) {
        return board[coOrds[0]][coOrds[1]];
    }


    public static Tile getTile(Move move, Tile[][] board) {
        return board[move.getRow()][move.getCol()];
    }

}
