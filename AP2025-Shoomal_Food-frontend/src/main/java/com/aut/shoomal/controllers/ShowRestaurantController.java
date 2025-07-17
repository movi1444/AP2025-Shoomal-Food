package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.response.RestaurantResponse;
import com.aut.shoomal.exceptions.FrontendServiceException;
import com.aut.shoomal.service.RestaurantService;
import com.aut.shoomal.utils.PreferencesManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

public class ShowRestaurantController extends AbstractBaseController
{
    @FXML private Label nameLabel;
    @FXML private Label addressLabel;
    @FXML private Label phoneLabel;
    @FXML private Label taxFeeLabel;
    @FXML private Label additionalFeeLabel;
    @FXML private Hyperlink updateRestaurantLink;
    @FXML private ImageView restaurantLogoView;

    private RestaurantService restaurantService;
    private String token;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        super.initialize(url, resourceBundle);
        restaurantService = new RestaurantService();
        token = PreferencesManager.getJwtToken();
        loadInfo();
    }

    private void loadInfo()
    {
        if (token == null || token.isEmpty())
        {
            showAlert("Authentication Error", "User not logged in. Please log in first.", Alert.AlertType.ERROR, null);
            navigateTo(updateRestaurantLink, "/com/aut/shoomal/views/SignInView.fxml", "/com/aut/shoomal/styles/SignInUpStyles.css", TransitionType.SLIDE_LEFT);
            return;
        }

        restaurantService.getRestaurants(token)
                .thenAccept(responses -> {
                    RestaurantResponse restaurant = responses.getFirst();
                    Platform.runLater(() -> {
                        nameLabel.setText("نام: " + restaurant.getName());
                        addressLabel.setText("آدرس: " + restaurant.getAddress());
                        phoneLabel.setText("شماره رستوران: " + restaurant.getPhone());
                        taxFeeLabel.setText("هزینه بسته بندی: " + restaurant.getTaxFee());
                        additionalFeeLabel.setText("هزینه اضافی: " + restaurant.getAdditionalFee());
                    });
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                       if (e.getCause() instanceof FrontendServiceException exception)
                           showAlert(exception);
                       else
                           showAlert("Unexpected Error", "An unexpected error occurred: " + e.getMessage(), Alert.AlertType.ERROR, null);
                    });
                    return null;
                });
    }

    @FXML
    private void handleBackToMain(ActionEvent event)
    {
        navigateTo(
                (Node) event.getSource(),
                "/com/aut/shoomal/views/MainView.fxml",
                "/com/aut/shoomal/styles/MainView.css",
                TransitionType.SLIDE_LEFT,
                (MainController controller) -> {
                    controller.setLoggedInUser(PreferencesManager.getUserData());
                }
        );
    }

    public void handleUpdateRestaurant(ActionEvent actionEvent)
    {

    }
}