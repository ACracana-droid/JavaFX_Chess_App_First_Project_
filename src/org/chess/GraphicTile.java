package org.chess;

import javafx.css.PseudoClass;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.List;

import static org.chess.ChessLoop.*;

public class GraphicTile extends Tile {


    enum ChessTileColours {
        LIGHT, DARK;

        public String toString() {
            return name().toLowerCase();
        }
    }

    PseudoClass CAPTURE = PseudoClass.getPseudoClass("capture");
    PseudoClass MOVE = PseudoClass.getPseudoClass("move");

    ChessTileColours tileColour;
    Pane pane = new StackPane();
    static protected Tile prevSelected = null;

    protected Circle selectedIndicator = null;

    GraphicTile(ChessTileColours colour) {
        super();
        this.tileColour = colour;
//        pane.setBackground(Background.fill(tileColour.paint)); // TODO: CSS
        pane.getStyleClass().add("chess-tile");
        pane.getStyleClass().add("tile-" + colour);
        pane.setOnMouseClicked(mouseEvent -> selectTile());
    }

    public static Tile getPrevSelected() {
        return prevSelected;
    }

    public void selectTile() {
        if (prevSelected == null) {
            if (this.hasAlliedTeam()) //// initiate move.
                selectFirstPiece();
        } else if (this.equals(prevSelected)) {
            this.deselect();
        } else { //// either move or cancel move
            if (this.hasAlliedTeam()) {
                prevSelected.deselect();
                selectFirstPiece();
            } else {
                Move move = isMove(this);
                if (move == null) {//// it is not a move.
                    prevSelected.deselect(); // if this line is omitted,
                    // then a piece is not deselected if an empty tile is pressed.
                    // This could be valid -> could be set as a preference.
                } else {
                    moveMainPiece(prevSelected, this, move);
                }
            }

        }
    }


    private void deleteSprite() {
        if (piece != null)
            pane.getChildren().remove(this.piece.sprite);// sprite
    }

    @Override
    public void addNewPiece(Piece newPiece, int[] coOrds) {
        this.piece = newPiece;
        this.piece.setCoOrds(coOrds);
        // resizable logic
        piece.sprite.fitWidthProperty().bind(pane.widthProperty().multiply(0.8));
        piece.sprite.fitHeightProperty().bind(pane.heightProperty().multiply(0.8));
        pane.getChildren().add(piece.sprite);
    }

    @Override
    public void deletePiece() {
        getGraphicTile(piece).deleteSprite();
        this.piece = null;
    }

    public void showStandardLocationHighlight() {
        if (this.hasPiece()) {
            pane.pseudoClassStateChanged(MOVE, false);
            pane.pseudoClassStateChanged(CAPTURE, true);
        } else {
            pane.pseudoClassStateChanged(MOVE, true);
            pane.pseudoClassStateChanged(CAPTURE, false);
        }

    }

    public void showCapture() {
        pane.pseudoClassStateChanged(MOVE, false);
        pane.pseudoClassStateChanged(CAPTURE, true);
    }

    private void selectFirstPiece() {
        if (selectedIndicator == null) {
            selectedIndicator = new Circle();
            selectedIndicator.setFill(Color.rgb(0, 0, 20));
            selectedIndicator.setOpacity(0.4);
            selectedIndicator.radiusProperty().bind(pane.widthProperty().multiply(0.4));
            pane.getChildren().addFirst(selectedIndicator);
            prevSelected = this;
        }
//        hasPieceSelected = true;
        List<Move> moves = getValidMoves(this.piece.coOrds);
        showMoves(moves);
    }


    public void clearTileToNormalState() {
        pane.pseudoClassStateChanged(CAPTURE, false);
        pane.pseudoClassStateChanged(MOVE, false);

    }


    @Override
    public void deselect() {
        prevSelected = null;
        hideMoves();
        pane.getChildren().remove(selectedIndicator);
        selectedIndicator = null;
    }
}
