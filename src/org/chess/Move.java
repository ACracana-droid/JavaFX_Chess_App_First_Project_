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
        String s = "Row: " + row + ", Col: " + col + (property != null ? property.name() : "");
        return s;
    }
}
