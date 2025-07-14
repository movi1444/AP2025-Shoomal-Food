package com.aut.shoomal.controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import java.net.URL;
import java.util.ResourceBundle;
import com.aut.shoomal.dto.response.UserResponse;
import com.aut.shoomal.utils.PreferencesManager;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Objects;
import javafx.scene.input.MouseEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;
import java.io.IOException;
import javafx.scene.Cursor;

public class MainController extends AbstractBaseController {

    @FXML private Label welcomeUserLabel;
    @FXML private Button logoutButton;
    @FXML private StackPane contentStackPane;

    @FXML private ScrollPane buyerDashboardScrollPane;
    @FXML private ScrollPane sellerDashboardScrollPane;
    @FXML private ScrollPane courierDashboardScrollPane;
    @FXML private ScrollPane adminDashboardScrollPane;
    @FXML private VBox defaultView;
    @FXML private ImageView profilePictureImageView;

    private UserResponse currentUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        hideAllDashboards();
        if (defaultView != null) {
            defaultView.setVisible(true);
            defaultView.setManaged(true);
        }

        if (logoutButton != null) {
            logoutButton.setOnAction(event -> navigateTo(logoutButton, "/com/aut/shoomal/views/SignInView.fxml", "/com/aut/shoomal/styles/SignInUpStyles.css", TransitionType.SLIDE_UP));
        }

        if (profilePictureImageView != null) {
            profilePictureImageView.setOnMouseClicked(this::handleProfilePictureClick);
            profilePictureImageView.setCursor(Cursor.HAND);
        }
        setProfileImage(null);
    }

    public void setLoggedInUser(UserResponse user) {
        this.currentUser = user;
        if (user != null) {
            if (welcomeUserLabel != null) {
                welcomeUserLabel.setText(user.getName() + " به شومال فود خوش آمدید!");
            }
            displayDashboardForRole(user.getRole());
            setProfileImage(user.getProfileImageBase64());
        } else {
            if (welcomeUserLabel != null) {
                welcomeUserLabel.setText("Welcome, Guest!");
            }
            displayDashboardForRole(null);
            setProfileImage(null);
        }
    }

    private void setProfileImage(String base64Image) {
        if (profilePictureImageView != null) {
            if (base64Image != null && !base64Image.isEmpty()) {
                try {
                    String cleanedBase64Image = base64Image.replaceAll("\\s", "");
                    byte[] imageBytes = Base64.getDecoder().decode(cleanedBase64Image);
                    Image image = new Image(new ByteArrayInputStream(imageBytes));
                    profilePictureImageView.setImage(image);
                    System.out.println("MainController: Profile image set from Base64.");
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid Base64 string for profile picture: " + e.getMessage());
                    profilePictureImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/aut/shoomal/images/default_profile.png"))));
                }
            } else {
                profilePictureImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/aut/shoomal/images/default_profile.png"))));
            }
        }
    }

    private void hideAllDashboards() {
        if (buyerDashboardScrollPane != null) {
            buyerDashboardScrollPane.setVisible(false);
            buyerDashboardScrollPane.setManaged(false);
        }
        if (sellerDashboardScrollPane != null) {
            sellerDashboardScrollPane.setVisible(false);
            sellerDashboardScrollPane.setManaged(false);
        }
        if (courierDashboardScrollPane != null) {
            courierDashboardScrollPane.setVisible(false);
            courierDashboardScrollPane.setManaged(false);
        }
        if (adminDashboardScrollPane != null) {
            adminDashboardScrollPane.setVisible(false);
            adminDashboardScrollPane.setManaged(false);
        }
        if (defaultView != null) {
            defaultView.setVisible(false);
            defaultView.setManaged(false);
        }
    }

    private void displayDashboardForRole(String role) {
        hideAllDashboards();

        ScrollPane targetPane = null;
        switch (role != null ? role.toLowerCase() : "") {
            case "buyer":
                targetPane = buyerDashboardScrollPane;
                break;
            case "seller":
                targetPane = sellerDashboardScrollPane;
                break;
            case "courier":
                targetPane = courierDashboardScrollPane;
                break;
            case "admin":
                targetPane = adminDashboardScrollPane;
                break;
            default:
                if (defaultView != null) {
                    defaultView.setVisible(true);
                    defaultView.setManaged(true);
                }
                break;
        }

        if (targetPane != null) {
            targetPane.setVisible(true);
            targetPane.setManaged(true);
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        System.out.println("Logout button clicked!");
        PreferencesManager.clearAuthInfo();
        if (logoutButton != null) {
            navigateToSignInView(logoutButton);
        }
    }

    private void handleProfilePictureClick(MouseEvent event) {
        System.out.println("Profile picture clicked! Navigating to user profile.");
        navigateToProfileView(profilePictureImageView);
    }

    private void navigateToProfileView(Node currentNode) {
        Stage stage = (Stage) currentNode.getScene().getWindow();
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/com/aut/shoomal/views/UserProfileView.fxml")));
            Parent profileRoot = loader.load();

            UserProfileController profileController = loader.getController();
            if (profileController != null) {
                profileController.setLoggedInUser(currentUser);
            }

            Scene newScene = new Scene(profileRoot, stage.getWidth(), stage.getHeight());
            stage.setScene(newScene);
            stage.setTitle("User Profile");
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to load UserProfileView.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert("Navigation Error", "Failed to load user profile page.", AlertType.ERROR, null);
        }
    }
}