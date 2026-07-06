package com.stockin;

import java.io.InputStream;

import com.stockin.config.DatabaseInitializer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        DatabaseInitializer.initialize();

        loadCustomFonts();

        FXMLLoader loader =
                new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));

        Scene scene = new Scene(loader.load());

        stage.setTitle("StockIn");

        stage.setScene(scene);

        stage.show();

    }

    private void loadCustomFonts() {

        loadFont("/fonts/Permanent_Marker/PermanentMarker-Regular.ttf", 56);
        loadFont("/fonts/Caveat/Caveat-Bold.ttf", 24);

    }

    private void loadFont(String resourcePath, double size) {

        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {

            if (is == null) {

                System.out.println("[FONT] NOT FOUND on classpath: " + resourcePath);
                return;

            }

            Font font = Font.loadFont(is, size);

            if (font == null) {

                System.out.println("[FONT] Found file but failed to parse: " + resourcePath);

            } else {

                System.out.println("[FONT] Loaded OK -> family name to use in CSS: \"" + font.getFamily() + "\" (from " + resourcePath + ")");

            }

        } catch (Exception e) {

            System.out.println("[FONT] Error loading " + resourcePath + " -> " + e.getMessage());

        }

    }

    public static void main(String[] args) {

        launch(args);

    }

}