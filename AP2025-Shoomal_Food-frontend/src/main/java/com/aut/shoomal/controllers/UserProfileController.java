package com.aut.shoomal.controllers;

import com.aut.shoomal.exceptions.FrontendServiceException;
import com.aut.shoomal.service.LogoutService;
import com.aut.shoomal.service.ProfileService;
import javafx.application.Platform;
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
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Button;
import com.aut.shoomal.utils.PreferencesManager;

public class UserProfileController extends AbstractBaseController {

    @FXML private Label nameLabel;
    @FXML private Label phoneLabel;
    @FXML private Label emailLabel;
    @FXML private Label roleLabel;
    @FXML private Label addressLabel;
    @FXML private Label bankInfoLabel;
    @FXML private Hyperlink updateProfileLink;
    @FXML private Hyperlink transactionHistoryLink;
    @FXML private Button signOutButton;

    private UserResponse loggedInUser;
    private String token;
    private LogoutService logoutService;
    private ProfileService profileService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        this.token = PreferencesManager.getJwtToken();
        logoutService = new LogoutService();
        profileService = new ProfileService();
        transactionHistoryLink.setVisible(false);
        transactionHistoryLink.setManaged(false);

        if (updateProfileLink != null) {
            updateProfileLink.setOnAction(this::handleUpdateProfile);
        }

        if (signOutButton != null) {
            signOutButton.setOnAction(this::handleSignOut);
        }
    }

    public void setLoggedInUser() {
        profileService.getProfile(token)
                .thenAccept(userResponse -> {
                    loggedInUser = userResponse;
                    Platform.runLater(() -> {
                        if (loggedInUser != null) {
                            nameLabel.setText("Full Name: " + loggedInUser.getName());
                            phoneLabel.setText("Phone: " + loggedInUser.getPhoneNumber());
                            emailLabel.setText("Email: " + (loggedInUser.getEmail() != null ? loggedInUser.getEmail() : "N/A"));
                            roleLabel.setText("Role: " + loggedInUser.getRole());
                            addressLabel.setText("Address: " + (loggedInUser.getAddress() != null ? loggedInUser.getAddress() : "N/A"));
                            if (loggedInUser.getRole().equalsIgnoreCase("buyer"))
                            {
                                transactionHistoryLink.setVisible(true);
                                transactionHistoryLink.setManaged(true);
                            }
                            if (loggedInUser.getBank() != null)
                                bankInfoLabel.setText("Bank Info: " + loggedInUser.getBank().getBankName() + " - " + loggedInUser.getBank().getAccountNumber());
                            else
                                bankInfoLabel.setText("Bank Info: N/A");
                        }
                    });
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        if (e.getCause() instanceof FrontendServiceException exception)
                            showAlert(exception);
                        else
                            showAlert("Unexpected Error", "An unexpected error occurred: " + e.getMessage(), Alert.AlertType.ERROR, null);
                    });
                    return null;
                });
    }

    @FXML
    private void handleBackToMain(ActionEvent event) {
        System.out.println("Back to Main button clicked!");
        navigateTo(
                (Node) event.getSource(),
                "/com/aut/shoomal/views/MainView.fxml",
                "/com/aut/shoomal/styles/MainView.css",
                TransitionType.SLIDE_LEFT,
                (MainController controller) -> {
                    controller.setLoggedInUser(loggedInUser);
                }
        );
    }

    @FXML
    private void handleUpdateProfile(ActionEvent event) {
        System.out.println("Update Profile link clicked!");
        navigateTo(updateProfileLink, "/com/aut/shoomal/views/UpdateProfileView.fxml", "/com/aut/shoomal/styles/SignInUpStyles.css", TransitionType.SLIDE_RIGHT);
    }

    @FXML
    private void handleSignOut(ActionEvent event) {
        System.out.println("Sign Out button clicked!");
        logoutService.logout(token)
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.isSuccess())
                        {
                            PreferencesManager.clearAuthInfo();
                            navigateToSignInView(signOutButton);
                            showAlert("Sign Out", "You have been successfully signed out.", Alert.AlertType.INFORMATION, null);
                        }
                        else
                            showAlert("Error", "Failed to logout: " + response.getError(), Alert.AlertType.ERROR, null);
                    });
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        if (e.getCause() instanceof FrontendServiceException fsException)
                            showAlert(fsException);
                        else
                            showAlert("Unexpected Error", "An unexpected error occurred: " + e.getMessage(), Alert.AlertType.ERROR, null);
                    });
                    return null;
                });
    }

    @FXML
    public void handleTransactionHistory(ActionEvent actionEvent)
    {
        navigateTo(transactionHistoryLink, "/com/aut/shoomal/views/TransactionHistoryView.fxml", "/com/aut/shoomal/styles/MainView.css", TransitionType.SLIDE_RIGHT);
    }
}