package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.response.RestaurantResponse;
import com.aut.shoomal.exceptions.FrontendServiceException;
import com.aut.shoomal.service.BuyerFavoriteService;
import com.aut.shoomal.utils.PreferencesManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class FavoriteRestaurantCardController extends AbstractBaseController
{
    @FXML private Button favoriteButton;
    @FXML private Button logoButton;
    @FXML private ImageView restaurantLogoView = new ImageView();
    @FXML private Label nameLabel;
    @FXML private Label addressLabel;
    @FXML private Label phoneLabel;

    private RestaurantResponse restaurant;
    private Consumer<Integer> onRestaurantClick;
    Consumer<Void> onUpdateCallback;
    private boolean isFavorite;
    private String token;
    private BuyerFavoriteService favoriteService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        super.initialize(url, resourceBundle);
        token = PreferencesManager.getJwtToken();
        favoriteService = new BuyerFavoriteService();
    }

    public void setRestaurantData(RestaurantResponse restaurant, Consumer<Integer> onRestaurantClick, boolean isFavorite, Consumer<Void> onUpdateCallback)
    {
        this.restaurant = restaurant;
        this.onRestaurantClick = onRestaurantClick;
        this.isFavorite = isFavorite;
        this.onUpdateCallback = onUpdateCallback;

        if (restaurant != null)
        {
            nameLabel.setText(restaurant.getName());
            addressLabel.setText(restaurant.getAddress());
            phoneLabel.setText(restaurant.getPhone());

            String logoBase64 = restaurant.getLogoBase64();
            if (logoBase64 != null && !logoBase64.isEmpty())
            {
                try {
                    super.setProfileImage(restaurantLogoView, logoBase64);
                } catch (Exception e) {
                    System.err.println("Error decoding restaurant logo for card: " + e.getMessage());
                    restaurantLogoView.setImage(null);
                }
            }
            else
                restaurantLogoView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/aut/shoomal/images/ShoomalFoodMahi.png"))));

            updateFavoriteButtonState();
        }
    }

    private void updateFavoriteButtonState()
    {
        if (isFavorite)
        {
            favoriteButton.setText("حذف از علاقه مندی");
            favoriteButton.getStyleClass().add("logout-button");
        }
        else
        {
            favoriteButton.setText("افزودن به علاقه مندی");
            favoriteButton.getStyleClass().add("primary-button");
        }
    }

    @FXML
    private void handleLogoClick(ActionEvent event)
    {
        if (restaurant != null && onRestaurantClick != null)
            onRestaurantClick.accept(restaurant.getId());
    }

    @FXML
    public void handleFavoriteAction(ActionEvent actionEvent)
    {
        if (restaurant == null || token == null || token.isEmpty())
        {
            showAlert("Error", "Restaurant or user information is missing.", Alert.AlertType.ERROR, null);
            return;
        }

        if (isFavorite)
        {
            favoriteService.deleteRestaurantFromFavorite(token, restaurant.getId())
                    .thenAccept(response -> Platform.runLater(() -> {
                        if (response.isSuccess())
                        {
                            showAlert("موفقیت", "رستوران با موفقیت از لیست مورد علاقه حذف شد.", Alert.AlertType.INFORMATION, null);
                            onUpdateCallback.accept(null);
                        }
                        else
                            showAlert("خطا", "خطا در حذف از لیست", Alert.AlertType.ERROR, null);
                    }))
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
        else
        {
            favoriteService.addRestaurantToFavorite(token, restaurant.getId())
                    .thenAccept(response -> Platform.runLater(() -> {
                        if (response.isSuccess())
                        {
                            showAlert("موفقیت", "رستوران با موفقیت به لیست مورد علاقه اضافه شد.", Alert.AlertType.INFORMATION, null);
                            onUpdateCallback.accept(null);
                        }
                        else
                            showAlert("خطا", "خطا در افزودن به لیست", Alert.AlertType.ERROR, null);
                    }))
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
    }
}