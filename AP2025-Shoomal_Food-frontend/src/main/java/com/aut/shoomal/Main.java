package com.aut.shoomal;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

import com.aut.shoomal.controllers.MainController;
import com.aut.shoomal.dto.response.UserResponse;
import com.aut.shoomal.utils.PreferencesManager;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {

        UserResponse loggedInUser = PreferencesManager.attemptAutoLogin();

        if (loggedInUser != null) {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/com/aut/shoomal/views/MainView.fxml")));
            Parent mainRoot = loader.load();

            MainController mainController = loader.getController();
            if (mainController != null) {
                mainController.setLoggedInUser(loggedInUser);
            }

            Scene scene = new Scene(mainRoot, 1280, 720);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/aut/shoomal/styles/MainView.css")).toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setTitle("Shoomal Food");
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.show();
        } else {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/aut/shoomal/views/SignInView.fxml")));
            primaryStage.setTitle("Shoomal Food");
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            Scene scene = new Scene(root, 1280, 720);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/aut/shoomal/styles/SignInUpStyles.css")).toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.show();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}