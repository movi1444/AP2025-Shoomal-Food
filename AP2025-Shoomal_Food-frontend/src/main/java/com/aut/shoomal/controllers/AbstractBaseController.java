package com.aut.shoomal.controllers;

import javafx.beans.value.ObservableValue;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.net.URL;
import java.util.ResourceBundle;

public abstract class AbstractBaseController implements Initializable {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    protected boolean isPersianCharacter(char c) {
        return (c >= '\u0600' && c <= '\u06FF') ||
                (c >= '\u0750' && c <= '\u077F') ||
                (c >= '\uFB50' && c <= '\uFDFF') ||
                (c >= '\uFE70' && c <= '\uFEFF');
    }

    protected <T extends Node> void addDirectionListenerForObservableString(ObservableValue<String> observable, T control) {
        if (observable == null || control == null) return;
        observable.addListener((obs, oldValue, newValue) -> {
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

    protected void addTextDirectionListener(TextField control) {
        addDirectionListenerForObservableString(control.textProperty(), control);
    }

    protected void addTextAreaDirectionListener(TextArea control) {
        addDirectionListenerForObservableString(control.textProperty(), control);
    }

    protected void addChoiceBoxDirectionListener(ChoiceBox<String> choiceBox) {
        addDirectionListenerForObservableString(choiceBox.valueProperty(), choiceBox);
    }

    protected void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}