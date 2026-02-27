package org.chess;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.chess.ChessBoard.BOARD_SIZE;
import static org.chess.ChessBoard.boardArray;
import static org.chess.GameLoop.*;
import static org.chess.Queen.QUEEN_MOVE_VECTORS;
import static org.chess.TeamAttributes.*;


public abstract class Piece {
    char id;
    int[] coOrds;
    ImageView sprite;
    TeamAttributes teamColour;

    Piece(TeamAttributes colour) {
        this.teamColour = colour;
        colour.pieces.add(this);

    }

    public boolean isPawn() {
        return false;
    }

    public boolean isKing() {
        return false;
    }


    abstract public List<int[]> moves(Tile[][] virtualBoard);


    protected List<int[]> infiniteDistancePieceMove(Tile[][] virtualBoard, int[][] moveVectors) {
        List<int[]> ls = new ArrayList<>();
        for (int[] vector : moveVectors) {
            int row = coOrds[0] + vector[0], col = coOrds[1] + vector[1];
            int xLimit = vector[0] == 1 ? BOARD_SIZE : -1;
            int yLimit = vector[1] == 1 ? BOARD_SIZE : -1;
            while (row != xLimit && col != yLimit) {
                if (virtualBoard[row][col].hasPiece())
                    break;
                ls.add(new int[]{row, col});
                row += vector[0];
                col += vector[1];
            }
            if (row != xLimit && col != yLimit) {
                if (virtualBoard[row][col].hasEnemyPiece()) {
                    ls.add(new int[]{row, col});
                } // otherwise it is an ally and a move is not allowed.
            }
        }
        return ls;
    }


    public ImageView getImageView(String partialFileName) {
        Image image = new Image(getClass().getResource("/images/"
                + (teamColour == WHITE_TEAM ? "w" : "b")
                + partialFileName).toExternalForm());
        ImageView imageView = new ImageView(image);

        imageView.setPreserveRatio(true);
        //// to ensure resizing, the sprite is bound to the tile in the Tile class.

        imageView.setSmooth(true);
        return imageView;
    }


    public boolean matchesPieceId(char id) {
        return this.id == id;
    }


    private boolean isValidCoOrd(int x, int y) { //eh. Services knight.
        return !(x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE)
                && (!boardArray[x][y].hasAlliedTeam());
    }

    public void updateNecessaryFirstMoveInfo() {
    }

    public boolean isSpecial() {
        return false;
    }

    public String toString() {
        String s = teamColour.name() + " " + id;
        return s;
    }

    public void deleteSprite() {
        getBoardTile(coOrds).deletePiece();
    }

    public void setCoOrds(int[] coOrds) {
        this.coOrds = coOrds;
    }
}


class King extends SpecialMovePiece {

    King(TeamAttributes colour) {
        super(colour);
        this.id = 'K';
        this.sprite = getImageView("-king.png");
        this.property = SpecialProperties.CHECK_AND_MATE;
    }

    @Override
    public boolean isKing() {
        return true;
    }

    @Override
    public List<int[]> moves(Tile[][] virtualBoard) {
        List<int[]> ls = new ArrayList<>();

        for (int[] move : QUEEN_MOVE_VECTORS) {
            int i = coOrds[0], j = coOrds[1];

            int rank = i + move[0];
            int file = j + move[1];
            if (!(rank < 0 || rank >= BOARD_SIZE || file < 0 || file >= BOARD_SIZE)) {
                if (!virtualBoard[rank][file].hasTeamOfThisColour(teamColour)) {

                    ls.add(new int[]{rank, file});
                    System.out.println("king move: " + Arrays.toString(ls.getLast()));
                }

            }
        }

        return ls;
    }

}


class Queen extends Piece {

    Queen(TeamAttributes colour) {
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
    public List<int[]> moves(Tile[][] virtualBoard) {
        return infiniteDistancePieceMove(virtualBoard, QUEEN_MOVE_VECTORS);
    }

}

class Rook extends SpecialMovePiece {


    Rook(TeamAttributes colour) {
        super(colour);
        this.id = 'R';
        this.sprite = getImageView("-rook.png");
        this.property = SpecialProperties.CASTLE;
    }

    private final int[][] moveVectors = {
            {1, 0},
            {0, 1},
            {-1, 0},
            {0, -1}
    };


    @Override
    public List<int[]> moves(Tile[][] virtualBoard) {
        return infiniteDistancePieceMove(virtualBoard, moveVectors);
    }


}

class Knight extends Piece {

    Knight(TeamAttributes colour) {
        super(colour);
        this.id = 'k';
        this.sprite = getImageView("-knight.png");
    }

    static final int[][] moveVectors = {
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
    public List<int[]> moves(Tile[][] virtualBoard) {
        int i = coOrds[0], j = coOrds[1];
        List<int[]> ls = new ArrayList<>();

        for (int[] move : moveVectors) {
            int x = i + move[0];
            int y = j + move[1];
            if (!(x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE)) { /// factor this out.
                if (!virtualBoard[x][y].hasAlliedTeam())
                    ls.add(new int[]{x, y}); // valid location.
            }
        }

        return ls;
    }

}

class Bishop extends Piece {
    private final int[][] moveVectors = {
            {1, 1},
            {1, -1},
            {-1, 1},
            {-1, -1}
    };


    Bishop(TeamAttributes colour) {
        super(colour);
        this.id = 'B';
        this.sprite = getImageView("-bishop.png");
    }

    @Override
    public List<int[]> moves(Tile[][] virtualBoard) {
        return infiniteDistancePieceMove(virtualBoard, moveVectors);
    }
}

class Pawn extends SpecialMovePiece {


    Pawn(TeamAttributes colour) {
        super(colour);
        this.id = 'p';
        this.sprite = getImageView("-pawn.png");
        this.property = SpecialProperties.PROMOTABLE;
    }

    final int moveVector = (teamColour.equals(WHITE_TEAM) ? 1 : -1);
    final int limit = (teamColour.equals(WHITE_TEAM) ? BOARD_SIZE : -1);

    @Override
    public List<int[]> moves(Tile[][] virtualBoard) {
        List<int[]> ls = new ArrayList<>();

        int row = coOrds[0] + moveVector;

        if (row == limit) return ls;
        diagonalCaptures(ls, row, false);
        int col;
        if (GameLoop.isEnPassantValid(coOrds))
            ls.add(enPassant());

        col = coOrds[1];
        if (!virtualBoard[row][col].hasPiece()) {
            ls.add(new int[]{row, col});
            if (firstMove) {
                pawnDoubleStepMove(ls);
            }
        }

        //// the last move in list is always the enabler of en passant!

        return ls;
    }

    private void diagonalCaptures(List<int[]> ls, int row, boolean ignoreIsMovePossible) {
        //// Pawn's two diagonal capture moves:
        int col = coOrds[1] - 1;
        if (col > -1) {
            if (boardArray[row][col].hasEnemyPiece() || ignoreIsMovePossible) {
                ls.add(new int[]{row, col});
            }
        }
        col = coOrds[1] + 1;
        if (col < BOARD_SIZE) {
            if (boardArray[row][col].hasEnemyPiece() || ignoreIsMovePossible) {
                ls.add(new int[]{row, col});
            }
        }
    }

    public static List<int[]> getDiagonalCaptures(Pawn piece) {
        List<int[]> ls = new ArrayList<>();
        if (piece.coOrds[0] + piece.moveVector == piece.limit) {
            System.out.println("There can't be a pawn here!!");
            return ls;
        }
        piece.diagonalCaptures(ls, piece.coOrds[0] + piece.moveVector, true);
        return ls;

    }

    private int[] enPassant() {
        Piece captureInPassing = getPrevEnemyPiece();
        boardArray[captureInPassing.coOrds[0] + this.moveVector][captureInPassing.coOrds[1]].setIsEnPassant();
        return new int[]{captureInPassing.coOrds[0] + moveVector, captureInPassing.coOrds[1]};
    }

    @Override
    public boolean isPawn() {
        return true;
    }


    private void pawnDoubleStepMove(List<int[]> ls) {
        int row = coOrds[0] + moveVector;
        int col = coOrds[1]; // don't worry about edge cases, it can only do this for one square.
        if (!boardArray[row][col].hasPiece()
                && !boardArray[row += moveVector][col].hasPiece()) {
            boardArray[row][col].setProperty(SpecialProperties.PAWN_DOUBLE_STEP);
            ls.add(new int[]{row, col});
        }
    }


}