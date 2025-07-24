package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.request.AddMenuTitleRequest;
import com.aut.shoomal.dto.response.MenuTitleResponse;
import com.aut.shoomal.exceptions.FrontendServiceException;
import com.aut.shoomal.service.RestaurantService;
import com.aut.shoomal.utils.PreferencesManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractMenuDetailsController extends AbstractBaseController
{
    @FXML protected TextField titleField;
    @FXML protected Button saveButton;
    @FXML protected Button backButton;
    @FXML protected Label titleLabel;

    protected RestaurantService restaurantService;
    protected String token;
    protected Integer restaurantId;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        super.initialize(url, resourceBundle);
        addTextDirectionListener(titleField);
        restaurantService = new RestaurantService();
        token = PreferencesManager.getJwtToken();
    }

    public void setRestaurantId(Integer restaurantId)
    {
        this.restaurantId = restaurantId;
    }

    protected abstract CompletableFuture<MenuTitleResponse> saveMenuDetails(AddMenuTitleRequest request);

    public void handleSaveChanges(ActionEvent actionEvent)
    {
        if (token == null || token.isEmpty())
        {
            showAlert("Authentication Error", "User not logged in. Please log in first.", Alert.AlertType.ERROR, null);
            navigateToSignInView(saveButton);
            return;
        }

        if (restaurantId == null)
        {
            showAlert("Error", "No restaurant selected.", Alert.AlertType.ERROR, null);
            return;
        }

        AddMenuTitleRequest request = new AddMenuTitleRequest();
        request.setTitle(titleField.getText());

        saveMenuDetails(request)
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response != null)
                    {
                        showAlert("Success", "Menu saved successfully!", Alert.AlertType.INFORMATION, null);
                        handleBackToPreviousPage(actionEvent);
                    }
                    else
                        showAlert("Error", "Failed to save menu details.", Alert.AlertType.ERROR, null);
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

    public void handleBackToPreviousPage(ActionEvent actionEvent)
    {
        navigateTo(
                (Node) actionEvent.getSource(),
                "/com/aut/shoomal/views/ListMenuView.fxml",
                "/com/aut/shoomal/styles/ListFoodsView.css",
                TransitionType.SLIDE_LEFT,
                controller -> {
                    if (controller instanceof ShowListMenuController showListMenuController)
                        showListMenuController.setRestaurantId(restaurantId);
                }
        );
    }
}