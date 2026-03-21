package org.chess;

public class Move {

    private final int row;
    private final int col;
    SpecialProperties property;


    Move(int row, int col, SpecialProperties property) {
        this.row = row;
        this.col = col;
        this.property = property;
    }

    Move(int row, int col) {
        this.row = row;
        this.col = col;
        this.property = SpecialProperties.DEFAULT;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int[] getCoOrds() {
        return new int[]{row, col};
    }

    public GraphicTile getGraphicTile(GraphicTile[][] board) {
        return board[row][col];
    }


    public String toString() {
        String s = "Row: " + row + ", Col: " + col + (property != null ? " (" + property.name() + ")" : "");
        return s;
    }

}

class MultiMove extends Move {

    int[] auxilliaryOrigin;
    Move auxilliaryMove;

    MultiMove(int row, int col, int[] auxilliaryOrigin, Move auxilliaryMove, SpecialProperties property) {
        super(row, col, property); // standard main move
        this.auxilliaryOrigin = auxilliaryOrigin;
        this.auxilliaryMove = auxilliaryMove;

        if (property == SpecialProperties.CASTLE) {
        }
        if (property == SpecialProperties.EN_PASSANT) { /// could recode en passant like this.

        }
    }


}
