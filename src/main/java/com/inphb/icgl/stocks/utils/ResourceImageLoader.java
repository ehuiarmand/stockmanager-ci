package com.inphb.icgl.stocks.utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;

public final class ResourceImageLoader {

    private ResourceImageLoader() {
    }

    public static void loadIfPresent(ImageView imageView, String resourcePath) {
        if (imageView == null || resourcePath == null || resourcePath.isBlank()) {
            return;
        }
        try (InputStream inputStream = ResourceImageLoader.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null || inputStream.available() <= 0) {
                return;
            }
            Image image = new Image(inputStream);
            if (image.isError()) {
                return;
            }
            imageView.setImage(image);
            imageView.setVisible(true);
            imageView.setManaged(true);
        } catch (Exception ignored) {
        }
    }
}
