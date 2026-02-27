package org.chess;

import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.chess.SpecialMovePiece.SpecialProperties;

import java.util.List;

import static org.chess.ChessBoard.boardArray;
import static org.chess.GameLoop.*;

public class Tile {


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

    static boolean hasPieceSelected = false;


    Piece piece;
    ChessTileColours tileColour;
    Pane pane = new StackPane();
    static private Tile prevSelected = null;

    private Circle selectedIndicator = null;
    SpecialProperties property;


    Tile(ChessTileColours colour) {
        this.tileColour = colour;
        property = SpecialProperties.DEFAULT;
        initialiseTile();
    }


    private void initialiseTile() {
        pane.setBackground(Background.fill(tileColour.paint));
        pane.setOnMouseClicked(mouseEvent -> selectTile());
    }

//    public void overridePiece(Piece override) {
//        deleteSprite();
//        deletePiece();
//        if (override != null) addPiece(override, override.coOrds);
//    }

    public void addPiece(Piece newPiece, int[] coOrds) {
        this.piece = newPiece;
        this.piece.setCoOrds(coOrds);

        pane.getChildren().add(piece.sprite);
    }

    public void superficialAddPiece(Piece newPiece, int[] coOrds) {
        this.piece = newPiece;
//        this.piece.setCoOrds(coOrds);
    }

    private void deleteSprite() {
        if (piece != null)
            pane.getChildren().remove(this.piece.sprite);// sprite
    }

    public void addNewPiece(Piece newPiece, int[] coOrds) {
        this.piece = newPiece;
        this.piece.setCoOrds(coOrds);
        // resizable logic
        piece.sprite.fitWidthProperty().bind(pane.widthProperty().multiply(0.8));
        piece.sprite.fitHeightProperty().bind(pane.heightProperty().multiply(0.8));
        pane.getChildren().add(piece.sprite);
    }

    public void deletePiece() {
        getBoardTile(piece.coOrds).deleteSprite();
        this.piece = null;
    }

    public void superficialDeletePiece() {
        this.piece = null;
    }


    public boolean hasPiece() {
        return piece != null;
    }

    public boolean hasAlliedTeam() {
        return hasPiece() && piece.teamColour.equals(GameLoop.getAlliedTeam());
    }

    public boolean hasTeamOfThisColour(TeamAttributes colour) {
        return hasPiece() && this.piece.teamColour.equals(colour);
    }

    public boolean hasEnemyPiece() {
        return hasPiece() && !piece.teamColour.equals(GameLoop.getAlliedTeam());
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

        //// Glow effect on piece
//        DropShadow glow = new DropShadow();
//        glow.setColor(Color.GOLD);
//        glow.radiusProperty().bind(pane.widthProperty().multiply(0.6));
//        pane.getChildren().getFirst().setEffect(glow);

    }

    public boolean isEnPassant() {
        return property.equals(SpecialProperties.EN_PASSANT);
    }

    public void setIsEnPassant() {
        property = SpecialProperties.EN_PASSANT;
    }

    public boolean isProperty(SpecialProperties property) {
        return this.property.equals(property);
    }

    public void setProperty(SpecialProperties property) {
        this.property = property;
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

    public void clearTileToNormalState() {
        pane.setBackground(Background.fill(tileColour.paint));
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
        hasPieceSelected = true;
        showValidMoves();
    }

    private void showValidMoves() {
        List<int[]> moves = this.piece.moves(boardArray);
        moves = removeMoveIfResultsInCheck(this, moves);
        moves = removeMoveIfNotValid(this, moves);
        showMoves(moves);

    }


    public void deselect() {
        prevSelected = null;
        hideMoves();
        pane.getChildren().remove(selectedIndicator);
        selectedIndicator = null;
    }


}
