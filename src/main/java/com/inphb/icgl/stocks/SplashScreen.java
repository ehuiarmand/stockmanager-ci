package com.inphb.icgl.stocks;

import javafx.animation.PauseTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class SplashScreen extends Stage {

    public SplashScreen() throws Exception {
        initStyle(StageStyle.UNDECORATED);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SplashScreen.fxml"));
        Scene scene = new Scene(loader.load(), 500, 300);
        setScene(scene);
    }

    public void showAndProceed(Stage loginStage) {
        centerOnScreen();
        setAlwaysOnTop(true);
        show();
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> {
            close();
            loginStage.centerOnScreen();
            loginStage.show();
            loginStage.toFront();
            loginStage.requestFocus();
            loginStage.setAlwaysOnTop(false);
        });
        pause.play();
    }
}
