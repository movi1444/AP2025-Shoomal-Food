package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.response.RestaurantResponse;
import com.aut.shoomal.exceptions.FrontendServiceException;
import com.aut.shoomal.service.RestaurantService;
import com.aut.shoomal.utils.PreferencesManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
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
    private Integer restaurantId;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        super.initialize(url, resourceBundle);
        restaurantService = new RestaurantService();
        token = PreferencesManager.getJwtToken();
        loadInfo();
    }

    public void loadInfo()
    {
        if (token == null || token.isEmpty())
        {
            showAlert("Authentication Error", "User not logged in. Please log in first.", Alert.AlertType.ERROR, null);
            navigateTo(updateRestaurantLink, "/com/aut/shoomal/views/SignInView.fxml", "/com/aut/shoomal/styles/SignInUpStyles.css", TransitionType.SLIDE_LEFT);
            return;
        }

        restaurantService.getRestaurants(token)
                .thenAccept(responses -> {
                    Platform.runLater(() -> {
                        RestaurantResponse restaurant ;
                        if (responses != null && !responses.isEmpty())
                        {
                            restaurant = responses.getFirst();
                            restaurantId = restaurant.getId();
                            nameLabel.setText("نام: " + restaurant.getName());
                            addressLabel.setText("آدرس: " + restaurant.getAddress());
                            phoneLabel.setText("شماره رستوران: " + restaurant.getPhone());
                            taxFeeLabel.setText("هزینه بسته بندی: " + restaurant.getTaxFee());
                            additionalFeeLabel.setText("هزینه اضافی: " + restaurant.getAdditionalFee());
                        }
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

    @FXML
    public void handleUpdateRestaurant(ActionEvent actionEvent)
    {
        if (restaurantId == null)
        {
            showAlert("Error", "No restaurant found to update.", Alert.AlertType.ERROR, null);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/com/aut/shoomal/views/UpdateRestaurantView.fxml")));
            Parent root = loader.load();

            UpdateRestaurantController updateController = loader.getController();
            if (updateController != null)
                updateController.setRestaurantId(restaurantId);

            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(scene);
            stage.setTitle("Update Restaurant");
            stage.show();

        } catch (IOException e) {
            System.err.println("Failed to load UpdateRestaurantView.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert("Navigation Error", "Failed to load restaurant update page.", Alert.AlertType.ERROR, null);
        }
    }
}