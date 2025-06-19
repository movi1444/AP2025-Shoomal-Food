package com.aut.shoomal.controllers;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.GridPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class SignUpController implements Initializable {

    @FXML private GridPane requiredFieldsSection;
    @FXML private TextField fullNameField;
    @FXML private TextField phoneNumberField;
    @FXML private PasswordField passwordField;
    @FXML private ChoiceBox<String> roleChoiceBox;
    @FXML private TextArea addressArea;
    @FXML private Button nextButton;

    @FXML private GridPane optionalFieldsSection;
    @FXML private TextField emailField;
    @FXML private TextField bankNameField;
    @FXML private TextField accountNumberField;
    @FXML private Button uploadImageButton;

    @FXML private Button backButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (roleChoiceBox != null) {
            ObservableList<String> roles = FXCollections.observableArrayList(
                    "خریدار",
                    "فروشنده",
                    "پیک"
            );
            roleChoiceBox.setItems(roles);
            roleChoiceBox.getSelectionModel().selectFirst();
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
        if (backButton != null) {
            backButton.setOnAction(event -> handlePreviousSection());
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

    private boolean isPersianCharacter(char c) {
        return (c >= '\u0600' && c <= '\u06FF') ||
                (c >= '\u0750' && c <= '\u077F') ||
                (c >= '\uFB50' && c <= '\uFDFF') ||
                (c >= '\uFE70' && c <= '\uFEFF');
    }

    private void addTextDirectionListener(TextField control) {
        if (control == null) return;
        control.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                if (isPersianCharacter(newValue.charAt(0))) {
                    control.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                    control.setAlignment(Pos.CENTER_RIGHT);
                } else {
                    control.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                    control.setAlignment(Pos.CENTER_LEFT);
                }
            } else {
                control.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                control.setAlignment(Pos.CENTER_LEFT);
            }
        });
    }

    private void addTextAreaDirectionListener(TextArea control) {
        if (control == null) return;
        control.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                if (isPersianCharacter(newValue.charAt(0))) {
                    control.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                } else {
                    control.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                }
            } else {
                control.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            }
        });
    }

    private void addChoiceBoxDirectionListener(ChoiceBox<String> choiceBox) {
        if (choiceBox == null) return;
        choiceBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                if (isPersianCharacter(newValue.charAt(0))) {
                    choiceBox.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                } else {
                    choiceBox.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                }
            } else {
                choiceBox.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            }
        });
    }
}