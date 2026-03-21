package org.chess;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;

import javafx.beans.binding.NumberBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;


import static org.chess.Main.settings;
import static org.chess.Tile.getTileCopy;
import static org.chess.GraphicTile.ChessTileColours.*;

public class ChessBoard {
    static public GraphicTile[][] graphicBoard;
    static public Tile[][] virtualBoard;
    static final int BOARD_SIZE = 8;
    final double GUI_SIZE = BOARD_SIZE + 2.5;
    private GridPane gridPane;

    protected static BooleanProperty turnProperty;
    protected static DoubleProperty rotateProperty;

    /// / part of game loop.
    static BorderPane boardView;


    protected static StackPane boardRoot;

    ChessBoard() {
        turnProperty = new SimpleBooleanProperty(true);
        rotateProperty = new SimpleDoubleProperty(0.0);
        boardRoot = new StackPane();
        makeBoardFromVisualTile2DArray();
        boardRoot.getChildren().addAll(makeBoardView());
    }


    private Node makeBoardView() {
        gridPane = new GridPane();
        gridPane.getStyleClass().add("board-grid");
        gridPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        addingBoardLabels();

        //// putting tiles onto the graphic board
        makeGraphicBoard();

        //// IMPORTANT: Sizes the square which in turn sizes the whole board.
        StackPane initialWrapper = new StackPane(makeBoardBackground("chessboard-edging", 0.96),
                makeBoardBackground("chessboard-background", 0.9),
                gridPane);

        PseudoClass lightBackground = PseudoClass.getPseudoClass("light");
        PseudoClass darkBackground = PseudoClass.getPseudoClass("dark");
        boardView = new BorderPane();
        boardView.setCenter(initialWrapper);
        boardView.getStyleClass().add("board-view");

        boardView.pseudoClassStateChanged(lightBackground, true);
        boardView.pseudoClassStateChanged(darkBackground, false);

        gridPane.rotateProperty().bind(rotateProperty);

        if (settings.rotateBoard) {
            turnProperty.addListener((obs, oldVal, newVal) -> {
                Timeline flip = new Timeline(
                        new KeyFrame(Duration.millis(500),
                                new KeyValue(rotateProperty, rotateProperty.get() + 180))
                );

                flip.play();
                boardView.pseudoClassStateChanged(lightBackground, newVal);
                boardView.pseudoClassStateChanged(darkBackground, oldVal);
            });
        } else {
            turnProperty.addListener((obs, oldVal, newVal) -> {
                rotateProperty.set(rotateProperty.get() + 180);
            });
        }
        return boardView;
    }


    private Node makeBoardBackground(String styleClass, double size) {
        Rectangle rectangle = new Rectangle();
        rectangle.getStyleClass().add(styleClass);
        rectangle.widthProperty().bind(
                Bindings.min(gridPane.heightProperty().multiply(size), gridPane.widthProperty().multiply(size))
        );
        rectangle.heightProperty().bind(
                Bindings.min(gridPane.heightProperty().multiply(size), gridPane.widthProperty().multiply(size))
        );
        rectangle.rotateProperty().bind(rotateProperty);
        return rectangle;
    }

    public static void resetVirtualBoardToGraphicState() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                virtualBoard[i][j] = getTileCopy(graphicBoard[i][j], i, j);
            }
        }
    }


    private void addingBoardLabels() {
        addHalfOfLabels(9, 0);

        DoubleProperty A = addHalfOfLabels(0, 9);
        gridPane.add(getSmallAccentOnCorner(A), 0, 0);
        gridPane.add(getSmallAccentOnCorner(A), 0, 9);
        gridPane.add(getSmallAccentOnCorner(A), 9, 0);
        gridPane.add(getSmallAccentOnCorner(A), 9, 9);

    }

    private DoubleProperty addHalfOfLabels(int n1, int n2) {
        Label axisLabelX;
        Label axisLabelY = null;
        for (int i = 0; i < BOARD_SIZE; i++) {
            axisLabelX = styleAxisLabel(new Label((char) ('A' + i) + ""));
            Pane pane1 = new StackPane();
            pane1.setBackground(Background.fill(Color.DARKGRAY));
            pane1.getChildren().add(axisLabelX);
            gridPane.add(pane1, i + 1, n1);

            Pane pane2 = new StackPane();
            axisLabelY = styleAxisLabel(new Label((i + 1) + ""));
            pane2.setBackground(Background.fill(Color.DARKGRAY));
            pane2.getChildren().add(axisLabelY);

            // because of the nature of text, we can bind the height to the other's width
            // and the width to the other's height
            axisLabelY.prefHeightProperty().bind(
                    axisLabelX.widthProperty());
            axisLabelY.prefWidthProperty().bind(
                    axisLabelX.heightProperty());


            axisLabelY.setAlignment(Pos.CENTER);
            gridPane.add(pane2, n2, 8 - i);
        }

        return axisLabelY.prefWidthProperty();
    }

    private Pane getSmallAccentOnCorner(DoubleProperty A) {
        Rectangle smallAccent = new Rectangle();
        smallAccent.setFill(Color.GRAY);

        smallAccent.heightProperty().bind(A);
        smallAccent.widthProperty().bind(A);

        StackPane pane = new StackPane(smallAccent);
        pane.setAlignment(Pos.CENTER);
        pane.setOnMouseClicked(mouseEvent -> resetBoardHighlights());
        return pane;
    }

    private void resetBoardHighlights() { // TODO: REMEMBER THIS LITTLE BUTTON!
        for (GraphicTile[] rank : graphicBoard) {
            for (GraphicTile tile : rank) {
                tile.clearTileToNormalState();
            }
        }
    }

    private void makeGraphicBoard() {
        NumberBinding tileSize = Bindings.min(gridPane.widthProperty().divide(GUI_SIZE),
                gridPane.heightProperty().divide(GUI_SIZE));
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                GraphicTile tile = graphicBoard[i][j];
                gridPane.add(tile.pane, j + 1, 7 - i + 1);

                tile.pane.setMaxSize(150, 150);
                tile.pane.setMinSize(5, 5);


                tile.pane.prefWidthProperty().bind(tileSize);
                tile.pane.prefHeightProperty().bind(tileSize);
            }
        }
    }


    private Label styleAxisLabel(Label axisLabel) {
        axisLabel.getStyleClass().add("chess-axis");
        GraphicTile tile = graphicBoard[0][0];
        //

        axisLabel.rotateProperty().bind(rotateProperty.negate());
        axisLabel.prefHeightProperty().bind(
                Bindings.min(
                        tile.pane.widthProperty().divide(GUI_SIZE + 10),
                        tile.pane.heightProperty().divide(GUI_SIZE + 10)));
        axisLabel.prefWidthProperty().bind(
                Bindings.min(
                        tile.pane.widthProperty().divide(GUI_SIZE + 10),
                        tile.pane.heightProperty().divide(GUI_SIZE + 10)));
        return axisLabel;
    }


    private void makeBoardFromVisualTile2DArray() {
        ////                     row: 1-8     col a-h
        graphicBoard = new GraphicTile[BOARD_SIZE][BOARD_SIZE];

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                graphicBoard[i][j] = new GraphicTile(DARK);
                j++;
                graphicBoard[i][j] = new GraphicTile(LIGHT);
            }
            i++;             //alternating colours.
            for (int j = 0; j < BOARD_SIZE; j++) {
                graphicBoard[i][j] = new GraphicTile(LIGHT);
                j++;
                graphicBoard[i][j] = new GraphicTile(DARK);
            }
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(); //

        sb.append("_".repeat((3 * BOARD_SIZE) + 4 + 2));
        sb.append("\n");
        // must traverse board from 8, downwards.

        //// using i to make rank index
        for (int i = BOARD_SIZE; i > 0; ) {
            sb.append(i).append(" | "); // length 4
            i--;
            for (GraphicTile tile : graphicBoard[i]) {
                sb.append(" ");
                if (tile.hasPiece()) {
                    sb.append(tile.piece.id);
                } else {
                    sb.append(tile.tileColour == LIGHT ? '#' : '-'); //// white symbol, black symbol

                }
                sb.append(" ");

            }
            sb.append(" |\n"); //length 2*
        }
        sb.append("_".repeat((3 * BOARD_SIZE) + 4 + 2));
        sb.append("\n").
                append(" ".repeat(4 + 1));
        //// making file index
        for (char i = 'a'; i <= 'h'; i++) {
            sb.append(i)
                    .append(" ".repeat(2));
        }
        return sb.toString();
    }

}

