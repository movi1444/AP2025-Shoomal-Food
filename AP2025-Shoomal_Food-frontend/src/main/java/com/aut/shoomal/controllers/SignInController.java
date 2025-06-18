package com.aut.shoomal.controllers;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class SignInController implements Initializable {

    @FXML
    private TextField phoneNumberField;

    @FXML
    private TextField passwordField;

    @FXML
    private Button enterButton;

    @FXML
    private Hyperlink signUpLink;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    @FXML
    private void handleEnterButton() {
        String phoneNumber = phoneNumberField.getText();
        String password = passwordField.getText();

        System.out.println("Enter button clicked!");
        System.out.println("Phone Number: " + phoneNumber);
        System.out.println("Password: " + password);

        if (phoneNumber.isEmpty() || password.isEmpty()) {
            System.out.println("Phone number and password are required.");
        } else {
        }
    }

    @FXML
    private void handleSignUpLink() {
        System.out.println("Sign up link clicked!");

        Stage stage = (Stage) signUpLink.getScene().getWindow();
        Parent currentRoot = signUpLink.getScene().getRoot();

        try {
            Parent signUpRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/aut/shoomal/SignUpView.fxml")));

            StackPane transitionContainer = new StackPane();
            transitionContainer.getChildren().addAll(currentRoot, signUpRoot);

            signUpRoot.setTranslateX(stage.getWidth());

            Scene newScene = new Scene(transitionContainer, stage.getWidth(), stage.getHeight());
            newScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/aut/shoomal/styles/Styles.css")).toExternalForm());
            stage.setScene(newScene);

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(600), signUpRoot);
            slideIn.setFromX(stage.getWidth());
            slideIn.setToX(0);

            slideIn.setOnFinished(event -> {
                transitionContainer.getChildren().remove(currentRoot);
            });

            slideIn.play();

        } catch (IOException e) {
            System.err.println("Failed to load SignUpView.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }
}