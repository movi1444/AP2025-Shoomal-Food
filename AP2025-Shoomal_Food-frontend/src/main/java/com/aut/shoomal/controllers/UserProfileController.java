package com.aut.shoomal.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import com.aut.shoomal.dto.response.UserResponse;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class UserProfileController extends AbstractBaseController {

    @FXML private Label nameLabel;
    @FXML private Label phoneLabel;
    @FXML private Label emailLabel;
    @FXML private Label roleLabel;
    @FXML private Label addressLabel;
    @FXML private Label bankInfoLabel;

    private UserResponse loggedInUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
    }

    public void setLoggedInUser(UserResponse user) {
        this.loggedInUser = user;
        if (loggedInUser != null) {
            nameLabel.setText("Full Name: " + loggedInUser.getName());
            phoneLabel.setText("Phone: " + loggedInUser.getPhoneNumber());
            emailLabel.setText("Email: " + (loggedInUser.getEmail() != null ? loggedInUser.getEmail() : "N/A"));
            roleLabel.setText("Role: " + loggedInUser.getRole());
            addressLabel.setText("Address: " + (loggedInUser.getAddress() != null ? loggedInUser.getAddress() : "N/A"));
            if (loggedInUser.getBank() != null) {
                bankInfoLabel.setText("Bank Info: " + loggedInUser.getBank().getBankName() + " - " + loggedInUser.getBank().getAccountNumber());
            } else {
                bankInfoLabel.setText("Bank Info: N/A");
            }
        }
    }

    @FXML
    private void handleBackToMain(ActionEvent event) {
        System.out.println("Back to Main button clicked!");
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        try {
            Parent mainRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/aut/shoomal/views/MainView.fxml")));
            Scene newScene = new Scene(mainRoot, stage.getWidth(), stage.getHeight());
            newScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/aut/shoomal/styles/MainView.css")).toExternalForm());
            stage.setScene(newScene);
            stage.setTitle("Shoomal Food");
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to load MainView.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert("Navigation Error", "Failed to load main page.", Alert.AlertType.ERROR, null);
        }
    }
}