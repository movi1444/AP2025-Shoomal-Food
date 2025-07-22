package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.response.ListItemResponse;
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
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

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

    private RestaurantService restaurantService;
    private String token;
    private Integer restaurantId;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        super.initialize(url, resourceBundle);
        restaurantService = new RestaurantService();
        token = PreferencesManager.getJwtToken();
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
                        if (foods != null && !foods.isEmpty())
                            for (ListItemResponse food : foods)
                            {
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
                        else
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

    private void handleError()
    {
        if (token == null || token.isEmpty())
        {
            showAlert("Authentication Error", "User not logged in. Please log in first.", Alert.AlertType.ERROR, null);
            return;
        }
        if (restaurantId == null)
            showAlert("Error", "No restaurant ID provided.", Alert.AlertType.ERROR, null);
    }
}