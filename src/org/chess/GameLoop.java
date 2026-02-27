package org.chess;

import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.chess.Pawn.getDiagonalCaptures;
import static org.chess.TeamAttributes.*;

public class GameLoop extends ChessBoard {

    static private TeamAttributes alliedTeam;
    static private TeamAttributes enemyTeam;
    static private GraphicPiece lastMovedEnemyPiece = null;
    static private int turnCount;
    static private List<int[]> shownMoves = null;
    static public StackPane hidden;

    static public List<int[][]> validMoves = null;

    GameLoop() {
        turnCount = 0;
        hidden = new StackPane();
        alliedTeam = WHITE_TEAM;
        enemyTeam = BLACK_TEAM;

    }

    static public int[] isMove(Tile clicked) {
        if (shownMoves == null) throw new UnsupportedOperationException("NO. Should not happen!!!!");
        for (int[] move : shownMoves) {
            if (clicked.equals(graphicBoard[move[0]][move[1]])) {
                return move;
            }
        }
        return new int[]{};
    }

    // legacy stuff
    private static GraphicPiece originalPiece;
    private static GraphicPiece nextPiece;

    // modern stuff
    private static GraphicPiece orig_piece;
    private static GraphicPiece dest_piece;

    public static void virtualMovePiece(int[] origin, int[] dest, Tile[][] board) {
        System.out.println("MOVE!");

        Tile originTile = getTile(origin, board);
        Tile destTile = getTile(dest, board);
        orig_piece = originTile.piece;
        dest_piece = destTile.piece;

        originTile.deletePiece();
        originTile.deselect();
//        orig_piece.updateNecessaryFirstMoveInfo();

        if (destTile.hasPiece()) { //// must be a capture
            System.out.println("KILL!");
//            getAlliedTeam().capturedPieces.add(destTile.piece); // can add to display.
//            enemyTeam.pieces.remove(destTile.piece);
            destTile.deletePiece();
        }

        if (orig_piece.isPawn()) {
//            if (dest[0] == alliedTeam.PROMOTION_RANK) {
//                System.out.println("PROMOTE!!");
//                new Promotion(orig_piece, root);
//                //// function will call updateGameLoop() - it must in order to compensate for how java runs threads.
////                lastMovedEnemyPiece = orig_piece;
//                return;
//            }
            if (destTile.isEnPassant()) {
                System.out.println("EN PASSANT!!!!");
                GraphicPiece passedPiece = getTile(lastMovedEnemyPiece.coOrds, board).piece;
//                getAlliedTeam().capturedPieces.add(passedPiece);
//                enemyTeam.pieces.remove(passedPiece);
                getTile(lastMovedEnemyPiece.coOrds, board).deletePiece();
            }
        }
    }

    public static void undoMovePiece(Tile[][] board) { // eh
        getTile(orig_piece.coOrds, board).addPiece(orig_piece, orig_piece.coOrds);

        if (orig_piece.isPawn() && dest_piece != null && getTile(dest_piece.coOrds, board).isEnPassant()) {
            getTile(lastMovedEnemyPiece.coOrds, board).addPiece(lastMovedEnemyPiece, lastMovedEnemyPiece.coOrds);
        } else if (dest_piece != null) getTile(dest_piece.coOrds, board).addPiece(dest_piece, dest_piece.coOrds);
    }


    static public void movePiece(Tile originalLocation, Tile nextLocation, int[] nextCoOrds) {
        System.out.println("MOVE!");
        GraphicPiece piece = originalLocation.piece;
        originalLocation.deletePiece();
        originalLocation.deselect();
        piece.updateNecessaryFirstMoveInfo();


        if (nextLocation.hasPiece()) { //// must be a capture
            System.out.println("KILL!");
            getAlliedTeam().capturedPieces.add(nextLocation.piece); // can add to display.
            enemyTeam.pieces.remove(nextLocation.piece);
            nextLocation.deletePiece();
        }

        nextLocation.addNewPiece(piece, nextCoOrds);

        //honestly, this is better than using my SpecialProperties enum
        if (piece.isPawn()) {
            if (nextCoOrds[0] == alliedTeam.PROMOTION_RANK) {
                System.out.println("PROMOTE!!");
                new Promotion(piece, root);
                //// function will call updateGameLoop() - it must in order to compensate for how java runs threads.
                lastMovedEnemyPiece = piece;
                return;
            }
            if (nextLocation.isEnPassant()) {
                System.out.println("EN PASSANT!!!!");
                GraphicPiece passedPiece = getVisualTile(lastMovedEnemyPiece.coOrds).piece;
                getAlliedTeam().capturedPieces.add(passedPiece);
                enemyTeam.pieces.remove(passedPiece);

                System.out.println(Arrays.toString(lastMovedEnemyPiece.coOrds));
                // enemy enpassantable pawn
                getVisualTile(lastMovedEnemyPiece.coOrds).deletePiece();
            }
        }

        updateGameLoop();
        lastMovedEnemyPiece = piece;
    }

    /// shouldn't be graphic but alas.
    public static boolean isThreatToKing(List<GraphicPiece> enemyTeam, int[] kingCoOrds, Tile[][] board) {
        for (GraphicPiece piece : enemyTeam) {
            if (piece.isPawn()) {
                for (int[] move : getDiagonalCaptures((Pawn) piece, board)) {
                    if (move[0] == kingCoOrds[0] && move[1] == kingCoOrds[1]) {
                        System.out.println("King: ahh~ stop threatening me");
                        return true;
                    }
                }
            } else {
                for (int[] move : piece.moves(board)) {
                    if (move[0] == kingCoOrds[0] && move[1] == kingCoOrds[1]) {
                        System.out.println("King: ahh~ stop threatening me");
                        return true;
                    }
                }
            }
        }

        return false;
    }

//    static public List<int[]> removeMoveIfResultsInCheck(Tile origin, List<int[]> moves) {
//        List<int[]> ls = new ArrayList<>();
//        for (int[] move : moves) {
//            superficialMovePiece(origin, getBoardTile(move), move);
//            if (!isCheck(enemyTeam.pieces, alliedTeam.king)) { // if the enemy team is checking the current user
//                ls.add(move);
//            }
//            System.out.println("ORIGINAL PIECE: " + originalPiece + " AND NEXT PIECE: " + nextPiece);
//            undoSuperficialMove(origin, getBoardTile(move));
//        }
//
//        return ls;
//    }

//    static public void superficialMovePiece(Tile originalLocation, Tile nextLocation, int[] nextCoOrds) {
//        // save pieces to later memory.
//        originalPiece = originalLocation.piece;
//        nextPiece = nextLocation.piece;
//        System.out.println("s-MOVE!");
//        originalLocation.superficialDeletePiece();
//
//
//        if (nextLocation.hasPiece()) { //// must be a capture
//            System.out.println("s-KILL!");
//            nextLocation.superficialDeletePiece();
//        }
//        nextLocation.superficialAddPiece(originalPiece, nextCoOrds);
//
//        if (originalPiece.isPawn() && nextLocation.isEnPassant()) {
//            System.out.println("s-EN PASSANT!!!!");
//            getBoardTile(lastMovedEnemyPiece.coOrds).superficialDeletePiece();
//        }
//
//    }

//    private static void undoSuperficialMove(Tile origin, Tile moveLocation) {
//        origin.superficialAddPiece(originalPiece, originalPiece.coOrds);
//
//        moveLocation.superficialDeletePiece();
//
//        if (originalPiece.isPawn() && moveLocation.isEnPassant()) {
//            getBoardTile(lastMovedEnemyPiece.coOrds).superficialAddPiece(nextPiece, nextPiece.coOrds);
//        } else if (nextPiece != null) moveLocation.superficialAddPiece(nextPiece, nextPiece.coOrds);
//        System.out.println("s-UNDID THAT MOVE");
//    }


    static public void showMoves(List<int[]> moves) {
        System.out.println();

        System.out.println("showing moves: ");

        for (int[] move : moves) {
            graphicBoard[move[0]][move[1]].showStandardLocationHighlight();
            System.out.println(Arrays.toString(move));
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
        for (int[] move : shownMoves) {
            graphicBoard[move[0]][move[1]].clearTileToNormalState();
        }
    }

    static public void updateGameLoop() {

//        if (isCheck(alliedTeam.pieces, enemyTeam.king)) {
//            if (isCheckmate(alliedTeam.pieces, enemyTeam.pieces, enemyTeam.king)) {
//                checkLabel.setText("CHECKMATE!");
//                // do something
//                return;
//            }
//            checkLabel.setText("CHECK");
//            validMoves = getValidMoves(alliedTeam.pieces, enemyTeam.pieces, enemyTeam.king);
//
//        }


        turnCount++;
        TeamAttributes swap = alliedTeam;
        alliedTeam = enemyTeam;
        enemyTeam = swap;

        turnLabel.setText(alliedTeam.turnLabelText);
        turnCountLabel.setText("Turn: " + turnCount);


        boardView.setBackground(new Background(
                new BackgroundFill(alliedTeam.paint, new CornerRadii(20), Insets.EMPTY)
        ));
    }

//    private static List<int[][]> getValidMoves(List<Piece> allies, List<Piece> enemies, Piece enemyKing) {
//        List<int[][]> validMoves = new ArrayList<>();
//        for (Piece enemy : enemies) {
//            int[] origCoOrds = enemy.coOrds;
//
//            for (int[] move : enemy.moves(boardArray)) {
//                superficialMovePiece(getBoardTile(origCoOrds), getBoardTile(move), move);
//                if (!isCheck(allies, enemyKing)) {
//                    validMoves.add(new int[][]{origCoOrds, move});
//                }
//                undoSuperficialMove(getBoardTile(origCoOrds), getBoardTile(move));
//            }
//            getBoardTile(origCoOrds).piece = enemy;
//        }
//
//        // shouldn't be here.
//        return validMoves;
//    }


    static public List<int[]> removeMoveIfNotValid(Tile origTile, List<int[]> moves) {
        if (validMoves == null) return moves;
        int[] origin = origTile.piece.coOrds;
        List<int[]> ls = new ArrayList<>();
        for (int[][] moveBundle : validMoves) {
            if (Arrays.equals(moveBundle[0], origin)) {
                ls.add(moveBundle[1]);
            }
        }
        return ls;
    }


//    private static boolean isCheck(List<Piece> allies, Piece enemyKing) {
//
//        for (Piece piece : allies) {
//            if (piece.isPawn()) { // different because some moves do not capture
//                for (int[] move : Pawn.getDiagonalCaptures((Pawn) piece)) {
////                    virtualBoard[move[0]][move[1]].pane.setBackground(Background.fill(Color.RED));
//
//                    if (move[0] == enemyKing.coOrds[0] && move[1] == enemyKing.coOrds[1]) {
//
//                        System.out.println(piece + " is causing check.");
//                        getVisualTile(piece).showPromotionHighlight();
//
//                        return true;
//                    }
//                }
//            } else {
//                for (int[] move : piece.moves(visualBoard)) {

    /// /                    virtualBoard[move[0]][move[1]].pane.setBackground(Background.fill(Color.RED));
//
//                    if (move[0] == enemyKing.coOrds[0] && move[1] == enemyKing.coOrds[1]) {
//
//                        System.out.println(piece + " is causing check.");
//                        getVisualTile(piece).showPromotionHighlight();
//
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }


//    private static boolean isCheckmate(List<Piece> allies, List<Piece> enemies, Piece enemyKing) {
//        System.out.println("CHECKING FOR MATE");
//
//        for (Piece enemy : enemies) {
//            int[] origCoOrds = enemy.coOrds;
//
//            for (int[] move : enemy.moves(boardArray)) {
//                superficialMovePiece(getBoardTile(origCoOrds), getBoardTile(move), move);
//                if (!isCheck(allies, enemyKing)) {
//                    System.out.println("THIS WASN'T MATE");
//                    System.out.println(enemy.id + "  " + Arrays.toString(move));
//                    return false;
//                }
//                undoSuperficialMove(getBoardTile(origCoOrds), getBoardTile(move));
//            }
//            getBoardTile(origCoOrds).piece = enemy;
//        }
//        System.out.println("THIS IS MATE!!!");
//        return true;
//    }
    private static List<int[]> getAllCaptureMoves(List<GraphicPiece> pieceList) {
        List<int[]> ls = new ArrayList<>();
        for (GraphicPiece piece : pieceList) {
            if (piece.isPawn()) { // different because some moves do not capture
                ls.addAll(getDiagonalCaptures((Pawn) piece, virtualBoard));
            } else {
                ls.addAll(piece.moves(graphicBoard));
            }
        }
        return ls;
    }


    static public TeamAttributes getAlliedTeam() {
        return alliedTeam;
    }

    static public TeamAttributes getEnemyTeam() {
        return enemyTeam;
    }

    public static GraphicPiece getPrevEnemyPiece() {
        return lastMovedEnemyPiece;
    }

    public static boolean isEnPassantValid(int[] alliedPawnOrigin) {
        if (lastMovedEnemyPiece == null) return false;

        //// EN PASSANT!
        return lastMovedEnemyPiece.isPawn()
                && getVisualTile(lastMovedEnemyPiece.coOrds).isProperty(SpecialProperties.PAWN_DOUBLE_STEP)
                && alliedPawnOrigin[0] == lastMovedEnemyPiece.coOrds[0]
                && Math.abs(alliedPawnOrigin[1] - lastMovedEnemyPiece.coOrds[1]) == 1; // when this happens, the condition PAWN_DOUBLE_STEP is permanently impossible to get. removed.
    }

    public static VisualTile getVisualTile(int[] coOrds) {
        return graphicBoard[coOrds[0]][coOrds[1]];
    }

    public static VisualTile getVisualTile(GraphicPiece piece) {
        return graphicBoard[piece.coOrds[0]][piece.coOrds[1]];
    }

    public static Tile getTile(int[] coOrds, Tile[][] board) {
        return board[coOrds[0]][coOrds[1]];
    }


}
