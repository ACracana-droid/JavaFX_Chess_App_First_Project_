package org.chess;

import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import org.chess.SpecialMovePiece.SpecialProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.chess.TeamAttributes.*;

public class GameLoop extends ChessBoard {

    static private TeamAttributes alliedTeam;
    static private TeamAttributes enemyTeam;
    static private Piece lastMovedEnemyPiece = null;
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
            if (clicked.equals(boardArray[move[0]][move[1]])) {
                return move;
            }
        }
        return new int[]{};
    }


    private static Piece originalPiece;
    private static Piece nextPiece;

    static public void movePiece(Tile originalLocation, Tile nextLocation, int[] nextCoOrds) {
        System.out.println("MOVE!");
        Piece piece = originalLocation.piece;
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
                Piece passedPiece = getBoardTile(lastMovedEnemyPiece.coOrds).piece;
                getAlliedTeam().capturedPieces.add(passedPiece);
                enemyTeam.pieces.remove(passedPiece);

                System.out.println(Arrays.toString(lastMovedEnemyPiece.coOrds));
                // enemy enpassantable pawn
                getBoardTile(lastMovedEnemyPiece.coOrds).deletePiece();
            }
        }

        updateGameLoop();
        lastMovedEnemyPiece = piece;
    }

    static public List<int[]> removeMoveIfResultsInCheck(Tile origin, List<int[]> moves) {
        List<int[]> ls = new ArrayList<>();
        for (int[] move : moves) {
            superficialMovePiece(origin, getBoardTile(move), move);
            if (!isCheck(enemyTeam.pieces, alliedTeam.king)) { // if the enemy team is checking the current user
                ls.add(move);
            }
            System.out.println("ORIGINAL PIECE: " + originalPiece + " AND NEXT PIECE: " + nextPiece);
            undoSuperficialMove(origin, getBoardTile(move));
        }

        return ls;
    }

    static public void superficialMovePiece(Tile originalLocation, Tile nextLocation, int[] nextCoOrds) {
        // save pieces to later memory.
        originalPiece = originalLocation.piece;
        nextPiece = nextLocation.piece;
        System.out.println("s-MOVE!");
        originalLocation.superficialDeletePiece();


        if (nextLocation.hasPiece()) { //// must be a capture
            System.out.println("s-KILL!");
            nextLocation.superficialDeletePiece();
        }
        nextLocation.superficialAddPiece(originalPiece, nextCoOrds);

        if (originalPiece.isPawn() && nextLocation.isEnPassant()) {
            System.out.println("s-EN PASSANT!!!!");
            getBoardTile(lastMovedEnemyPiece.coOrds).superficialDeletePiece();
        }

    }

    private static void undoSuperficialMove(Tile origin, Tile moveLocation) {
        origin.superficialAddPiece(originalPiece, originalPiece.coOrds);

        moveLocation.superficialDeletePiece();

        if (originalPiece.isPawn() && moveLocation.isEnPassant()) {
            getBoardTile(lastMovedEnemyPiece.coOrds).superficialAddPiece(nextPiece, nextPiece.coOrds);
        } else if (nextPiece != null) moveLocation.superficialAddPiece(nextPiece, nextPiece.coOrds);
        System.out.println("s-UNDID THAT MOVE");
    }


    static public void showMoves(List<int[]> moves) {
        System.out.println();

        System.out.println("showing moves: ");

        for (int[] move : moves) {
            boardArray[move[0]][move[1]].showStandardLocationHighlight();
            System.out.println(Arrays.toString(move));
        }
        shownMoves = moves;
        System.out.println();
        //// List must be tracked so moves can be hidden.
    }

    static public void showMoves(List<int[]> moves, SpecialProperties property) {
        switch (property) {
            case PROMOTABLE:
                for (int[] move : moves) {
                    if (move[0] == alliedTeam.PROMOTION_RANK) {
                        boardArray[move[0]][move[1]].showPromotionHighlight();
                    } else {
                        boardArray[move[0]][move[1]].showStandardLocationHighlight();
                    }
                    System.out.println(Arrays.toString(move));
                }
                shownMoves = moves;
                break;
            case CHECK_AND_MATE:

                break;
            default:
                showMoves(moves);
        }
    }


    static public void hideMoves() {
        if (shownMoves == null) return; //// no visible moves to hide
        for (int[] move : shownMoves) {
            boardArray[move[0]][move[1]].clearTileToNormalState();
        }
    }

    static public void updateGameLoop() {

        if (isCheck(alliedTeam.pieces, enemyTeam.king)) {
            if (isCheckmate(alliedTeam.pieces, enemyTeam.pieces, enemyTeam.king)) {
                checkLabel.setText("CHECKMATE!");
                // do something
                return;
            }
            checkLabel.setText("CHECK");
            validMoves = getValidMoves(alliedTeam.pieces, enemyTeam.pieces, enemyTeam.king);

        }


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

    private static List<int[][]> getValidMoves(List<Piece> allies, List<Piece> enemies, Piece enemyKing) {
        List<int[][]> validMoves = new ArrayList<>();
        for (Piece enemy : enemies) {
            int[] origCoOrds = enemy.coOrds;

            for (int[] move : enemy.moves(boardArray)) {
                superficialMovePiece(getBoardTile(origCoOrds), getBoardTile(move), move);
                if (!isCheck(allies, enemyKing)) {
                    validMoves.add(new int[][]{origCoOrds, move});
                }
                undoSuperficialMove(getBoardTile(origCoOrds), getBoardTile(move));
            }
            getBoardTile(origCoOrds).piece = enemy;
        }

        // shouldn't be here.
        return validMoves;
    }


    static public List<int[]> removeMoveIfNotValid(Tile origTile, List<int[]> moves) {
        if (validMoves == null) return moves;
        int[] origcoOrds = origTile.piece.coOrds;
        List<int[]> ls = new ArrayList<>();
        for (int[][] moveBundle : validMoves) {
            if (Arrays.equals(moveBundle[0], origcoOrds)) {
                ls.add(moveBundle[1]);
            }
        }
        return ls;
    }


    private static boolean isCheck(List<Piece> allies, Piece enemyKing) {

        for (Piece piece : allies) {
            if (piece.isPawn()) { // different because some moves do not capture
                for (int[] move : Pawn.getDiagonalCaptures((Pawn) piece)) {
//                    virtualBoard[move[0]][move[1]].pane.setBackground(Background.fill(Color.RED));

                    if (move[0] == enemyKing.coOrds[0] && move[1] == enemyKing.coOrds[1]) {

                        System.out.println(piece + " is causing check.");
                        getBoardTile(piece).showPromotionHighlight();

                        return true;
                    }
                }
            } else {
                for (int[] move : piece.moves(boardArray)) {
//                    virtualBoard[move[0]][move[1]].pane.setBackground(Background.fill(Color.RED));

                    if (move[0] == enemyKing.coOrds[0] && move[1] == enemyKing.coOrds[1]) {

                        System.out.println(piece + " is causing check.");
                        getBoardTile(piece).showPromotionHighlight();

                        return true;
                    }
                }
            }
        }
        return false;
    }


    private static boolean isCheckmate(List<Piece> allies, List<Piece> enemies, Piece enemyKing) {
        System.out.println("CHECKING FOR MATE");

        for (Piece enemy : enemies) {
            int[] origCoOrds = enemy.coOrds;

            for (int[] move : enemy.moves(boardArray)) {
                superficialMovePiece(getBoardTile(origCoOrds), getBoardTile(move), move);
                if (!isCheck(allies, enemyKing)) {
                    System.out.println("THIS WASN'T MATE");
                    System.out.println(enemy.id + "  " + Arrays.toString(move));
                    return false;
                }
                undoSuperficialMove(getBoardTile(origCoOrds), getBoardTile(move));
            }
            getBoardTile(origCoOrds).piece = enemy;
        }
        System.out.println("THIS IS MATE!!!");
        return true;
    }

    private static List<Piece> getKingCheckers(List<Piece> pieceList, int[] coOrds) {
        List<Piece> threats = new ArrayList<>(8);

        for (Piece piece : pieceList) {
            if (piece.isPawn()) { // different because some moves do not capture
                for (int[] move : Pawn.getDiagonalCaptures((Pawn) piece)) {
                    if (Arrays.equals(move, coOrds)) {
                        threats.add(piece);
                    }
                }
            } else {
                for (int[] move : piece.moves(boardArray)) {
                    if (Arrays.equals(move, coOrds)) {
                        threats.add(piece);
                    }
                }
            }
        }
        return threats;
    }

    private static Tile getBoardTile(Piece piece) {
        return getBoardTile(piece.coOrds);
    }

    private static List<int[]> getAllCaptureMoves(List<Piece> pieceList) {
        List<int[]> ls = new ArrayList<>();
        for (Piece piece : pieceList) {
            if (piece.isPawn()) { // different because some moves do not capture
                for (int[] move : Pawn.getDiagonalCaptures((Pawn) piece)) {
                    ls.add(move);
                }
            } else {
                for (int[] move : piece.moves(boardArray)) {
                    ls.add(move);
                }
            }
        }
        return ls;
    }


    static public TeamAttributes getAlliedTeam() {
        return alliedTeam;
    }

    public static Piece getPrevEnemyPiece() {
        return lastMovedEnemyPiece;
    }

    public static boolean isEnPassantValid(int[] alliedPawnOrigin) {
        if (lastMovedEnemyPiece == null) return false;

        if (lastMovedEnemyPiece.isPawn()
                && getBoardTile(lastMovedEnemyPiece.coOrds).isProperty(SpecialProperties.PAWN_DOUBLE_STEP)
                && alliedPawnOrigin[0] == lastMovedEnemyPiece.coOrds[0]
                && Math.abs(alliedPawnOrigin[1] - lastMovedEnemyPiece.coOrds[1]) == 1
        ) {
            //// EN PASSANT!
            return true; // when this happens, the condition PAWN_DOUBLE_STEP is permanently impossible to get. removed.
        }
        return false;
    }

    public static Tile getBoardTile(int[] coOrds) {
        return boardArray[coOrds[0]][coOrds[1]];
    }
}
