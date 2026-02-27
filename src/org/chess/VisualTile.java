package org.chess;

import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.List;

import static org.chess.ChessBoard.visualBoard;
import static org.chess.GameLoop.*;

public class VisualTile extends Tile {


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
    public void addNewPiece(Piece newPiece, int[] coOrds) {
        this.piece = newPiece;
        this.piece.setCoOrds(coOrds);
        // resizable logic
        piece.sprite.fitWidthProperty().bind(pane.widthProperty().multiply(0.8));
        piece.sprite.fitHeightProperty().bind(pane.heightProperty().multiply(0.8));
        pane.getChildren().add(piece.sprite);
    }

    @Override
    public void addPiece(Piece newPiece, int[] coOrds) {
        super.addPiece(newPiece, coOrds);

        pane.getChildren().add(piece.sprite);
    }

    public void deletePiece() {
        getVisualTile(piece.coOrds).deleteSprite();
        this.piece = null;
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
        List<int[]> moves = this.piece.moves(visualBoard);
//        moves = removeMoveIfResultsInCheck(this, moves);
//        moves = removeMoveIfNotValid(this, moves);
        showMoves(moves);

    }

    @Override
    public void deselect() {
        prevSelected = null;
        hideMoves();
        pane.getChildren().remove(selectedIndicator);
        selectedIndicator = null;
    }
}
