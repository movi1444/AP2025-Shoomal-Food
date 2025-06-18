package com.aut.shoomal.controllers;

import javafx.animation.FadeTransition;
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

        FadeTransition fadeOut = new FadeTransition(Duration.millis(800), currentRoot);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(event -> {
            try {
                Parent signUpRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/aut/shoomal/SignUpView.fxml")));

                double currentWidth = stage.getWidth();
                double currentHeight = stage.getHeight();

                Scene newScene = new Scene(signUpRoot, currentWidth, currentHeight);
                newScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/aut/shoomal/styles/Styles.css")).toExternalForm());
                stage.setScene(newScene);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(800), signUpRoot);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();

            } catch (IOException e) {
                System.err.println("Failed to load SignUpView.fxml: " + e.getMessage());
                e.printStackTrace();
            }
        });
        fadeOut.play();
    }
}