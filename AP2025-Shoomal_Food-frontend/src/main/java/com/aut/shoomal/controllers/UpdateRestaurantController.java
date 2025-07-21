package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.request.UpdateRestaurantRequest;
import com.aut.shoomal.dto.response.RestaurantResponse;
import com.aut.shoomal.exceptions.FrontendServiceException;
import com.aut.shoomal.service.RestaurantService;
import com.aut.shoomal.utils.PreferencesManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

public class UpdateRestaurantController extends AbstractBaseController
{
    @FXML private ImageView restaurantLogoImageView;
    @FXML private Button uploadLogoButton;
    @FXML private Button saveButton;
    @FXML private Button backButton;
    @FXML private TextField nameField;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private TextField taxFeeField;
    @FXML private TextField additionalFeeField;

    private String logoImageBase64String;
    private RestaurantService restaurantService;
    private String token;
    private Integer restaurantId;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        super.initialize(url, resourceBundle);
        restaurantService = new RestaurantService();
        token = PreferencesManager.getJwtToken();
        addTextDirectionListener(nameField);
        addTextDirectionListener(addressField);
        addTextDirectionListener(phoneField);
        addTextDirectionListener(taxFeeField);
        addTextDirectionListener(additionalFeeField);
        loadRestaurantInfo();
    }

    public void setRestaurantId(Integer restaurantId)
    {
        this.restaurantId = restaurantId;
    }

    private void loadRestaurantInfo()
    {
        if (token == null || token.isEmpty())
        {
            showAlert("Authentication Error", "User not logged in. Please log in first.", Alert.AlertType.ERROR, null);
            navigateTo(saveButton, "/com/aut/shoomal/views/SignInView.fxml", "/com/aut/shoomal/styles/SignInUpStyles.css", TransitionType.SLIDE_LEFT);
            return;
        }

        restaurantService.getRestaurants(token)
                .thenAccept(responses -> {
                    Platform.runLater(() -> {
                        if (responses != null && !responses.isEmpty())
                        {
                            RestaurantResponse restaurant = responses.getFirst();
                            if (nameField != null)
                                nameField.setText(restaurant.getName());
                            if (addressField != null)
                                addressField.setText(restaurant.getAddress());
                            if (phoneField != null)
                                phoneField.setText(restaurant.getPhone());
                            if (taxFeeField != null)
                                taxFeeField.setText(String.valueOf(restaurant.getTaxFee()));
                            if (additionalFeeField != null)
                                additionalFeeField.setText(String.valueOf(restaurant.getAdditionalFee()));
                            super.setProfileImage(restaurantLogoImageView, restaurant.getLogoBase64());
                        }
                    });
                })
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

    @FXML
    public void handleUploadLogo(ActionEvent actionEvent)
    {
        this.logoImageBase64String = super.handleImageUploadAndConvert(uploadLogoButton, restaurantLogoImageView);
    }

    @FXML
    public void handleSaveRestaurant(ActionEvent actionEvent)
    {
        UpdateRestaurantRequest request = getUpdateRestaurantRequest();
        restaurantService.updateRestaurant(request, token, restaurantId)
                .thenAccept(restaurant -> {
                    Platform.runLater(() -> {
                        if (restaurant != null)
                        {
                            showAlert("Success", "Restaurant updated successfully.", Alert.AlertType.INFORMATION, null);
                            handleBackToPreviousPage(actionEvent);
                        }
                        else
                            showAlert("Error", "Failed to update restaurant.", Alert.AlertType.ERROR, null);
                    });
                })
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

    private UpdateRestaurantRequest getUpdateRestaurantRequest()
    {
        UpdateRestaurantRequest request = new UpdateRestaurantRequest();
        if (nameField != null)
            request.setName(nameField.getText());
        if (addressField != null)
            request.setAddress(addressField.getText());
        if (phoneField != null)
            request.setPhone(phoneField.getText());
        if (taxFeeField != null)
            request.setTaxFee(Integer.parseInt(taxFeeField.getText()));
        if (additionalFeeField != null)
            request.setAdditionalFee(Integer.parseInt(additionalFeeField.getText()));
        request.setLogoBase64(logoImageBase64String);
        return request;
    }

    @FXML
    public void handleBackToPreviousPage(ActionEvent actionEvent)
    {
        navigateTo(
                (Node) actionEvent.getSource(),
                "/com/aut/shoomal/views/ShowRestaurantView.fxml",
                "/com/aut/shoomal/styles/MainView.css",
                TransitionType.SLIDE_LEFT,
                ShowRestaurantController::loadInfo
        );
    }
}