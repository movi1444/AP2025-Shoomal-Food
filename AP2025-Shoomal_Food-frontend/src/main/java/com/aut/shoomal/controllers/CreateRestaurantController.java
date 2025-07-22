package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.request.CreateRestaurantRequest;
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

public class CreateRestaurantController extends AbstractBaseController
{
    @FXML private ImageView restaurantLogoImageView = new ImageView();
    @FXML private Button uploadLogoButton;
    @FXML private Button saveButton;
    @FXML private Button backButton;
    @FXML private TextField nameField;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private TextField taxFeeField;
    @FXML private TextField additionalFeeField;

    private String logoImageBase64;
    private RestaurantService restaurantService;
    private String token;

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
    }

    @FXML
    public void handleUploadLogo(ActionEvent actionEvent)
    {
        this.logoImageBase64 = super.handleImageUploadAndConvert(uploadLogoButton, restaurantLogoImageView);
    }

    @FXML
    public void handleBackToPreviousPage(ActionEvent actionEvent)
    {
        navigateToMainView((Node) actionEvent.getSource());
    }

    @FXML
    public void handleSaveRestaurant(ActionEvent actionEvent)
    {
        if (token == null || token.isEmpty())
        {
            showAlert("Authentication Error", "User not logged in. Please log in first.", Alert.AlertType.ERROR, null);
            navigateTo(saveButton, "/com/aut/shoomal/views/SignInView.fxml", "/com/aut/shoomal/styles/SignInUpStyles.css", TransitionType.SLIDE_LEFT);
            return;
        }
        CreateRestaurantRequest request = getRequest();

        restaurantService.createRestaurant(request, token)
                .thenAccept(restaurant -> {
                    Platform.runLater(() -> {
                        if (restaurant != null)
                        {
                            showAlert("Success", "Restaurant created.", Alert.AlertType.INFORMATION, null);
                            navigateTo(
                                    (Node) actionEvent.getSource(),
                                    "/com/aut/shoomal/views/ShowRestaurantView.fxml",
                                    "/com/aut/shoomal/styles/MainView.css",
                                    TransitionType.SLIDE_LEFT,
                                    ShowRestaurantController::loadInfo
                            );
                        }
                        else
                            showAlert("Error", "Restaurant creation failed.", Alert.AlertType.ERROR, null);
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

    private CreateRestaurantRequest getRequest()
    {
        CreateRestaurantRequest request = new CreateRestaurantRequest();
        if (nameField != null)
            request.setName(nameField.getText());
        if (addressField != null)
            request.setAddress(addressField.getText());
        if (phoneField != null)
            request.setPhone(phoneField.getText());
        if (taxFeeField != null && !taxFeeField.getText().isEmpty())
            request.setTaxFee(Integer.parseInt(taxFeeField.getText()));
        if (additionalFeeField != null && !additionalFeeField.getText().isEmpty())
            request.setAdditionalFee(Integer.parseInt(additionalFeeField.getText()));
        request.setLogoBase64(logoImageBase64);
        return request;
    }
}