package org.chess;

import javafx.beans.binding.Bindings;

import javafx.beans.binding.NumberBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;


import static org.chess.Tile.ChessTileColours.*;
import static org.chess.TeamAttributes.*;

public class ChessBoard {
    static public VisualTile[][] visualBoard;
    static final int BOARD_SIZE = 8;
    final double GUI_SIZE = BOARD_SIZE + 1.5;
    GridPane chessBoard;
    Rectangle boardDecor;

    /// / part of game loop.
    static BorderPane boardView;
    static Label turnLabel;
    static Label turnCountLabel;
    static Label checkLabel;


    static protected StackPane root;
    Stage stage;


    ChessBoard() {
        makeVisualBoard();
        root = new StackPane();
        chessBoard = new GridPane();

        chessBoard.setHgap(5);
        chessBoard.setVgap(5);
        chessBoard.setGridLinesVisible(false);
//        board.setPadding(new Insets(5));

        chessBoard.setAlignment(Pos.CENTER);
        chessBoard.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        addStartingPieces();

        boardDecor = new Rectangle();

        //// IMPORTANT. Sizes the square which in turn sizes the whole board.
        final double sizeToWindow = 0.97;
        boardDecor.widthProperty().bind(
                Bindings.min(chessBoard.heightProperty().multiply(sizeToWindow), chessBoard.widthProperty().multiply(sizeToWindow))
        );
        boardDecor.heightProperty().bind(
                Bindings.min(chessBoard.heightProperty().multiply(sizeToWindow), chessBoard.widthProperty().multiply(sizeToWindow))
        );

        addingBoardLabels();

        //// putting tiles onto the board
        makeGraphicBoard();

        HBox infoWrapper = new HBox();
        infoWrapper.setStyle("""
                -fx-padding: 10;
                """);

        turnLabel = new Label();
        turnLabel.setText("White's\nTurn");
        turnCountLabel = new Label();
        checkLabel = new Label();

        infoWrapper.getChildren().addAll(turnLabel, turnCountLabel, checkLabel);

        StackPane initialWrapper = new StackPane(boardDecor, chessBoard);
        boardView = new BorderPane();
        boardView.setCenter(initialWrapper);
        boardView.setStyle("""
                 -fx-border-radius: 8;
                 -fx-border-width: 12;
                 -fx-border-color: black;
                """);


        boardView.setBackground(new Background(
                new BackgroundFill(WHITE_TEAM.paint, new CornerRadii(20), Insets.EMPTY)
        ));
        infoWrapper.setAlignment(Pos.CENTER);
        boardView.setTop(infoWrapper);
        root.getChildren().add(boardView);


        decorateBoardAndCSS();

        // TODO: TAKE OUT WHEN NOT NEEDED.
//        virtualBoard[5][5].addNewPiece(new Pawn(WHITE_TEAM), new int[]{5, 5});
//        virtualBoard[3][2].addNewPiece(new Pawn(BLACK_TEAM), new int[]{3, 2});
//        virtualBoard[5][6].addNewPiece(new Queen(WHITE_TEAM), new int[]{5, 6});


        Scene chessBoardScene = new Scene(root, 1000, 700);

        // TODO: TAKE OUT WHEN NEEDED
//        chessBoardScene.setOnKeyPressed(keyEvent -> visuallyCheckDanger());


        stage = new Stage();
        stage.getIcons().add(new Image(getClass().getResource("/images/w-king.png").toExternalForm()));
        stage.setTitle("Chess App");
        stage.setScene(chessBoardScene);
        stage.setMinWidth(300);
        stage.setMinHeight(300);
        stage.show();

//        boardView.setBackground(new Background(new BackgroundFill(Color.PALEGREEN, CornerRadii.EMPTY, Insets.EMPTY)));

    }

    private void addingBoardLabels() {
        Label axisLabelX = null;
        Label axisLabelY = null;
        for (int i = 0; i < BOARD_SIZE; i++) {
            axisLabelX = styleAxisLabel(new Label((char) ('A' + i) + ""));
            Pane pane1 = new StackPane();
            pane1.setBackground(Background.fill(Color.DARKGRAY));
            pane1.getChildren().add(axisLabelX);
            chessBoard.add(pane1, i + 1, 9);

            Pane pane2 = new StackPane();
            axisLabelY = new Label((i + 1) + "");
            axisLabelY.setStyle("""
                    -fx-font-family: "Times New Roman", Times, serif;
                    -fx-font-weight: bold
                    """);

            // because of the nature of text, we can bind the height to the other's width
            // and the width to the other's height
            axisLabelY.prefHeightProperty().bind(
                    axisLabelX.widthProperty());
            axisLabelY.prefWidthProperty().bind(
                    axisLabelX.heightProperty());

            pane2.setBackground(Background.fill(Color.DARKGRAY));
            pane2.getChildren().add(axisLabelY);
            axisLabelY.setAlignment(Pos.CENTER);
            chessBoard.add(pane2, 0, 8 - i);
        }


        //        Circle smallAccent = new Circle();
//        smallAccent.setFill(WHITE_BOARD.paint);
//        smallAccent.radiusProperty().bind(Bindings.min(
//                axisLabelX.widthProperty().divide(2),
//                axisLabelX.heightProperty().divide(2)));
        Rectangle smallAccent = new Rectangle();
        smallAccent.setFill(Color.GRAY);
        smallAccent.heightProperty().bind(
                axisLabelX.heightProperty());
        smallAccent.widthProperty().bind(
                axisLabelY.widthProperty());
        StackPane pane = new StackPane(smallAccent);
        pane.setAlignment(Pos.CENTER);
        chessBoard.add(pane, 0, 9);
        pane.setOnMouseClicked(mouseEvent -> resetBoardHighlights());

    }

    private void resetBoardHighlights() { // TODO: REMEMBER THIS LITTLE BUTTON!
        for (VisualTile[] rank : visualBoard) {
            for (VisualTile tile : rank) {
                tile.clearTileToNormalState();
            }
        }
    }

    private void makeGraphicBoard() {
        NumberBinding tileSize = Bindings.min(boardDecor.widthProperty().divide(GUI_SIZE),
                boardDecor.heightProperty().divide(GUI_SIZE));
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                VisualTile tile = visualBoard[i][j];
                chessBoard.add(tile.pane, j + 1, 7 - i + 1);

                tile.pane.setMaxSize(150, 150);
                tile.pane.setMinSize(5, 5);


                tile.pane.prefWidthProperty().bind(tileSize);
                tile.pane.prefHeightProperty().bind(tileSize);
            }
        }
    }

    private void decorateBoardAndCSS() {


        /// outer part
        root.setStyle("-fx-background-color: beige;");
        root.setPadding(new Insets(10));
        /// board
        boardDecor.setStyle("""
                -fx-background-color: grey;
                """);
        /// turnLabel
        turnLabel.setStyle("""
                -fx-font-family: "Times New Roman";
                -fx-font-size: 25;
                -fx-background-color: gray;
                """);
    }

    private Label styleAxisLabel(Label axisLabel) {
        axisLabel.setStyle("""
                -fx-font-family: "Times New Roman", Times, serif;
                -fx-font-weight: bold
                """);
        VisualTile tile = visualBoard[0][0];
        //
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

    private void addStartingPieces() {
        //Pawns:
        for (int i = 0; i < BOARD_SIZE; i++) {
            visualBoard[1][i].addNewPiece(new Pawn(WHITE_TEAM), new int[]{1, i});
            visualBoard[6][i].addNewPiece(new Pawn(BLACK_TEAM), new int[]{6, i});
        }
        //Pieces:
        int row = 0;
        TeamAttributes colour = TeamAttributes.WHITE_TEAM;
        for (int i = 0; i < 2; i++) {
            visualBoard[row][0].addNewPiece(new Rook(colour), new int[]{row, 0});
            visualBoard[row][7].addNewPiece(new Rook(colour), new int[]{row, 7});
            visualBoard[row][1].addNewPiece(new Knight(colour), new int[]{row, 1});
            visualBoard[row][6].addNewPiece(new Knight(colour), new int[]{row, 6});
            visualBoard[row][2].addNewPiece(new Bishop(colour), new int[]{row, 2});
            visualBoard[row][5].addNewPiece(new Bishop(colour), new int[]{row, 5});
            visualBoard[row][3].addNewPiece(new Queen(colour), new int[]{row, 3});
//            virtualBoard[row][4].addNewPiece(new King(colour), new int[]{row, 4});
            colour.king = new King(colour);
            visualBoard[row][4].addNewPiece(colour.king, new int[]{row, 4});

            colour = BLACK_TEAM;
            row = 7;
        }

    }

    private void makeVisualBoard() {
        ////                     row: 1-8     col a-h
        visualBoard = new VisualTile[BOARD_SIZE][BOARD_SIZE];

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                visualBoard[i][j] = new VisualTile(BLACK_BOARD);
                j++;
                visualBoard[i][j] = new VisualTile(WHITE_BOARD);
            }
            i++;             //alternating colours.
            for (int j = 0; j < BOARD_SIZE; j++) {
                visualBoard[i][j] = new VisualTile(WHITE_BOARD);
                j++;
                visualBoard[i][j] = new VisualTile(BLACK_BOARD);
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
            for (VisualTile tile : visualBoard[i]) {
                sb.append(" ");
                if (tile.hasPiece()) {
                    sb.append(tile.piece.id);
                } else {
                    sb.append(tile.tileColour == WHITE_BOARD ? '#' : '-'); //// white symbol, black symbol

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

