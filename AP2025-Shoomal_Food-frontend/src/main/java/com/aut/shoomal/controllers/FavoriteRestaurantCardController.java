package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.response.RestaurantResponse;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
    @FXML private Button logoButton;
    @FXML private ImageView restaurantLogoView = new ImageView();
    @FXML private Label nameLabel;
    @FXML private Label addressLabel;
    @FXML private Label phoneLabel;

    private RestaurantResponse restaurant;
    private Consumer<Integer> onRestaurantClick;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        super.initialize(url, resourceBundle);
    }

    public void setRestaurantData(RestaurantResponse restaurant, Consumer<Integer> onRestaurantClick)
    {
        this.restaurant = restaurant;
        this.onRestaurantClick = onRestaurantClick;

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
        }
    }

    @FXML
    private void handleLogoClick(ActionEvent event)
    {
        if (restaurant != null && onRestaurantClick != null)
            onRestaurantClick.accept(restaurant.getId());
    }
}