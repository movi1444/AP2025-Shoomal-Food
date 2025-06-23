package com.aut.shoomal.controllers;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.GridPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aut.shoomal.dto.request.UserRegisterRequest;
import com.aut.shoomal.dto.response.UserRegisterResponse;
import com.aut.shoomal.dto.response.BankInfoResponse;
import com.aut.shoomal.utils.ImageToBase64Converter;

public class SignUpController extends AbstractBaseController {

    @FXML private GridPane requiredFieldsSection;
    @FXML private TextField fullNameField;
    @FXML private TextField phoneNumberField;
    @FXML private TextField passwordField;
    @FXML private ChoiceBox<String> roleChoiceBox;
    @FXML private TextArea addressArea;
    @FXML private Button nextButton;
    @FXML private Button submitButton;
    @FXML private Hyperlink backToLoginLink;

    @FXML private GridPane optionalFieldsSection;
    @FXML private TextField emailField;
    @FXML private TextField bankNameField;
    @FXML private TextField accountNumberField;
    @FXML private Button uploadImageButton;
    @FXML private Button backButton;
    private String profileImageBase64String;

    private final Map<String, String> roleMapping = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        roleMapping.put("خریدار", "buyer");
        roleMapping.put("فروشنده", "seller");
        roleMapping.put("پیک", "courier");


        if (roleChoiceBox != null) {
            ObservableList<String> roles = FXCollections.observableArrayList(
                    "خریدار",
                    "فروشنده",
                    "پیک"
            );
            roleChoiceBox.setItems(roles);
        }

        if (requiredFieldsSection != null && optionalFieldsSection != null) {
            requiredFieldsSection.setVisible(true);
            requiredFieldsSection.setManaged(true);
            optionalFieldsSection.setVisible(false);
            optionalFieldsSection.setManaged(false);
            requiredFieldsSection.setTranslateY(0);
            optionalFieldsSection.setTranslateY(0);
        }

        if (nextButton != null) {
            nextButton.setOnAction(event -> handleNextSection());
        }
        if (submitButton != null) {
            submitButton.setOnAction(event -> handleSubmitSignUp());
        }
        if (backButton != null) {
            backButton.setOnAction(event -> handlePreviousSection());
        }
        if (backToLoginLink != null) {
            backToLoginLink.setOnAction(event -> handleBackToLogin());
        }

        if (fullNameField != null) addTextDirectionListener(fullNameField);
        if (phoneNumberField != null) addTextDirectionListener(phoneNumberField);
        if (passwordField != null) addTextDirectionListener(passwordField);
        if (addressArea != null) addTextAreaDirectionListener(addressArea);
        if (roleChoiceBox != null) addChoiceBoxDirectionListener(roleChoiceBox);

        if (emailField != null) addTextDirectionListener(emailField);
        if (bankNameField != null) addTextDirectionListener(bankNameField);
        if (accountNumberField != null) addTextDirectionListener(accountNumberField);
    }

    private void handleNextSection() {
        System.out.println("Next button clicked! Initiating sequential transition.");

        if (requiredFieldsSection != null && optionalFieldsSection != null) {
            double fullHeight = requiredFieldsSection.getScene().getHeight();

            TranslateTransition slideOutRequired = new TranslateTransition(Duration.millis(400), requiredFieldsSection);
            slideOutRequired.setFromY(0);
            slideOutRequired.setToY(fullHeight);

            slideOutRequired.setOnFinished(e -> {
                requiredFieldsSection.setVisible(false);
                requiredFieldsSection.setManaged(false);
                requiredFieldsSection.setTranslateY(0);

                optionalFieldsSection.setTranslateY(fullHeight);
                optionalFieldsSection.setVisible(true);
                optionalFieldsSection.setManaged(true);

                TranslateTransition slideInOptional = new TranslateTransition(Duration.millis(400), optionalFieldsSection);
                slideInOptional.setFromY(fullHeight);
                slideInOptional.setToY(0);

                slideInOptional.setOnFinished(event -> {
                    optionalFieldsSection.setTranslateY(0);
                });

                slideInOptional.play();
            });

            slideOutRequired.play();
        }
    }

    private void handlePreviousSection() {
        System.out.println("Back button clicked! Initiating sequential transition (reverse).");

        if (requiredFieldsSection != null && optionalFieldsSection != null) {
            double fullHeight = optionalFieldsSection.getScene().getHeight();

            TranslateTransition slideOutOptional = new TranslateTransition(Duration.millis(400), optionalFieldsSection);
            slideOutOptional.setFromY(0);
            slideOutOptional.setToY(fullHeight);

            slideOutOptional.setOnFinished(e -> {
                optionalFieldsSection.setVisible(false);
                optionalFieldsSection.setManaged(false);
                optionalFieldsSection.setTranslateY(0);

                requiredFieldsSection.setTranslateY(fullHeight);
                requiredFieldsSection.setVisible(true);
                requiredFieldsSection.setManaged(true);

                TranslateTransition slideInRequired = new TranslateTransition(Duration.millis(400), requiredFieldsSection);
                slideInRequired.setFromY(fullHeight);
                slideInRequired.setToY(0);

                slideInRequired.setOnFinished(event -> {
                    requiredFieldsSection.setTranslateY(0);
                });

                slideInRequired.play();
            });

            slideOutOptional.play();
        }
    }

    @FXML
    private void handleBackToLogin() {
        System.out.println("Back to Login link clicked!");
        Stage stage = (Stage) backToLoginLink.getScene().getWindow();
        Parent currentRoot = backToLoginLink.getScene().getRoot();

        try {
            Parent signInRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/aut/shoomal/SignInView.fxml")));

            StackPane transitionContainer = new StackPane();
            transitionContainer.getChildren().addAll(currentRoot, signInRoot);

            signInRoot.setTranslateX(-stage.getWidth());

            Scene newScene = new Scene(transitionContainer, stage.getWidth(), stage.getHeight());
            newScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/aut/shoomal/styles/SignInUpStyles.css")).toExternalForm());
            stage.setScene(newScene);

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(500), signInRoot);
            slideIn.setFromX(-stage.getWidth());
            slideIn.setToX(0);

            slideIn.setOnFinished(event -> {
                transitionContainer.getChildren().remove(currentRoot);
            });

            slideIn.play();

        } catch (IOException e) {
            System.err.println("Failed to load SignInView.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUploadImageButton() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(uploadImageButton.getScene().getWindow());

        if (selectedFile != null) {
            try {
                profileImageBase64String = ImageToBase64Converter.convertImageFileToBase64(selectedFile.getAbsolutePath());
                System.out.println("Image uploaded and converted to Base64. Size: " + profileImageBase64String.length() + " characters.");
            } catch (IOException e) {
                System.err.println("Failed to convert image to Base64: " + e.getMessage());
                e.printStackTrace();
                showAlert("Image Error", "Failed to process image. Please try again.");
            }
        }
    }

    private void handleSubmitSignUp() {
        System.out.println("Submit button clicked! Sending registration data to backend.");

        String fullName = fullNameField.getText();
        String phoneNumber = phoneNumberField.getText();
        String password = passwordField.getText();
        String selectedRoleInPersian = roleChoiceBox.getValue();
        String address = addressArea.getText();
        String email = emailField.getText();
        String bankName = bankNameField.getText();
        String accountNumber = accountNumberField.getText();

        if (fullName.isEmpty() || phoneNumber.isEmpty() || password.isEmpty() || selectedRoleInPersian == null || address.isEmpty()) {
            showAlert("Validation Error", "لطفاً تمام فیلدهای الزامی (نام کامل، شماره تلفن، رمز عبور، نقش، آدرس) را پر کنید.");
            return;
        }

        String role = roleMapping.get(selectedRoleInPersian);
        if (role == null) {
            showAlert("Validation Error", "نقش انتخاب شده نامعتبر است. لطفاً یک نقش معتبر انتخاب کنید.");
            return;
        }

        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setFullName(fullName);
        registerRequest.setPhone(phoneNumber);
        registerRequest.setPassword(password);
        registerRequest.setRole(role);
        registerRequest.setAddress(address);

        if (!email.isEmpty()) {
            registerRequest.setEmail(email);
        }
        if (profileImageBase64String != null && !profileImageBase64String.isEmpty()) {
            registerRequest.setProfileImageBase64(profileImageBase64String);
        }
        if (!bankName.isEmpty() && !accountNumber.isEmpty()) {
            BankInfoResponse bankInfo = new BankInfoResponse();
            bankInfo.setBankName(bankName);
            bankInfo.setAccountNumber(accountNumber);
            registerRequest.setBankInfo(bankInfo);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBodyJson;
        try {
            requestBodyJson = objectMapper.writeValueAsString(registerRequest);
            System.out.println("Request JSON: " + requestBodyJson);
        } catch (IOException e) {
            System.err.println("Error serializing registration request: " + e.getMessage());
            e.printStackTrace();
            showAlert("System Error", "Could not prepare registration data. Please try again.");
            return;
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/auth/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                .build();

        new Thread(() -> {
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                javafx.application.Platform.runLater(() -> {
                    System.out.println("Response Status Code: " + response.statusCode());
                    System.out.println("Response Body: " + response.body());

                    if (response.statusCode() == 200) {
                        try {
                            UserRegisterResponse registerResponse = objectMapper.readValue(response.body(), UserRegisterResponse.class);
                            showAlert("Success", "ثبت نام با موفقیت انجام شد! شناسه کاربری: " + registerResponse.getUserId() + "\nتوکن: " + registerResponse.getToken());
                            handleBackToLogin();
                        } catch (IOException e) {
                            System.err.println("Error deserializing success response: " + e.getMessage());
                            e.printStackTrace();
                            showAlert("Registration Error", "ثبت نام با موفقیت انجام شد، اما پاسخ قابل پردازش نبود.");
                        }
                    } else {
                        try {
                            @SuppressWarnings("unchecked")
                            java.util.Map<String, String> errorResponse = objectMapper.readValue(response.body(), java.util.Map.class);
                            String errorMessage = errorResponse.getOrDefault("error", "خطای ناشناخته در هنگام ثبت نام رخ داد.");
                            showAlert("Registration Failed", errorMessage);
                        } catch (IOException e) {
                            System.err.println("Error parsing error response: " + e.getMessage());
                            e.printStackTrace();
                            showAlert("Registration Failed", "خطای غیرمنتظره‌ای رخ داد. کد وضعیت: " + response.statusCode());
                        }
                    }
                });
            } catch (IOException | InterruptedException e) {
                System.err.println("Network/API call error: " + e.getMessage());
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    showAlert("Network Error", "اتصال به سرور برقرار نشد. لطفاً اتصال اینترنت خود را بررسی کرده یا بعداً امتحان کنید.");
                });
            }
        }).start();
    }
}