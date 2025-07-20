package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.request.AddMenuTitleRequest;
import com.aut.shoomal.dto.response.MenuTitleResponse;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class AddMenuController extends AbstractMenuDetailsController
{
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        super.initialize(url, resourceBundle);
        titleLabel.setText("ایجاد منوی جدید");
    }

    @Override
    protected CompletableFuture<MenuTitleResponse> saveMenuDetails(AddMenuTitleRequest request)
    {
        return restaurantService.addMenu(request, token, restaurantId);
    }
}