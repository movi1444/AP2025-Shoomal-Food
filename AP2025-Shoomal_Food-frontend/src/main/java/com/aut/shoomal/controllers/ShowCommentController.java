package com.aut.shoomal.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public abstract class ShowCommentController extends AbstractBaseController
{
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        super.initialize(url, resourceBundle);
    }

    protected void handleSeeComments(Integer foodId)
    {
        if (foodId == null) {
            showAlert("Error", "Food ID is missing for comments.", Alert.AlertType.ERROR, null);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aut/shoomal/views/FoodCommentsView.fxml"));
            Parent root = loader.load();

            FoodCommentsController foodCommentsController = loader.getController();
            if (foodCommentsController != null)
                foodCommentsController.setFoodId(foodId);

            Stage commentsStage = new Stage();
            commentsStage.initModality(Modality.APPLICATION_MODAL);
            commentsStage.setTitle("نظرات غذا");
            commentsStage.setScene(new Scene(root));
            commentsStage.setResizable(true);
            commentsStage.showAndWait();
        } catch (IOException e) {
            System.err.println("Failed to load FoodCommentsView.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert("Navigation Error", "Failed to load food comments page.", Alert.AlertType.ERROR, null);
        }
    }
}