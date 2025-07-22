package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.request.AddFoodItemRequest;
import com.aut.shoomal.dto.response.ListItemResponse;
import com.aut.shoomal.exceptions.FrontendServiceException;
import com.aut.shoomal.service.RestaurantService;
import com.aut.shoomal.utils.PreferencesManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractFoodDetailsController extends AbstractBaseController
{
    @FXML protected Label titleLabel;
    @FXML protected ImageView foodImageView = new ImageView();
    @FXML protected Button uploadImageButton;
    @FXML protected TextField nameField;
    @FXML protected TextField descriptionField;
    @FXML protected TextField priceField;
    @FXML protected TextField supplyField;
    @FXML protected TextArea keywordsField;
    @FXML protected Button saveButton;
    @FXML protected Button backButton;

    protected String imageBase64String;
    protected RestaurantService restaurantService;
    protected String token;
    protected Integer restaurantId;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        super.initialize(url, resourceBundle);
        restaurantService = new RestaurantService();
        token = PreferencesManager.getJwtToken();
        if (token == null || token.isEmpty())
        {
            showAlert("Authentication Error", "User not logged in. Please log in first.", Alert.AlertType.ERROR, null);
            navigateToSignInView(saveButton);
        }

        addTextDirectionListener(nameField);
        addTextDirectionListener(descriptionField);
        addTextDirectionListener(priceField);
        addTextDirectionListener(supplyField);
        addTextAreaDirectionListener(keywordsField);
    }

    public void setRestaurantId(Integer restaurantId)
    {
        this.restaurantId = restaurantId;
    }

    protected abstract CompletableFuture<ListItemResponse> saveFoodDetails(AddFoodItemRequest request);

    @FXML
    public void handleUploadImage(ActionEvent actionEvent)
    {
        this.imageBase64String = handleImageUploadAndConvert(uploadImageButton, foodImageView);
    }

    @FXML
    public void handleSaveChanges(ActionEvent actionEvent)
    {
        if (restaurantId == null)
        {
            showAlert("Error", "Restaurant ID not set. Cannot save food.", Alert.AlertType.ERROR, null);
            return;
        }

        int supply, price;
        try {
            price = Integer.parseInt(priceField.getText());
            supply = Integer.parseInt(supplyField.getText());
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Price and Supply must be valid numbers.", Alert.AlertType.ERROR, null);
            return;
        }

        AddFoodItemRequest request = new AddFoodItemRequest();
        request.setName(nameField.getText());
        request.setDescription(descriptionField.getText());
        request.setPrice(price);
        request.setSupply(supply);
        request.setKeywords(convertToList(keywordsField.getText()));

        saveFoodDetails(request)
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response != null)
                    {
                        showAlert("Success", "Food details saved successfully!", Alert.AlertType.INFORMATION, null);
                        handleBackToPreviousPage(actionEvent);
                    }
                    else
                        showAlert("Error", "Failed to save food details.", Alert.AlertType.ERROR, null);
                }))
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
    public void handleBackToPreviousPage(ActionEvent actionEvent)
    {
        navigateTo(
                (Node) actionEvent.getSource(),
                "/com/aut/shoomal/views/ListFoodView.fxml",
                "/com/aut/shoomal/styles/ListFoodsView.css",
                TransitionType.SLIDE_LEFT,
                controller -> {
                    if (controller instanceof ShowListFoodController showListFoodController)
                        showListFoodController.setRestaurantId(restaurantId);
                }
        );
    }

    private List<String> convertToList(String keywords)
    {
        List<String> list = new ArrayList<>();
        Collections.addAll(list, keywords.split("-"));
        return list;
    }
}