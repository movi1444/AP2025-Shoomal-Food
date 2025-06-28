package com.aut.shoomal.controllers;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;
import java.net.URL;
import java.util.*;

import com.aut.shoomal.dto.request.UserRegisterRequest;
import com.aut.shoomal.dto.response.UserRegisterResponse;
import com.aut.shoomal.dto.response.BankInfoResponse;

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

    @FXML
    private ImageView profileImageView;

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
        navigateToSignInView(backToLoginLink);
    }

    @FXML
    private void handleUploadImageButton() {
        profileImageBase64String = handleImageUploadAndConvert(uploadImageButton, profileImageView);
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

        sendHttpRequest(
                "http://localhost:8080/auth/register",
                "POST",
                registerRequest,
                UserRegisterResponse.class,
                response -> {
                    showAlert("Success", "ثبت نام با موفقیت انجام شد! شناسه کاربری: " + response.getUserId() + "\nتوکن: " + response.getToken());
                    navigateToSignInView(submitButton);
                },
                (statusCode, errorMessage) -> {
                    showAlert("Registration Failed", errorMessage);
                }
        );
    }
}