package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.response.ListItemResponse;
import com.aut.shoomal.dto.response.UserResponse;
import com.aut.shoomal.exceptions.FrontendServiceException;
import com.aut.shoomal.service.RestaurantService;
import com.aut.shoomal.utils.PreferencesManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import com.aut.shoomal.dto.request.AddItemToCartRequest;
import com.aut.shoomal.service.CartService;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class BuyerShowRestaurantDetailsController extends AbstractBaseController
{
    @FXML private ImageView restaurantLogoView;
    @FXML private Label restaurantNameLabel;
    @FXML private Label restaurantAddressLabel;
    @FXML private Button backButton;
    @FXML private Button seeMenusButton;
    @FXML private FlowPane foodsContainerFlowPane;
    @FXML private Hyperlink viewCartButton;
    private UserResponse loggedInUser;

    private RestaurantService restaurantService;
    private CartService cartService;
    private String token;
    private Integer restaurantId;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        super.initialize(url, resourceBundle);
        restaurantService = new RestaurantService();
        cartService = new CartService();
        token = PreferencesManager.getJwtToken();

        if (viewCartButton != null) {
            viewCartButton.setOnAction(this::handleViewCart);
        }
    }

    public void setLoggedInUser(UserResponse loggedInUser){
        this.loggedInUser = loggedInUser;
    }

    public void setRestaurantId(Integer restaurantId)
    {
        this.restaurantId = restaurantId;
        loadRestaurantDetailsAndFoods();
    }

    private void loadRestaurantDetailsAndFoods()
    {
        handleError();

        restaurantService.getRestaurantById(token, restaurantId)
                .thenCombine(restaurantService.getFoodsByRestaurantId(token, restaurantId), (restaurantResponse, foods) -> {
                    Platform.runLater(() -> {
                        if (restaurantResponse != null)
                        {
                            restaurantNameLabel.setText(restaurantResponse.getName());
                            restaurantAddressLabel.setText("آدرس:   " + restaurantResponse.getAddress());

                            String logoBase64 = restaurantResponse.getLogoBase64();
                            if (logoBase64 != null && !logoBase64.isEmpty())
                            {
                                try {
                                    super.setProfileImage(restaurantLogoView, logoBase64);
                                } catch (Exception e) {
                                    System.err.println("Error decoding restaurant logo: " + e.getMessage());
                                    restaurantLogoView.setImage(null);
                                }
                            }
                            else
                                restaurantLogoView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/aut/shoomal/images/ShoomalFoodMahi.png"))));
                        }
                        else
                            showAlert("Error", "Failed to load restaurant details.", Alert.AlertType.ERROR, null);

                        foodsContainerFlowPane.getChildren().clear();
                        int count = 0;
                        if (foods != null && !foods.isEmpty())
                            for (ListItemResponse food : foods)
                            {
                                if (food.getSupply() > 0)
                                {
                                    count++;
                                    try {
                                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aut/shoomal/views/components/FoodCard.fxml"));
                                        VBox foodCard = loader.load();
                                        FoodCardController foodCardController = loader.getController();

                                        foodCardController.setFoodData(food,
                                                this::handleAddToCart,
                                                this::handleSeeComments
                                        );
                                        foodsContainerFlowPane.getChildren().add(foodCard);
                                    } catch (IOException e) {
                                        System.err.println("Failed to load food card: " + e.getMessage());
                                        e.printStackTrace();
                                        showAlert("Load Error", "Could not load a food item card.", Alert.AlertType.ERROR, null);
                                    }
                                }
                            }
                        if (foods == null || foods.isEmpty() || count == 0)
                            foodsContainerFlowPane.getChildren().add(new Label("هیچ غذایی در این رستوران موجود نیست."));
                    });
                    return null;
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        if (e.getCause() instanceof FrontendServiceException fsException) {
                            showAlert(fsException);
                        } else {
                            showAlert("Unexpected Error", "An unexpected error occurred: " + e.getMessage(), Alert.AlertType.ERROR, null);
                        }
                    });
                    return null;
                });
    }

    public void handleAddToCart(Long foodItemId, Integer quantity) {
        if (token == null || token.isEmpty()) {
            showAlert("Authentication Error", "User not logged in. Please log in first.", Alert.AlertType.ERROR, null);
            return;
        }
        if (this.restaurantId == null) {
            showAlert("Error", "Restaurant information not loaded. Cannot add to cart.", Alert.AlertType.ERROR, null);
            return;
        }

        AddItemToCartRequest request = new AddItemToCartRequest();
        request.setRestaurantId(this.restaurantId.longValue());
        request.setFoodItemId(foodItemId);
        request.setQuantity(quantity);

        cartService.addItemToCart(request, token)
                .thenAccept(cartResponse -> Platform.runLater(() -> showAlert("موفقیت", "آیتم به سبد خرید اضافه شد! مجموع: " + cartResponse.getTotalPrice() + " تومان", Alert.AlertType.INFORMATION, null)))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        if (e.getCause() instanceof FrontendServiceException exception) {
                            showAlert(exception);
                        } else {
                            showAlert("خطا", "افزودن آیتم به سبد خرید با شکست مواجه شد: " + e.getMessage(), Alert.AlertType.ERROR, null);
                        }
                    });
                    return null;
                });
    }

    @FXML
    public void handleViewCart(ActionEvent event) {
        if (token == null || token.isEmpty()) {
            showAlert("Authentication Error", "User not logged in. Please log in first.", Alert.AlertType.ERROR, null);
            return;
        }
        if (this.restaurantId == null) {
            showAlert("Error", "Restaurant information not loaded. Cannot view cart.", Alert.AlertType.ERROR, null);
            return;
        }

        navigateTo(
                (Node) event.getSource(),
                "/com/aut/shoomal/views/CartView.fxml",
                "/com/aut/shoomal/styles/AdminDashboardStyles.css",
                TransitionType.SLIDE_RIGHT,
                controller -> {
                    if (controller instanceof CartController cartController) {
                        cartController.setRestaurantId(this.restaurantId);
                        cartController.setUserId(Objects.requireNonNull(PreferencesManager.getUserData()).getId());
                    }
                }
        );
    }

    @FXML
    public void handleBack(ActionEvent event)
    {
        navigateToMainView((Node) event.getSource());
    }

    @FXML
    public void handleSeeAllMenus(ActionEvent event)
    {
        navigateTo(
                (Node) event.getSource(),
                "/com/aut/shoomal/views/BuyerRestaurantMenuView.fxml",
                "/com/aut/shoomal/styles/MainView.css",
                TransitionType.SLIDE_LEFT,
                controller -> {
                    if (controller instanceof BuyerShowListMenuController menuController)
                        menuController.setRestaurantId(restaurantId);
                }
        );
    }

    private void handleError() {
        if (token == null || token.isEmpty()) {
            showAlert("Authentication Error", "User not logged in. Please log in first.", Alert.AlertType.ERROR, null);
            return;
        }
        if (restaurantId == null)
            showAlert("Error", "No restaurant ID provided.", Alert.AlertType.ERROR, null);
    }
}
