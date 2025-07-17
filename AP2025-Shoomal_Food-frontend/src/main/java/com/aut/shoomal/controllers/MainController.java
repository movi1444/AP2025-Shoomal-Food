package com.aut.shoomal.controllers;

import com.aut.shoomal.service.RestaurantService;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.event.ActionEvent;
import java.net.URL;
import java.util.ResourceBundle;
import com.aut.shoomal.dto.response.UserResponse;
import com.aut.shoomal.utils.PreferencesManager;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.Cursor;
import javafx.scene.shape.Circle;

public class MainController extends AbstractBaseController {

    @FXML private Label welcomeUserLabel;
    @FXML private StackPane contentStackPane;

    @FXML private ScrollPane buyerDashboardScrollPane;
    @FXML private ScrollPane sellerDashboardScrollPane;
    @FXML private ScrollPane courierDashboardScrollPane;
    @FXML private ScrollPane adminDashboardScrollPane;
    @FXML private VBox defaultView;
    @FXML private ImageView profilePictureImageView;

    //Seller
    @FXML private MenuBar sellerMenuBar;
    private RestaurantService restaurantService;

    private UserResponse currentUser;
    private String token;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        restaurantService = new RestaurantService();
        token = PreferencesManager.getJwtToken();
        hideAllDashboards();
        if (defaultView != null) {
            defaultView.setVisible(true);
            defaultView.setManaged(true);
        }

        if (profilePictureImageView != null) {
            final double imageSize = 100.0;
            profilePictureImageView.setFitWidth(imageSize);
            profilePictureImageView.setFitHeight(imageSize);
            profilePictureImageView.setPreserveRatio(true);

            Circle clip = new Circle(imageSize / 2, imageSize / 2, imageSize / 2);
            profilePictureImageView.setClip(clip);

            profilePictureImageView.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
                clip.setCenterX(newBounds.getWidth() / 2.0);
                clip.setCenterY(newBounds.getHeight() / 2.0);
                clip.setRadius(Math.min(newBounds.getWidth(), newBounds.getHeight()) / 2.0);
            });

            profilePictureImageView.setOnMouseClicked(this::handleProfilePictureClick);
            profilePictureImageView.setCursor(Cursor.HAND);
            Tooltip.install(profilePictureImageView, new Tooltip("Click to view profile"));
        }
        super.setProfileImage(profilePictureImageView, null);
    }

    public void setLoggedInUser(UserResponse user) {
        this.currentUser = user;
        if (user != null) {
            if (welcomeUserLabel != null) {
                welcomeUserLabel.setText(user.getName() + " به شومال فود خوش اومدی ");
            }
            displayDashboardForRole(user.getRole());
            super.setProfileImage(profilePictureImageView, user.getProfileImageBase64());
        } else {
            if (welcomeUserLabel != null) {
                welcomeUserLabel.setText("Welcome, Guest!");
            }
            displayDashboardForRole(null);
            super.setProfileImage(profilePictureImageView, null);
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
                if (sellerMenuBar != null)
                {
                    sellerMenuBar.setVisible(true);
                    sellerMenuBar.setManaged(true);
                }
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

    private void handleProfilePictureClick(MouseEvent event) {
        System.out.println("Profile picture clicked! Navigating to user profile.");
        navigateToProfileView(profilePictureImageView);
    }

    private void navigateToProfileView(Node currentNode) {
        navigateTo(
                currentNode,
                "/com/aut/shoomal/views/UserProfileView.fxml",
                "/com/aut/shoomal/styles/MainView.css",
                TransitionType.SLIDE_RIGHT,
                UserProfileController::setLoggedInUser
        );
    }

    public void handleCreateRestaurant(ActionEvent actionEvent)
    {

    }

    public void handleShowRestaurant(ActionEvent actionEvent)
    {

    }

    public void handleAddFoodToRestaurant(ActionEvent actionEvent)
    {

    }

    public void handleEditFood(ActionEvent actionEvent)
    {

    }

    public void handleDeleteFoodFromRestaurant(ActionEvent actionEvent)
    {

    }

    public void handleCreateMenu(ActionEvent actionEvent)
    {

    }

    public void handleShowMenus(ActionEvent actionEvent)
    {

    }

    public void handleManageOrders(ActionEvent actionEvent)
    {

    }
}