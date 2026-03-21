package org.chess;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.chess.ChessBoard.BOARD_SIZE;
import static org.chess.ChessBoard.graphicBoard;
import static org.chess.ChessLoop.*;
import static org.chess.Team.*;


public abstract class Piece {

    char id;
    int[] coOrds;
    Team team;
    SpecialProperties property = SpecialProperties.DEFAULT;

    ImageView sprite;

    Piece(Team colour) {
        this.team = colour;
        colour.pieces.add(this);
    }

    protected List<Move> infiniteDistancePieceMove(Tile[][] board, int[][] moveVectors) {
        List<Move> ls = new ArrayList<>();
        for (int[] vector : moveVectors) {
            int row = coOrds[0] + vector[0], col = coOrds[1] + vector[1];
            int xLimit = vector[0] == 1 ? BOARD_SIZE : -1;
            int yLimit = vector[1] == 1 ? BOARD_SIZE : -1;
            while (row != xLimit && col != yLimit) {
                if (board[row][col].hasPiece()) break;
                ls.add(new Move(row, col));
                row += vector[0];
                col += vector[1];
            }
            if (row != xLimit && col != yLimit) {
                if (board[row][col].isPossibleMove(team)) {
                    ls.add(new Move(row, col));
                } // otherwise it is an ally and a move is not allowed.
            }
        }
        return ls;
    }


    public boolean matchesProperty(SpecialProperties property) {
        return this.property.equals(property);
    }

    public boolean isPawn() {
        return false;
    }

    public boolean matchesPieceId(char id) {
        return this.id == id;
    }

    abstract public List<Move> moves(Tile[][] virtualBoard);


    public ImageView getImageView(String partialFileName) {
        Image image = new Image(Objects.requireNonNull(getClass().getResource("/images/"
                + (team == WHITE_TEAM ? "w" : "b")
                + partialFileName)).toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        //// to ensure resizing, the sprite is bound to the tile in the Tile class.
        imageView.rotateProperty().bind(rotateProperty.negate());
        imageView.setSmooth(true);
        return imageView;
    }


    protected static boolean isValidCoOrd(int rank, int file, Team colour) {
        return !(rank < 0 || rank >= BOARD_SIZE || file < 0 || file >= BOARD_SIZE)
                && (graphicBoard[rank][file].isPossibleMove(colour));
    }

    public void updateNecessaryFirstMoveInfo() {
    }

    public String toString() {
        return team.name() + " " + id;
    }

    public void setCoOrds(int[] coOrds) {
        this.coOrds = coOrds;
    }


}


class Queen extends Piece {

    Queen(Team colour) {
        super(colour);
        this.id = 'Q';
        this.sprite = getImageView("-queen.png");
    }

    final static int[][] QUEEN_MOVE_VECTORS = {
            // rook
            {1, 0},
            {0, 1},
            {-1, 0},
            {0, -1},
            // bishop
            {1, 1},
            {1, -1},
            {-1, 1},
            {-1, -1}
    };


    @Override
    public List<Move> moves(Tile[][] board) {
        return infiniteDistancePieceMove(board, QUEEN_MOVE_VECTORS);
    }

}

class Rook extends Piece implements SpecialFirstMover {
    boolean firstMove = true;

    @Override
    public void updateNecessaryFirstMoveInfo() {
        firstMove = false;
    }


    public boolean isFirstMove() {
        return firstMove;
    }

    Rook(Team colour) {
        super(colour);
        this.id = 'R';
        this.sprite = getImageView("-rook.png");
//        this.property = SpecialProperties.CASTLE;
    }

    private static final int[][] ROOK_MOVE_VECTORS = {
            {1, 0},
            {0, 1},
            {-1, 0},
            {0, -1}
    };


    @Override
    public List<Move> moves(Tile[][] board) {
        return infiniteDistancePieceMove(board, ROOK_MOVE_VECTORS);
    }


}

class Knight extends Piece {


    Knight(Team colour) {
        super(colour);
        this.id = 'k';
        this.sprite = getImageView("-knight.png");
    }

    private static final int[][] KNIGHT_MOVE_VECTORS = {
            {2, 1},
            {1, 2},
            {-1, 2},
            {2, -1},
            {-2, 1},
            {1, -2},
            {-1, -2},
            {-2, -1}
    };

    /// / for a knight, this is easier. All moves are always legal if within bounds and not hitting an allied piece.
    @Override
    public List<Move> moves(Tile[][] board) {
        int i = coOrds[0], j = coOrds[1];
        List<Move> ls = new ArrayList<>();

        for (int[] move : KNIGHT_MOVE_VECTORS) {
            int rank = i + move[0];
            int file = j + move[1];
            if (isValidCoOrd(rank, file, team))
                ls.add(new Move(rank, file)); // valid location.
        }

        return ls;
    }

}

class Bishop extends Piece {
    private final static int[][] BISHOP_MOVE_VECTORS = {
            {1, 1},
            {1, -1},
            {-1, 1},
            {-1, -1}
    };


    Bishop(Team colour) {
        super(colour);
        this.id = 'B';
        this.sprite = getImageView("-bishop.png");
    }

    @Override
    public List<Move> moves(Tile[][] virtualBoard) {
        return infiniteDistancePieceMove(virtualBoard, BISHOP_MOVE_VECTORS);
    }
}

class Pawn extends Piece implements SpecialFirstMover {
    private boolean firstMove = true;

    Pawn(Team colour) {
        super(colour);
        this.id = 'p';
        this.sprite = getImageView("-pawn.png");
//        this.property = SpecialProperties.PROMOTABLE;
    }

    final int moveVector = (team.equals(WHITE_TEAM) ? 1 : -1);
    final int LIMIT = (team.equals(WHITE_TEAM) ? BOARD_SIZE : -1);

    @Override
    public List<Move> moves(Tile[][] board) {
        List<Move> ls = new ArrayList<>();

        int row = coOrds[0] + moveVector;

        if (row == LIMIT) return ls;
        diagonalCaptures(ls, row, false, board);
        int col;
        if (ChessLoop.isEnPassantValid(coOrds, board))
            ls.add(enPassant());
        col = coOrds[1];
        if (!board[row][col].hasPiece()) {
            ls.add(new Move(row, col));
            if (firstMove) {
                pawnDoubleStepMove(ls, board);
            }
        }

        return ls;
    }

    private void diagonalCaptures(List<Move> ls, int row, boolean ignoreIsMovePossible, Tile[][] board) {
        //// Pawn's two diagonal capture moves:
        int col = coOrds[1] - 1;
        if (col > -1) {
            if (board[row][col].hasEnemyPiece() || ignoreIsMovePossible) {
                ls.add(new Move(row, col));
            }
        }
        col = coOrds[1] + 1;
        if (col < BOARD_SIZE) {
            if (board[row][col].hasEnemyPiece() || ignoreIsMovePossible) {
                ls.add(new Move(row, col));
            }
        }
    }

    public static List<Move> getDiagonalCaptures(Pawn piece, Tile[][] board) {
        List<Move> ls = new ArrayList<>();
        if (piece.coOrds[0] + piece.moveVector == piece.LIMIT) {
            return ls;
        }
        piece.diagonalCaptures(ls, piece.coOrds[0] + piece.moveVector, true, board);
        return ls;

    }

    private Move enPassant() {
        Piece captureInPassing = getPrevEnemyPiece();
        return new Move(captureInPassing.coOrds[0] + this.moveVector, captureInPassing.coOrds[1], SpecialProperties.EN_PASSANT);
    }

    @Override
    public boolean isPawn() {
        return true;
    }


    private void pawnDoubleStepMove(List<Move> ls, Tile[][] board) {
        int row = coOrds[0] + moveVector;
        int col = coOrds[1]; // don't worry about edge cases, it can only do this for one square.
        if (!board[row][col].hasPiece()
                && !board[row += moveVector][col].hasPiece()) {
            ls.add(new Move(row, col, SpecialProperties.PAWN_DOUBLE_STEP));
        }
    }


    @Override
    public void updateNecessaryFirstMoveInfo() {
        firstMove = false;
    }

}