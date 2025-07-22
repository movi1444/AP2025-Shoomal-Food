package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.request.AddFoodItemRequest;
import com.aut.shoomal.dto.request.UpdateFoodItemRequest;
import com.aut.shoomal.dto.response.ListItemResponse;
import com.aut.shoomal.exceptions.FrontendServiceException;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class EditFoodController extends AbstractFoodDetailsController
{
    private Integer foodId;
    private boolean idsSetAndReady = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        super.initialize(url, resourceBundle);
        titleLabel.setText("تغییر اطلاعات غذا");
    }

    @Override
    public void setRestaurantId(Integer restaurantId)
    {
        super.setRestaurantId(restaurantId);
        checkAndLoadFoodDetails();
    }

    public void setFoodId(Integer foodId)
    {
        this.foodId = foodId;
        checkAndLoadFoodDetails();
    }

    private void checkAndLoadFoodDetails()
    {
        if (this.restaurantId != null && this.foodId != null && !idsSetAndReady)
        {
            idsSetAndReady = true;
            loadFoodDetails();
        }
    }

    private void loadFoodDetails()
    {
        if (restaurantId == null || foodId == null)
        {
            showAlert("Error", "Restaurant ID or Food ID not set. Cannot load food details.", Alert.AlertType.ERROR, null);
            return;
        }
        if (token == null || token.isEmpty())
        {
            showAlert("Authentication Error", "User not logged in. Please log in first.", Alert.AlertType.ERROR, null);
            navigateToSignInView(saveButton);
            return;
        }

        restaurantService.getFoodById(token, foodId)
                .thenAccept(food -> Platform.runLater(() -> {
                    if (food != null)
                    {
                        nameField.setText(food.getName());
                        descriptionField.setText(food.getDescription());
                        priceField.setText(String.valueOf(food.getPrice()));
                        supplyField.setText(String.valueOf(food.getSupply()));
                        keywordsField.setText(convertToTextField(food.getKeywords()));
                    }
                    else
                        showAlert("Error", "Failed to load food details.", Alert.AlertType.ERROR, null);
                }))
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

    @Override
    protected CompletableFuture<ListItemResponse> saveFoodDetails(AddFoodItemRequest request)
    {
        UpdateFoodItemRequest updateRequest = new UpdateFoodItemRequest();
        updateRequest.setName(request.getName());
        updateRequest.setDescription(request.getDescription());
        updateRequest.setPrice(request.getPrice());
        updateRequest.setSupply(request.getSupply());
        updateRequest.setKeywords(request.getKeywords());
        updateRequest.setImageBase64(request.getImageBase64());

        return restaurantService.editFood(updateRequest, token, foodId);
    }

    private String convertToTextField(List<String> keywords)
    {
        StringBuilder result = new StringBuilder();
        for (String keyword : keywords)
        {
            result.append(keyword);
            if (!Objects.equals(keyword, keywords.getLast()))
                result.append("-");
        }
        return result.toString();
    }
}