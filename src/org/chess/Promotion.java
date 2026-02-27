package org.chess;


import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.function.Function;

import static org.chess.ChessBoard.root;
import static org.chess.ChessBoard.boardArray;
import static org.chess.GameLoop.updateGameLoop;
import static org.chess.TeamAttributes.BLACK_TEAM;
import static org.chess.TeamAttributes.WHITE_TEAM;


public class Promotion {
    HBox pieceSelectRibbon;
    Label selected;
    Rectangle overlay;
    BorderPane borderPane;

    enum PromotionEnum {
        QUEEN("Queen", "-queen.png", Queen::new),
        ROOK("Rook", "-rook.png", Rook::new),
        BISHOP("Bishop", "-bishop.png", Bishop::new),
        KNIGHT("Knight", "-knight.png", Knight::new);

        public final String displayName;
        public final String partialFileName;
        public ImageView view;
        public StackPane wrapper;

        // This is the only thing a little bit beyond my means. It is like a Lambda function.
        // It can call the constructor of the class. Hence, 'new'.
        public final Function<TeamAttributes, Piece> function;

        PromotionEnum(String displayName, String partialFileName, Function<TeamAttributes, Piece> function) {
            this.displayName = displayName;
            this.partialFileName = partialFileName;
            this.function = function;
        }
    }

    public Promotion(Piece promotionCandidate, StackPane root) {
        borderPane = new BorderPane();
        pieceSelectRibbon = new HBox();
        double size = 0.5;

        for (PromotionEnum piece : PromotionEnum.values()) {

            piece.view = promotionCandidate.getImageView(piece.partialFileName);
            piece.view.setFitWidth(60);
            piece.view.setFitHeight(60);
            piece.view.fitWidthProperty().bind(pieceSelectRibbon.heightProperty().multiply(0.4));
            piece.view.fitHeightProperty().bind(pieceSelectRibbon.heightProperty().multiply(0.4));

            piece.wrapper = new StackPane();
            piece.wrapper.setBackground(Background.fill(Color.LIGHTBLUE));
            piece.wrapper.setMaxSize(100, 100);
            piece.wrapper.setMinSize(10, 10);

            piece.wrapper.prefWidthProperty().bind(pieceSelectRibbon.heightProperty().multiply(size));
            piece.wrapper.prefHeightProperty().bind(pieceSelectRibbon.heightProperty().multiply(size));

            piece.wrapper.setOnMouseEntered(mouseEvent -> enterView(piece));
            piece.wrapper.setOnMouseExited(mouseEvent -> exitView(piece));

            piece.wrapper.setOnMouseClicked(mouseEvent -> selectPromotion(promotionCandidate, piece));

            piece.wrapper.getChildren().add(piece.view);
            piece.wrapper.setStyle("""
                    -fx-margin: 5;
                    -fx-border-color: black;
                    -fx-border-width: 8;
                    """);
            pieceSelectRibbon.getChildren().add(piece.wrapper);
        }

        pieceSelectRibbon.setAlignment(Pos.CENTER);
        pieceSelectRibbon.setStyle("""
                -fx-padding: 10;
                """);
        pieceSelectRibbon.prefHeightProperty().bind(borderPane.heightProperty().multiply(size));
        pieceSelectRibbon.prefWidthProperty().bind(borderPane.widthProperty().multiply(size));

        borderPane.setStyle("""
                -fx-margin: 10;
                -fx-border-width: 5;
                -fx-border-color: black;
                -fx-border-radius: 18;
                """);
        borderPane.setBackground(new Background(new BackgroundFill(
                promotionCandidate.teamColour.paint, new CornerRadii(20), Insets.EMPTY)));
        borderPane.setPadding(new Insets(10)); //does spacing

        Color contrastTeamColor = promotionCandidate.teamColour.paint.equals(WHITE_TEAM.paint) ?
                BLACK_TEAM.paint : WHITE_TEAM.paint;

        selected = new Label();
        selected.setAlignment(Pos.CENTER);
        selected.setTextFill(contrastTeamColor);
        StackPane selectedWrap = new StackPane();
        selectedWrap.getChildren().add(selected);
        borderPane.setBottom(selectedWrap);

        Label info = new Label("Select Promotion");
        info.setStyle("""
                -fx-font-size: 17;
                """);

        info.setTextFill(contrastTeamColor);
        StackPane wrapInfo = new StackPane();
        wrapInfo.getChildren().add(info);

        wrapInfo.setAlignment(Pos.CENTER);
        borderPane.setTop(wrapInfo);

        borderPane.setCenter(pieceSelectRibbon);

        borderPane.setMaxSize(400, 200);

        borderPane.prefHeightProperty().bind(root.heightProperty().multiply(0.1));
        borderPane.prefWidthProperty().bind(root.widthProperty().multiply(0.2));


        overlay = new Rectangle();
        overlay.widthProperty().bind(root.widthProperty());
        overlay.heightProperty().bind(root.heightProperty());
        overlay.setFill(Color.rgb(0, 0, 0, 0.4));
        overlay.setOnMouseClicked(Event::consume); // consumes my event.

        root.getChildren().addAll(overlay, borderPane);
        //TODO: GET THIS POSITIONING STUFF WORKING.
        double x = promotionCandidate.sprite.getX();
        double y = promotionCandidate.sprite.getY();
        System.out.println("X: " + x + " Y: " + y + "  ");
//        borderPane.setTranslateX(100.0);
//        borderPane.setTranslateY(50.0);


    }

    static final Color DEFAULT = Color.LIGHTBLUE;
    static final Color HOVER = Color.LIGHTGREEN;
    final Color CLICKED = Color.DARKGREEN;

    private void enterView(PromotionEnum piece) {
        piece.wrapper.setBackground(Background.fill(HOVER));
        selected.setText(piece.displayName);
    }

    private void selectPromotion(Piece pawn, PromotionEnum choice) {
        //// also is the 'close' for this 'window'
        choice.wrapper.setBackground(Background.fill(CLICKED));
        root.getChildren().removeAll(overlay, borderPane);
        Piece piece = choice.function.apply(pawn.teamColour);
        // using the tile because we have the static board.
        boardArray[pawn.coOrds[0]][pawn.coOrds[1]].piece.deleteSprite();
        boardArray[pawn.coOrds[0]][pawn.coOrds[1]].deletePiece();
        boardArray[pawn.coOrds[0]][pawn.coOrds[1]].addNewPiece(piece, pawn.coOrds);

        //// compensating for how java runs threads or something.
        //// hence why this is not called when promotion happens in GameLoop.movePiece();
        updateGameLoop();
    }

    private void exitView(PromotionEnum piece) {
        piece.wrapper.setBackground(Background.fill(DEFAULT));
        selected.setText("");

    }


}
