package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.request.AddFoodItemRequest;
import com.aut.shoomal.dto.response.ListItemResponse;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class AddFoodController extends AbstractFoodDetailsController
{
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        super.initialize(url, resourceBundle);
        titleLabel.setText("ایجاد غذای جدید");
    }

    @Override
    protected CompletableFuture<ListItemResponse> saveFoodDetails(AddFoodItemRequest request)
    {
        return restaurantService.addFoodToRestaurant(request, token, restaurantId);
    }
}