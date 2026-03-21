package org.chess;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main extends Application {

    enum Options {
        TWO_PLAYER("Two Player Game", "Play with a friend!"),
        VS_BOT("One Player Game", "Play against a beginner bot 'AI'!"),
        CHANGE_COLOUR_SCHEME("Change Board Appearance", "Pick how the board looks!"),
        INFO("INFO", "Read about what this little project is.");

        public final String title;
        public final String tip;

        Options(String title, String tip) {
            this.title = title;
            this.tip = tip;
        }
    }


    public static Scene scene;
    public static Settings settings;
    static ImageView backArrow;

    StackPane root;
    Pane menuRoot;

    Rectangle fadeCover;
    DoubleProperty stageHeight = new SimpleDoubleProperty();
    DoubleProperty stageWidth = new SimpleDoubleProperty();

    private BooleanProperty buttonChosen = new SimpleBooleanProperty(false);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        settings = SettingsManager.load();
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/images/w-king.png")).toExternalForm()));
        stageHeight.bind(stage.heightProperty());
        stageWidth.bind(stage.widthProperty());

        stage.setTitle("Chess App");
        stage.setScene(makeMainMenu());
        stage.setMinWidth(300);
        stage.setMinHeight(300);
        stage.show();
    }


    private Scene makeMainMenu() {
        scene = new Scene(getSceneContent(), 1000, 700);
        return scene;
    }

    private Pane getSceneContent() {
        menuRoot = new StackPane(getBackground(),
                getOptionsVBox(),
                makeForegroundForFadeTransition());
        root = new StackPane(menuRoot, getArrowImage(), makeForegroundForFadeTransition());
        root.getStylesheets().addAll(
                Objects.requireNonNull(ChessBoard.class.getResource(settings.style)).toExternalForm(),
                Objects.requireNonNull(ChessBoard.class.getResource("/chess-stylesheet.css")).toExternalForm());
        return root;
    }

    Node getArrowImage() {
        backArrow = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/images/back-arrow-icon.png")).toExternalForm()));
        backArrow.setSmooth(true);
        backArrow.setPreserveRatio(true);
        backArrow.setOpacity(0.0);
        final double size = 0.1;
        backArrow.fitWidthProperty().bind(stageWidth.multiply(size));
        backArrow.fitHeightProperty().bind(stageHeight.multiply(size));
        backArrow.setPickOnBounds(true);

        backArrow.setOnMouseClicked(e -> goBackToMainMenu());
        backArrow.setOnMouseEntered(e -> {
            backArrow.setScaleX(1.1);
            backArrow.setScaleY(1.1);
        });
        backArrow.setOnMouseExited(e -> {
            backArrow.setScaleX(1.0);
            backArrow.setScaleY(1.0);
        });
        StackPane.setMargin(backArrow, new Insets(10));
        StackPane.setAlignment(backArrow, Pos.TOP_RIGHT);
        return backArrow;
    }

    private void goBackToMainMenu() {
        buttonChosen.set(false);
        crossFadeTransition(menuRoot, false);
    }


    private Rectangle makeForegroundForFadeTransition() {
        fadeCover = new Rectangle();
        fadeCover.setFill(Color.WHITESMOKE);
        fadeCover.setMouseTransparent(true);
        fadeCover.setOpacity(0.0);
        fadeCover.heightProperty().bind(stageHeight);
        fadeCover.widthProperty().bind(stageWidth);
        return fadeCover;
    }

    private ImageView getTitlePicture() {
        Image image = new Image(Objects.requireNonNull(getClass().getResource("/images/menu-image.png")).toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);

        imageView.fitWidthProperty().bind(stageWidth.multiply(0.9));
        imageView.fitHeightProperty().bind(stageHeight.multiply(0.3));
        return imageView;
    }

    private Region getOptionsVBox() {
        Pane wrapper = new StackPane();
        VBox vBox = new VBox();
        vBox.setMaxWidth(Double.MAX_VALUE);
        vBox.getStyleClass().add("info-wrapper");
        vBox.getChildren().addAll(Objects.requireNonNull(getTitlePicture()));
        vBox.getChildren().addAll(Objects.requireNonNull(getOptionButtons()));
//        wrapper.getStyleClass().add("turn-counter-label");

        vBox.setAlignment(Pos.TOP_CENTER);
        wrapper.getChildren().add(vBox); /// add labels, string properties etc...

        return wrapper;
    }

    private Region getOptionButtons() {
        List<Node> ls = new ArrayList<>();
        for (Options o : Options.values()) {
            ls.add(makeButton(o));
        }

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.TOP_CENTER);
        vBox.getChildren().addAll(Objects.requireNonNull(ls));

        buttonChosen.addListener((observable, oldValue, newValue) -> {
            for (Node b : ls) {
                b.setDisable(newValue);
            }
        });
        return vBox;
    }

    private Node makeButton(Options op) {
        HBox wrapper = new HBox();
        Button button = new Button(op.title);
        Label label = new Label(op.tip);
        label.setOpacity(0.0);
        label.setWrapText(true);

        wrapper.getChildren().addAll(button, label);
        wrapper.getStyleClass().add("info-wrapper");

        button.getStyleClass().add("chess-button");
        label.getStyleClass().add("chess-label");

        /// why so difficult...
        button.translateXProperty().bind(stageWidth.multiply(0.1));
        label.translateXProperty().bind(button.translateXProperty());

        button.setOnAction(event -> handleButtonPressed(op));
        button.setOnMouseEntered(event -> addTip(label));
        button.setOnMouseExited(event -> removeTip(label));
        return wrapper;
    }

    private void removeTip(Label label) {
        FadeTransition fadeOut = new FadeTransition(new Duration(300), label);
        fadeOut.setToValue(0);
        fadeOut.play();
    }

    private void addTip(Label label) {
        FadeTransition fadeIn = new FadeTransition(new Duration(300), label);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private void handleButtonPressed(Options op) {
        buttonChosen.set(true);
        Node nextRoot = ChessBoard.boardRoot;
        switch (op) {

            case CHANGE_COLOUR_SCHEME:
                new ChessBoard();
                nextRoot = ChessBoard.boardRoot;
                break;
            case INFO:
                nextRoot = new InformationPane();
                return;


            case TWO_PLAYER:
                new ChessLoop();
                nextRoot = ChessLoop.boardRoot;
                break;
            case VS_BOT:
                new ChessLoop(); //implement (must ask for either white or black player)
                nextRoot = ChessBoard.boardRoot;
                break;
        }
        crossFadeTransition(nextRoot, true);

    }

    private void crossFadeTransition(Node nextRoot, boolean showBackArrow) {
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), fadeCover);
        fadeOut.setFromValue(0);
        fadeOut.setToValue(1);

        fadeOut.setOnFinished(e -> {
            root.getChildren().set(0, nextRoot);
            if (showBackArrow) backArrow.setOpacity(1.0);
            else backArrow.setOpacity(0.0);
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), fadeCover);
            fadeIn.setFromValue(1);
            fadeIn.setToValue(0);
            fadeIn.play();

        });
        fadeOut.play();
    }

    private Rectangle getBackground() {
        Rectangle background = new Rectangle();
        background.getStyleClass().add("background");
        return background;
    }

}
