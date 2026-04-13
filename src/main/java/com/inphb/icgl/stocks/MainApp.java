package com.inphb.icgl.stocks;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        SplashScreen splash = new SplashScreen();

        FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
        Scene loginScene = new Scene(loginLoader.load(), 520, 480);
        loginScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        Stage loginStage = new Stage();
        loginStage.setTitle("StockManager CI - Connexion");
        loginStage.setScene(loginScene);
        loginStage.setResizable(false);
        loginStage.setWidth(520);
        loginStage.setHeight(480);
        loginStage.setAlwaysOnTop(true);

        splash.showAndProceed(loginStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
