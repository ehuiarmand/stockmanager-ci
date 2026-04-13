package com.inphb.icgl.stocks.controller;

import com.inphb.icgl.stocks.utils.ResourceImageLoader;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;

public class SplashScreenController {

    @FXML
    private ImageView imgBanner;
    @FXML
    private ImageView imgLogo;

    @FXML
    public void initialize() {
        ResourceImageLoader.loadIfPresent(imgBanner, "/images/banner.png");
        ResourceImageLoader.loadIfPresent(imgLogo, "/images/logo.png");
    }
}
