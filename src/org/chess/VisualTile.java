package org.chess;

import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.chess.ChessBoard.graphicBoard;
import static org.chess.GameLoop.*;

public class VisualTile extends Tile {


    public enum ChessTileColours {
        WHITE_BOARD(Color.ANTIQUEWHITE, Color.ORANGERED, Color.BLUE) {
        },
        BLACK_BOARD(Color.DARKGREEN, Color.ORANGERED, Color.BLUE) {
        };

        public final Color paint;
        public final Color kill;
        public final Color move;

        ChessTileColours(Color colour, Color kill, Color move) {
            this.paint = colour;
            this.kill = kill;
            this.move = move;
        }
    }

    ChessTileColours tileColour;
    Pane pane = new StackPane();
    static protected Tile prevSelected = null;

    protected Circle selectedIndicator = null;

    VisualTile(ChessTileColours colour) {
        super();
        this.tileColour = colour;
        pane.setBackground(Background.fill(tileColour.paint));
        pane.setOnMouseClicked(mouseEvent -> selectTile());
    }

    public void selectTile() {
        if (prevSelected == null) {
            if (this.hasAlliedTeam()) //// initiate move.
                selectFirstPiece();
        } else { //// either move or cancel move
            if (this.hasAlliedTeam()) {
                prevSelected.deselect();
                selectFirstPiece();
            } else {
                int[] move = isMove(this);
                if (move.length == 0) {//// it is not a move.
                    prevSelected.deselect(); // if this line is omitted,
                    // then a piece is not deselected if an empty tile is pressed.
                    // This could be valid -> could be set as a preference.
                } else {
                    movePiece(prevSelected, this, move);
                }
            }

        }
    }


    private void deleteSprite() {
        if (piece != null)
            pane.getChildren().remove(this.piece.sprite);// sprite
    }

    @Override
    public void addNewPiece(GraphicPiece newPiece, int[] coOrds) {
        this.piece = newPiece;
        this.piece.setCoOrds(coOrds);
        // resizable logic
        piece.sprite.fitWidthProperty().bind(pane.widthProperty().multiply(0.8));
        piece.sprite.fitHeightProperty().bind(pane.heightProperty().multiply(0.8));
        pane.getChildren().add(piece.sprite);
    }

    @Override
    public void addPiece(GraphicPiece newPiece, int[] coOrds) {
        super.addPiece(newPiece, coOrds);

        pane.getChildren().add(piece.sprite);
    }

    @Override
    public void deletePiece() {
        getVisualTile(piece).deleteSprite();
        this.piece = null;
    }

    public void overridePiece(GraphicPiece override) {
        deletePiece();
        if (override != null) addPiece(override, override.coOrds);
    }

    public void showStandardLocationHighlight() {
        if (this.hasPiece() || isEnPassant()) {
            pane.setBackground(Background.fill(tileColour.kill));
        } else {
            pane.setBackground(Background.fill(tileColour.move));
        }

    }

    public void showPromotionHighlight() {
        if (this.hasPiece()) {
            pane.setBackground(Background.fill(Promotion.DEFAULT));
        } else {
            pane.setBackground(Background.fill(Promotion.DEFAULT));
        }
    }


    private void selectFirstPiece() {
        if (selectedIndicator == null) {
            selectedIndicator = new Circle();
            selectedIndicator.setFill(Color.GRAY);
            selectedIndicator.setOpacity(0.7);
            selectedIndicator.radiusProperty().bind(pane.widthProperty().multiply(0.4));
            pane.getChildren().addFirst(selectedIndicator);
            prevSelected = this;
        }
//        hasPieceSelected = true;
        showValidMoves();
    }

    public void clearTileToNormalState() {
        pane.setBackground(Background.fill(tileColour.paint));
    }

    private void showValidMoves() {
        List<int[]> moves = this.piece.moves(graphicBoard);
        List<int[]> validMoves = new ArrayList<>();
        if (this.piece.equals(getAlliedTeam().king)) {
            for (int[] move : moves) {
                GameLoop.virtualMovePiece(prevSelected.piece.coOrds, move, virtualBoard);
                if (!isThreatToKing(getEnemyTeam().pieces, move, virtualBoard)) {
                    System.out.println("not a threat");
                    validMoves.add(move);
                }
                makeVirtualBoardCopy();
            }

        } else {
            for (int[] move : moves) {
                GameLoop.virtualMovePiece(prevSelected.piece.coOrds, move, virtualBoard);
                if (!isThreatToKing(getEnemyTeam().pieces, getAlliedTeam().king.coOrds, virtualBoard)) {
                    System.out.println("not a threat");
                    validMoves.add(move);
                }
                makeVirtualBoardCopy();
            }
        }

        showMoves(validMoves);

    }

    @Override
    public void deselect() {
        prevSelected = null;
        hideMoves();
        pane.getChildren().remove(selectedIndicator);
        selectedIndicator = null;
    }
}
