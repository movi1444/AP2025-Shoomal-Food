package com.aut.shoomal.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.TranslateTransition;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.ResourceBundle;

import com.aut.shoomal.dto.request.UserLoginRequest;
import com.aut.shoomal.dto.response.UserLoginResponse;
import com.aut.shoomal.dto.response.ApiResponse;

public class SignInController extends AbstractBaseController {

    @FXML
    private TextField phoneNumberField;

    @FXML
    private TextField passwordField;

    @FXML
    private Button enterButton;

    @FXML
    private Hyperlink signUpLink;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
        System.out.println("Password: " + password);

        if (phoneNumber.isEmpty() || password.isEmpty()) {
            showAlert("خطا", "شماره تلفن و رمز عبور الزامی هستند.");
            System.out.println("Phone number and password are required.");
        } else {
            authenticateUser(phoneNumber, password);
        }
    }

    private void authenticateUser(String phoneNumber, String password) {
        try {
            URL url = new URL("https://localhost:8080/auth/login");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            UserLoginRequest loginRequest = new UserLoginRequest(phoneNumber, password);
            String jsonInputString = objectMapper.writeValueAsString(loginRequest);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    UserLoginResponse loginResponse = objectMapper.readValue(response.toString(), UserLoginResponse.class);
                    showAlert("موفقیت", loginResponse.getMessage());
                    System.out.println("Login successful! Token: " + loginResponse.getToken());
                }
            } else {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    ApiResponse apiResponse = objectMapper.readValue(response.toString(), ApiResponse.class);
                    showAlert("ورود ناموفق", apiResponse.getError() != null ? apiResponse.getError() : apiResponse.getMessage());
                    System.err.println("Login failed: " + (apiResponse.getError() != null ? apiResponse.getError() : apiResponse.getMessage()));
                }
            }
        } catch (IOException e) {
            showAlert("خطای شبکه", "نمی توان به سرور متصل شد. لطفا اتصال اینترنت خود را بررسی کنید.");
            System.err.println("Network or IO error during login: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert("خطایی رخ داد", "هنگام ورود به سیستم خطای غیرمنتظره ای رخ داد.");
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
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