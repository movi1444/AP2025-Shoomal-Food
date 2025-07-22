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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class BuyerShowMenuFoodsController extends AbstractBaseController
{
    @FXML private FlowPane foodsContainerFlowPane;
    @FXML private Label menuTitleLabel;
    @FXML private Button backButton;

    private RestaurantService restaurantService;
    private Integer restaurantId;
    private String title, token;
    private boolean ready = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        super.initialize(url, resourceBundle);
        restaurantService = new RestaurantService();
        token = PreferencesManager.getJwtToken();
    }

    private void loadFoods()
    {
        handleError();

        restaurantService.getMenuByTitle(token, restaurantId, title)
                .thenCombine(restaurantService.getFoodsByMenuTitle(token, restaurantId, title), (menu, foods) -> {
                    Platform.runLater(() -> {
                        if (menu != null)
                            menuTitleLabel.setText("عنوان منو:  " + menu.getTitle());
                        else
                            showAlert("Error", "Failed to load menu details.", Alert.AlertType.ERROR, null);
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
                            foodsContainerFlowPane.getChildren().add(new Label("هیچ غذایی در این منو موجود نیست."));
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

    public void setRestaurantId(Integer restaurantId)
    {
        this.restaurantId = restaurantId;
        isReadyToLoad();
    }

    public void setTitle(String title)
    {
        this.title = title;
        isReadyToLoad();
    }

    private void isReadyToLoad()
    {
        if (restaurantId != null && title != null && !title.isEmpty() && !ready)
        {
            ready = true;
            loadFoods();
        }
    }

    @FXML
    public void handleBack(ActionEvent actionEvent)
    {
        navigateTo(
                (Node) actionEvent.getSource(),
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
        if (title == null || title.isEmpty())
        {
            showAlert("Error", "No menu title provided.", Alert.AlertType.ERROR, null);
            return;
        }
        if (restaurantId == null)
            showAlert("Error", "No restaurant ID provided.", Alert.AlertType.ERROR, null);
    }
}