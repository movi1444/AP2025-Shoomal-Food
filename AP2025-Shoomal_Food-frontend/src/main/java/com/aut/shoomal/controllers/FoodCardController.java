package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.response.ListItemResponse;
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

public class FoodCardController extends AbstractBaseController
{
    @FXML
    private ImageView foodImageView;
    @FXML private Label nameLabel;
    @FXML private Label priceLabel;
    @FXML private Button addToCartButton;
    @FXML private Button seeCommentsButton;

    private ListItemResponse food;
    private Consumer<Integer> onAddToCart;
    private Consumer<Integer> onSeeComments;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        super.initialize(url, resourceBundle);
    }

    public void setFoodData(ListItemResponse food, Consumer<Integer> onAddToCart, Consumer<Integer> onSeeComments)
    {
        this.food = food;
        this.onAddToCart = onAddToCart;
        this.onSeeComments = onSeeComments;

        if (food != null)
        {
            nameLabel.setText(food.getName());
            priceLabel.setText("قیمت: " + food.getPrice());

            String imageBase64 = food.getImageBase64();
            if (imageBase64 != null && !imageBase64.isEmpty())
            {
                try {
                    super.setProfileImage(foodImageView, imageBase64);
                } catch (Exception e) {
                    System.err.println("Error decoding food image for card: " + e.getMessage());
                    foodImageView.setImage(null);
                }
            }
            else
                foodImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/aut/shoomal/images/ShoomalFoodMahi.png"))));
        }
    }

    @FXML
    private void handleAddToCart(ActionEvent event)
    {
        if (food != null && onAddToCart != null)
            onAddToCart.accept(food.getId());
    }

    @FXML
    private void handleSeeComments(ActionEvent event)
    {
        if (food != null && onSeeComments != null)
            onSeeComments.accept(food.getId());
    }
}