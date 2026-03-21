package org.chess;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.chess.Pawn.getDiagonalCaptures;
import static org.chess.Team.*;

public class ChessLoop extends ChessBoard {

    private static final int STALE_MOVE_COUNT_RULE = 50;

    static private Team alliedTeam;
    static private Team enemyTeam;
    static private Piece lastMovedEnemyPiece;
    static private int turnCount;
    static Label turnLabel;
    static Label turnCountLabel;
    static Label checkLabel;

    static private int halfMoveClock;
    private static boolean stalemateFlag;


    static private List<Move> shownMoves = null;

    enum BoardState {
        DEFAULT, CHECK, CHECKMATE, STALEMATE
    }

    ChessLoop() {
        super();
        lastMovedEnemyPiece = null;
        BLACK_TEAM.pieces.clear();
        WHITE_TEAM.pieces.clear();
        boardView.setTop(makeInfoWrapper());
        turnCount = 0;

        halfMoveClock = 0;
        alliedTeam = WHITE_TEAM;
        enemyTeam = BLACK_TEAM;
        addStartingPieces(); // could animate
        /*
        for developing or experimenting purposes, you can add new pieces you want the game
        to start with here.
        use the addNewPiece function and specify coOrds.
        */

        virtualBoard = new Tile[BOARD_SIZE][BOARD_SIZE];
        resetVirtualBoardToGraphicState();

    }

    private Node makeInfoWrapper() {
        HBox hBox = new HBox();
        hBox.getStyleClass().add("info-wrapper");

        turnLabel = new Label();
        turnLabel.getStyleClass().add("turn-counter-label");
        turnLabel.setText("White's\nTurn");
        turnCountLabel = new Label();
        turnCountLabel.getStyleClass().add("turn-counter-label");
        turnCountLabel.setText("Turn: 0");
        checkLabel = new Label();

        hBox.getChildren().addAll(turnLabel, turnCountLabel, checkLabel);
        hBox.setAlignment(Pos.CENTER);
        boardView.setTop(hBox);
        return hBox;
    }

    private void addStartingPieces() {
        //Pawns:
        for (int i = 0; i < BOARD_SIZE; i++) {
            graphicBoard[1][i].addNewPiece(new Pawn(WHITE_TEAM), new int[]{1, i});
            graphicBoard[6][i].addNewPiece(new Pawn(BLACK_TEAM), new int[]{6, i});
        }
        //Pieces:
        int row = 0;
        Team colour = WHITE_TEAM;
        for (int i = 0; i < 2; i++) {
            graphicBoard[row][0].addNewPiece(new Rook(colour), new int[]{row, 0});
            graphicBoard[row][7].addNewPiece(new Rook(colour), new int[]{row, 7});
            graphicBoard[row][1].addNewPiece(new Knight(colour), new int[]{row, 1});
            graphicBoard[row][6].addNewPiece(new Knight(colour), new int[]{row, 6});
            graphicBoard[row][2].addNewPiece(new Bishop(colour), new int[]{row, 2});
            graphicBoard[row][5].addNewPiece(new Bishop(colour), new int[]{row, 5});
            graphicBoard[row][3].addNewPiece(new Queen(colour), new int[]{row, 3});
//            virtualBoard[row][4].addNewPiece(new King(colour), new int[]{row, 4});
            colour.setKing(new King(colour));
            graphicBoard[row][4].addNewPiece(colour.getKing(), new int[]{row, 4});

            colour = BLACK_TEAM;
            row = 7;
        }

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


    public static List<Piece> virtualMovePiece(int[] origin, Move move, Tile[][] board) {

        List<Piece> virtualEnemyPieceList = new ArrayList<>(enemyTeam.pieces);

        Tile originTile = getTile(origin, board);
        Tile destTile = getTile(move, board);
        Piece orig_piece = originTile.piece;
        Piece dest_piece = destTile.piece;

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

    private static void moveAuxilliaryPiece(MultiMove move, Tile[][] board) {
        System.out.println("MOVE!");
        Piece piece = getTile(move.auxilliaryOrigin, board).piece;
        getTile(move.auxilliaryOrigin, board).deletePiece();
        getTile(move.auxilliaryOrigin, board).deselect();

        /// capture is unimplemented. Can't happen.

        getTile(move.auxilliaryMove, board).addNewPiece(piece, move.getCoOrds());
        piece.updateNecessaryFirstMoveInfo();
    }

    static public void moveMainPiece(Tile orig, Tile dest, Move move) {
        stalemateFlag = false;
        System.out.println("MOVE!");
        Piece piece = orig.piece;
        orig.deletePiece();
        orig.deselect();


        if (dest.hasPiece()) { //// must be a capture
            System.out.println("KILL!");
            getAlliedTeam().capturedPieces.add(dest.piece); // can add to display.
            enemyTeam.pieces.remove(dest.piece);
            dest.deletePiece();

            stalemateFlag = true;
        }

        dest.addNewPiece(piece, move.getCoOrds());

        /// castle
        if (move.property.equals(SpecialProperties.CASTLE)) {
            moveAuxilliaryPiece(((MultiMove) move), graphicBoard);
        }

        if (piece.isPawn()) {
            stalemateFlag = true;
            if (move.getRow() == alliedTeam.PROMOTION_RANK) {
                System.out.println("PROMOTE!!");
                new Promotion(piece, boardRoot);
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
                        Piece passedPiece = getGraphicTile(lastMovedEnemyPiece.coOrds).piece;
                        getAlliedTeam().capturedPieces.add(passedPiece);
                        enemyTeam.pieces.remove(passedPiece);

                        System.out.println(Arrays.toString(lastMovedEnemyPiece.coOrds));
                        getGraphicTile(lastMovedEnemyPiece.coOrds).deletePiece();
                        piece.property = SpecialProperties.DEFAULT;
                    }
                }
            }


        }

        piece.updateNecessaryFirstMoveInfo();
        lastMovedEnemyPiece = piece;
        updateGameLoop();
    }

    public static List<int[]> getThreats(List<Piece> enemyTeam, Tile[][] board) {
        List<int[]> ls = new ArrayList<>();
        for (Piece piece : enemyTeam) {
            if (piece.isPawn()) {
                for (Move move : getDiagonalCaptures((Pawn) piece, board)) {
                    ls.add(move.getCoOrds());
                }
            } else if (piece.matchesPieceId('K')) {
                /// Necessary. Causes stack overflow otherwise.
                for (Move move : ((King) piece).threatsIgnoreLegal()) {
                    ls.add(move.getCoOrds());
                }
            } else {
                for (Move move : piece.moves(board)) {
                    ls.add(move.getCoOrds());
                }
            }
        }
        return ls;
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
        System.out.println("showing moves: "); //TODO: REMOVE SOUT

        for (Move move : moves) {
            if (move.property == SpecialProperties.EN_PASSANT) {  // splitting the decisions...
                move.getGraphicTile(graphicBoard).showCapture();
            } else {
                move.getGraphicTile(graphicBoard).showStandardLocationHighlight();
            }
            System.out.println(move);
        }
        shownMoves = moves;
        //// List must be tracked so moves can be hidden.
    }


    static public void hideMoves() {
        if (shownMoves == null) return; //// no visible moves to hide
        for (Move move : shownMoves) {
            move.getGraphicTile(graphicBoard).clearTileToNormalState();
        }
    }


    static public void updateGameLoop() {

        // TODO: TWO BUGS:
        // fix the death of an attacking king. Able to attack when attack brings king into check.
        // Take king without ending game.

        turnCount++;

        if (stalemateFlag) halfMoveClock = 0;
        else halfMoveClock++;

        if (halfMoveClock >= STALE_MOVE_COUNT_RULE) {
            endChessGame(BoardState.STALEMATE);
            return;
        }

        turnProperty.set(!turnProperty.getValue());
        Team swap = alliedTeam;
        alliedTeam = enemyTeam;
        enemyTeam = swap;


        switch (getBoardState(alliedTeam.pieces)) {
            case CHECKMATE -> {
                checkLabel.setText("CHECKMATE!");
                checkLabel.getStyleClass().add("turn-counter-label");
                endChessGame(BoardState.CHECKMATE);
                return;
            }
            case CHECK -> {
                checkLabel.setText("CHECK!");
                checkLabel.getStyleClass().add("turn-counter-label");

            }
            case DEFAULT -> {
                checkLabel.setText("");
                checkLabel.getStyleClass().remove("turn-counter-label");

            }
        }
        turnLabel.setText(alliedTeam.turnLabelText);
        turnCountLabel.setText("Turn: " + turnCount);
    }

    private static void endChessGame(BoardState state) {
        System.out.println("END GAME!");
        System.out.println(state.name().toLowerCase());

    }

    private static BoardState getBoardState(List<Piece> pieceList) {
        List<Move> validMoves = new ArrayList<>();
        resetVirtualBoardToGraphicState();
        for (Piece piece : pieceList) {
            validMoves.addAll(getTile(piece.coOrds, virtualBoard).getValidMoves(piece.coOrds));
        }
        if (validMoves.isEmpty()) {
            if (isNotThreatToKing(enemyTeam.pieces, getAlliedTeam().getKing().coOrds, virtualBoard))
                return BoardState.STALEMATE;
            else return BoardState.CHECKMATE;
        }
        if (!isNotThreatToKing(enemyTeam.pieces, getAlliedTeam().getKing().coOrds, virtualBoard)) {
            return BoardState.CHECK;
        }

        return BoardState.DEFAULT;
    }

    static public Team getAlliedTeam() {
        return alliedTeam;
    }

    static public Team getEnemyTeam() {
        return enemyTeam;
    }

    public static Piece getPrevEnemyPiece() {
        return lastMovedEnemyPiece;
    }

    public static boolean isEnPassantValid(int[] coOrds, Tile[][] board) {
        if (lastMovedEnemyPiece == null) return false;
        Tile current = getTile(coOrds, board);
        if (current == null || !current.hasPiece() || !current.piece.isPawn()) return false;

        //// EN PASSANT!
        return current.piece.coOrds[0] == lastMovedEnemyPiece.coOrds[0]
                && lastMovedEnemyPiece.matchesProperty(SpecialProperties.EN_PASSANT)
                && Math.abs(current.piece.coOrds[1] - lastMovedEnemyPiece.coOrds[1]) == 1;
    }

    public static GraphicTile getGraphicTile(int[] coOrds) {
        return graphicBoard[coOrds[0]][coOrds[1]];
    }

    public static GraphicTile getGraphicTile(Piece piece) {
        return graphicBoard[piece.coOrds[0]][piece.coOrds[1]];
    }

    public static Tile getTile(int[] coOrds, Tile[][] board) {
        return board[coOrds[0]][coOrds[1]];
    }


    public static Tile getTile(Move move, Tile[][] board) {
        return board[move.getRow()][move.getCol()];
    }

}
