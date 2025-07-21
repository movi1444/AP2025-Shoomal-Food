package com.aut.shoomal.controllers;

import com.aut.shoomal.exceptions.FrontendServiceException;
import com.aut.shoomal.service.RestaurantService;
import javafx.application.Platform;
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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert.AlertType;
import java.io.IOException;
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

    @FXML private MenuBar sellerMenuBar;
    private RestaurantService restaurantService;
    private Integer restaurantId;

    @FXML private MenuBar courierMenuBar;

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
            final double imageSize = 110.0;
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
        if (sellerMenuBar != null) {
            sellerMenuBar.setVisible(false);
            sellerMenuBar.setManaged(false);
        }
        if (courierMenuBar != null) {
            courierMenuBar.setVisible(false);
            courierMenuBar.setManaged(false);
        }
    }

    private void displayDashboardForRole(String role) {
        hideAllDashboards();

        ScrollPane targetPane = null;
        switch (role != null ? role.toLowerCase() : "") {
            case "buyer":
                targetPane = buyerDashboardScrollPane;
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aut/shoomal/views/BuyerDashboardContent.fxml"));
                    Parent buyerContentRoot = loader.load();

                    BuyerDashboardContentController buyerController = loader.getController();
                     if (buyerController != null) {buyerController.setLoggedInUser(this.currentUser);
                     }
                    buyerDashboardScrollPane.setContent(buyerContentRoot);
                } catch (IOException e) {
                    System.err.println("Failed to load BuyerDashboardContent.fxml: " + e.getMessage());
                    e.printStackTrace();
                    showAlert("Error", "Failed to load buyer dashboard content.", AlertType.ERROR, null);
                }
                break;
            case "seller":
                targetPane = sellerDashboardScrollPane;
                if (sellerMenuBar != null) {
                    sellerMenuBar.setVisible(true);
                    sellerMenuBar.setManaged(true);
                }
                loadSellerRestaurantId();
                break;
            case "courier":
                targetPane = courierDashboardScrollPane;
                if (courierMenuBar != null) {
                    courierMenuBar.setVisible(true);
                    courierMenuBar.setManaged(true);
                }
                break;
            case "admin":
                targetPane = adminDashboardScrollPane;
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aut/shoomal/views/AdminDashboardContent.fxml"));
                    Parent adminContentRoot = loader.load();

                    AdminDashboardContentController adminController = loader.getController();
                    if (adminController != null) {
                        adminController.setLoggedInUser(this.currentUser);
                    }
                    adminDashboardScrollPane.setContent(adminContentRoot);
                } catch (IOException e) {
                    System.err.println("Failed to load AdminDashboardContent.fxml: " + e.getMessage());
                    e.printStackTrace();
                    showAlert("Error", "Failed to load admin dashboard content.", AlertType.ERROR, null);
                }
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

    private void loadSellerRestaurantId()
    {
        if (token == null || token.isEmpty())
        {
            showAlert("Authentication Error", "User not logged in. Please log in first.", Alert.AlertType.ERROR, null);
            return;
        }

        restaurantService.getRestaurants(token)
                .thenAccept(restaurants -> Platform.runLater(() -> {
                    if (restaurants != null && !restaurants.isEmpty())
                        this.restaurantId = restaurants.getFirst().getId();
                    else
                        restaurantId = null;
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        if (e.getCause() instanceof FrontendServiceException fsException) {
                            showAlert(fsException);
                        } else {
                            showAlert("Error", "Failed to load seller's restaurant ID: " + e.getMessage(), Alert.AlertType.ERROR, null);
                        }
                    });
                    restaurantId = null;
                    return null;
                });
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

    @FXML
    public void handleCreateRestaurant(ActionEvent actionEvent)
    {
        navigateTo(
                (MenuItem) actionEvent.getSource(),
                "/com/aut/shoomal/views/CreateRestaurantView.fxml",
                "/com/aut/shoomal/styles/MainView.css",
                TransitionType.SLIDE_LEFT
        );
    }

    @FXML
    public void handleShowRestaurant(ActionEvent actionEvent)
    {
        navigateTo(
                (MenuItem) actionEvent.getSource(),
                "/com/aut/shoomal/views/ShowRestaurantView.fxml",
                "/com/aut/shoomal/styles/MainView.css",
                TransitionType.SLIDE_LEFT
        );

    }

    @FXML
    public void handleListFoods(ActionEvent actionEvent)
    {
        if (restaurantId == null)
        {
            showAlert("Error", "Please create a restaurant first.", Alert.AlertType.WARNING, null);
            return;
        }

        navigateTo(
                (MenuItem) actionEvent.getSource(),
                "/com/aut/shoomal/views/ListFoodView.fxml",
                "",
                TransitionType.SLIDE_LEFT,
                controller -> {
                    if (controller instanceof ShowListFoodController showListFoodController)
                        showListFoodController.setRestaurantId(restaurantId);
                }
        );
    }

    @FXML
    public void handleManageMenus(ActionEvent actionEvent)
    {
        handleError();
        navigateTo(
                (MenuItem) actionEvent.getSource(),
                "/com/aut/shoomal/views/ListMenuView.fxml",
                "",
                TransitionType.SLIDE_LEFT,
                controller -> {
                    if (controller instanceof ShowListMenuController showListMenuController)
                        showListMenuController.setRestaurantId(restaurantId);
                }
        );
    }

    @FXML
    public void handleManageOrders(ActionEvent actionEvent)
    {
        handleError();
        navigateTo(
                (MenuItem) actionEvent.getSource(),
                "/com/aut/shoomal/views/ListOrderView.fxml",
                "",
                TransitionType.SLIDE_LEFT,
                controller -> {
                    if (controller instanceof ShowListOrderController showListOrderController)
                        showListOrderController.setRestaurantId(restaurantId);
                }
        );
    }

    @FXML
    public void handleShowAvailableOrders(ActionEvent actionEvent)
    {
        navigateTo(
                (MenuItem) actionEvent.getSource(),
                "/com/aut/shoomal/views/CourierAvailableView.fxml",
                "/com/aut/shoomal/styles/ListFoodsView.css",
                TransitionType.SLIDE_LEFT,
                controller -> {
                    if (controller instanceof CourierAvailableController courierAvailableController)
                        courierAvailableController.loadAvailableOrders();
                }
        );
    }

    @FXML
    public void handleDeliveryHistory(ActionEvent actionEvent)
    {
        navigateTo(
                (MenuItem) actionEvent.getSource(),
                "/com/aut/shoomal/views/CourierDeliveryHistoryView.fxml",
                "/com/aut/shoomal/styles/ListFoodsView.css",
                TransitionType.SLIDE_LEFT,
                controller -> {
                    if (controller instanceof CourierDeliveryHistoryController historyController)
                        historyController.loadDeliveryHistory(null, null, null);
                }
        );
    }

    private void handleError() {
        if (token == null || token.isEmpty())
        {
            showAlert("Authentication Error", "User not logged in. Please log in first.", AlertType.ERROR, null);
            return;
        }

        if (restaurantId == null)
            showAlert("Error", "Please create a restaurant first.", AlertType.WARNING, null);
    }
}