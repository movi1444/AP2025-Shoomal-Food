package com.aut.shoomal.controllers;

import com.aut.shoomal.exceptions.FrontendServiceException;
import com.aut.shoomal.service.AuthService;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;
import java.net.URL;
import java.util.*;
import javafx. scene. control. Alert. AlertType;

import com.aut.shoomal.dto.request.UserRegisterRequest;
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
    @FXML private ImageView profileImageView;

    private String profileImageBase64String;
    private AuthService authService;

    private final Map<String, String> roleMapping = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        authService = new AuthService();

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
            backToLoginLink.setOnAction(event -> navigateTo(backToLoginLink, "/com/aut/shoomal/views/SignInView.fxml", "/com/aut/shoomal/styles/SignInUpStyles.css", TransitionType.SLIDE_LEFT));
        }

        if (fullNameField != null) addTextDirectionListener(fullNameField);
        if (phoneNumberField != null) addTextDirectionListener(phoneNumberField);
        if (passwordField != null) addTextDirectionListener(passwordField);
        if (addressArea != null) addTextAreaDirectionListener(addressArea);
        if (roleChoiceBox != null) addChoiceBoxDirectionListener(roleChoiceBox);

        if (emailField != null) addTextDirectionListener(emailField);
        if (bankNameField != null) addTextDirectionListener(bankNameField);
        if (accountNumberField != null) addTextDirectionListener(accountNumberField);

        if (uploadImageButton != null) {
            uploadImageButton.setOnAction(event -> handleUploadImageButton());
        }
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

                slideInOptional.setOnFinished(event -> optionalFieldsSection.setTranslateY(0));

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

                slideInRequired.setOnFinished(event -> requiredFieldsSection.setTranslateY(0));

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
            showAlert("خطای اعتبارسنجی", "لطفاً تمام فیلدهای الزامی (نام کامل، شماره تلفن، رمز عبور، نقش، آدرس) را پر کنید.", AlertType.WARNING, null);
            return;
        }

        String role = roleMapping.get(selectedRoleInPersian);
        if (role == null) {
            showAlert("خطای اعتبارسنجی", "نقش انتخاب شده نامعتبر است. لطفاً یک نقش معتبر انتخاب کنید.", AlertType.WARNING, null);
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

        authService.register(registerRequest)
                .thenAccept(registerResponse -> Platform.runLater(() -> {
                    if (registerResponse != null)
                    {
                        showAlert("موفقیت", "ثبت نام با موفقیت انجام شد.", Alert.AlertType.INFORMATION, null);
                        navigateTo(
                                submitButton,
                                "/com/aut/shoomal/views/SignInView.fxml",
                                "/com/aut/shoomal/styles/SignInUpStyles.css",
                                TransitionType.SLIDE_LEFT
                        );
                    }
                    else
                        showAlert("خطا", "خطا در ثبت نام.", Alert.AlertType.ERROR, null);
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        if (e.getCause() instanceof FrontendServiceException fsException)
                            showAlert(fsException);
                        else
                            showAlert("خطا", "خطای غیرمنتظره در ثبت نام: " + e.getMessage(), Alert.AlertType.ERROR, null);
                    });
                    return null;
                });
    }
}