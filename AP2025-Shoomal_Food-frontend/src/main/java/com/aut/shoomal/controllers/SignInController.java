package com.aut.shoomal.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.layout.StackPane;
import javafx.animation.TranslateTransition;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

import com.aut.shoomal.dto.request.UserLoginRequest;
import com.aut.shoomal.dto.response.UserLoginResponse;
import com.aut.shoomal.utils.PreferencesManager;

public class SignInController extends AbstractBaseController {

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
        super.initialize(url, resourceBundle);

        if (phoneNumberField != null) addTextDirectionListener(phoneNumberField);
        if (passwordField != null) addTextDirectionListener(passwordField);
    }

    @FXML
    private void handleEnterButton() {
        String phoneNumber = phoneNumberField.getText();
        String password = passwordField.getText();

        System.out.println("Enter button clicked!");
        System.out.println("Phone Number: " + phoneNumber);

        if (phoneNumber.isEmpty() || password.isEmpty()) {
            showAlert("خطا", "شماره تلفن و رمز عبور الزامی هستند.");
            System.out.println("Phone number and password are required.");
        } else {
            authenticateUser(phoneNumber, password);
        }
    }

    private void authenticateUser(String phoneNumber, String password) {
        UserLoginRequest loginRequest = new UserLoginRequest(phoneNumber, password);

        sendHttpRequest(
                "http://localhost:8080/auth/login",
                "POST",
                loginRequest,
                UserLoginResponse.class,
                response -> {
                    showAlert("موفقیت", response.getMessage());
                    System.out.println("Login successful! Token: " + response.getToken());

                    PreferencesManager.saveAuthInfo(response.getToken(), response.getUser());

                    try {
                        Stage stage = (Stage) enterButton.getScene().getWindow();
                        Parent currentRoot = enterButton.getScene().getRoot();

                        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/com/aut/shoomal/MainView.fxml")));
                        Parent mainRoot = loader.load();

                        MainController mainController = loader.getController();
                        if (mainController != null) {
                            mainController.setLoggedInUser(response.getUser());
                        }

                        StackPane transitionContainer = new StackPane();
                        transitionContainer.getChildren().addAll(currentRoot, mainRoot);

                        mainRoot.setTranslateY(-stage.getHeight());

                        Scene newScene = new Scene(transitionContainer, stage.getWidth() - 15, stage.getHeight() - 38);
                        newScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/aut/shoomal/styles/MainView.css")).toExternalForm());
                        stage.setScene(newScene);
                        stage.setTitle("Shoomal Food");
                        stage.show();

                        TranslateTransition slideIn = new TranslateTransition(Duration.millis(500), mainRoot);
                        slideIn.setFromY(-stage.getHeight());
                        slideIn.setToY(0);

                        slideIn.setOnFinished(event -> {
                            transitionContainer.getChildren().remove(currentRoot);
                        });

                        slideIn.play();

                    } catch (IOException e) {
                        System.err.println("Failed to load MainView.fxml: " + e.getMessage());
                        e.printStackTrace();
                        showAlert("خطای ناوبری", "خطا در بارگذاری صفحه اصلی برنامه.", AlertType.ERROR, null);
                    }
                },
                (statusCode, errorMessage) -> {
                    String displayMessage;
                    if (statusCode == -1) {
                        displayMessage = "نمی توان به سرور متصل شد. لطفا اتصال اینترنت خود را بررسی کنید.";
                    } else if (statusCode == 401) {
                        displayMessage = "نام کاربری یا رمز عبور اشتباه است.";
                    } else if (statusCode == 400) {
                        displayMessage = "ورودی نامعتبر: " + errorMessage;
                    } else {
                        displayMessage = "هنگام ورود به سیستم خطای غیرمنتظره ای رخ داد: " + errorMessage;
                    }
                    showAlert("ورود ناموفق", displayMessage, AlertType.ERROR, null);
                    System.err.println("Login failed: Status " + statusCode + ", Error: " + errorMessage);
                },
                null
        );
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

            Scene newScene = new Scene(transitionContainer, stage.getWidth() - 15, stage.getHeight() - 38);
            newScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/aut/shoomal/styles/SignInUpStyles.css")).toExternalForm());
            stage.setScene(newScene);

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(500), signUpRoot);
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