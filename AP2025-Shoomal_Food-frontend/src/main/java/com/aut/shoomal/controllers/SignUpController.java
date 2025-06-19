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
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

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


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

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

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(600), signInRoot);
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

    private void handleSubmitSignUp() {
        System.out.println("Submit button clicked! Final registration logic here.");
    }
}