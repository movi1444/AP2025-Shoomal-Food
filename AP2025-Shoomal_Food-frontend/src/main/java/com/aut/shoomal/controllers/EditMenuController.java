package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.request.AddMenuTitleRequest;
import com.aut.shoomal.dto.response.MenuTitleResponse;
import com.aut.shoomal.exceptions.FrontendServiceException;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class EditMenuController extends AbstractMenuDetailsController
{
    private String title;
    private boolean idsSetAndReady = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        super.initialize(url, resourceBundle);
        titleLabel.setText("تغییر اطلاعات منو");
    }

    @Override
    protected CompletableFuture<MenuTitleResponse> saveMenuDetails(AddMenuTitleRequest request)
    {
        return restaurantService.editMenu(request, token, restaurantId, title);
    }

    @Override
    public void setRestaurantId(Integer restaurantId)
    {
        super.setRestaurantId(restaurantId);
        checkAndLoadMenuDetails();
    }

    public void setTitle(String title)
    {
        this.title = title;
        checkAndLoadMenuDetails();
    }

    private void checkAndLoadMenuDetails()
    {
        if (this.restaurantId != null && this.title != null && !this.title.isEmpty() && !idsSetAndReady)
        {
            idsSetAndReady = true;
            loadMenuDetails();
        }
    }

    private void loadMenuDetails()
    {
        if (restaurantId == null || title == null || title.isEmpty())
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

        restaurantService.getMenuByTitle(token, restaurantId, title)
                .thenAccept(menu -> Platform.runLater(() -> {
                    if (menu != null)
                        titleField.setText(menu.getTitle());
                    else
                        showAlert("Error", "Failed to load menu details.", Alert.AlertType.ERROR, null);
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
}