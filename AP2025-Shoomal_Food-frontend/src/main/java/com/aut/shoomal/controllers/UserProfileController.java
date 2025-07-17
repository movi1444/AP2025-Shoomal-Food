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
import javafx.scene.Node;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Button;
import com.aut.shoomal.utils.PreferencesManager;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

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
    @FXML private ImageView profileImageView;

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

        if (profileImageView != null) {
            final double imageSize = 300.0;
            profileImageView.setFitWidth(imageSize);
            profileImageView.setFitHeight(imageSize);
            profileImageView.setPreserveRatio(true);

            Circle clip = new Circle(imageSize / 2, imageSize / 2, imageSize / 2);
            profileImageView.setClip(clip);

            profileImageView.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
                clip.setCenterX(newBounds.getWidth() / 2.0);
                clip.setCenterY(newBounds.getHeight() / 2.0);
                clip.setRadius(Math.min(newBounds.getWidth(), newBounds.getHeight()) / 2.0);
            });
        }
        super.setProfileImage(profileImageView, null);
    }

    public void setLoggedInUser() {
        profileService.getProfile(token)
                .thenAccept(userResponse -> {
                    loggedInUser = userResponse;
                    Platform.runLater(() -> {
                        if (loggedInUser != null) {
                            nameLabel.setText("نام : " + loggedInUser.getName());
                            nameLabel.getStyleClass().add("profile-info-label");
                            phoneLabel.setText("شماره همراه : " + loggedInUser.getPhoneNumber());
                            phoneLabel.getStyleClass().add("profile-info-label");
                            emailLabel.setText("ایمیل: " + (loggedInUser.getEmail() != null ? loggedInUser.getEmail() : "N/A"));
                            emailLabel.getStyleClass().add("profile-info-label");
                            roleLabel.setText("نقش : " + loggedInUser.getRole());
                            roleLabel.getStyleClass().add("profile-info-label");
                            addressLabel.setText("آدرس : " + (loggedInUser.getAddress() != null ? loggedInUser.getAddress() : "N/A"));
                            addressLabel.getStyleClass().add("profile-info-label");
                            if (loggedInUser.getRole().equalsIgnoreCase("buyer"))
                            {
                                transactionHistoryLink.setVisible(true);
                                transactionHistoryLink.setManaged(true);
                            }
                            if (loggedInUser.getBank() != null)
                                bankInfoLabel.setText("اطلاعات بانکی : " + loggedInUser.getBank().getBankName() + " - " + loggedInUser.getBank().getAccountNumber());
                            else
                                bankInfoLabel.setText("اطلاعات بانکی : N/A");
                            bankInfoLabel.getStyleClass().add("profile-info-label");
                            super.setProfileImage(profileImageView, loggedInUser.getProfileImageBase64());
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
        navigateTo(updateProfileLink, "/com/aut/shoomal/views/UpdateProfileView.fxml", "/com/aut/shoomal/styles/SignInUpStyles.css", TransitionType.SLIDE_RIGHT);
    }

    @FXML
    private void handleSignOut(ActionEvent event) {
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