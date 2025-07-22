package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.response.RatingResponse;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;

import java.net.URL;
import java.util.ResourceBundle;

public class CommentCardController extends AbstractBaseController
{
    @FXML private Label userIdLabel;
    @FXML private Label ratingNumberLabel;
    @FXML private Label commentTextLabel;
    @FXML private FlowPane commentImagesFlowPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        super.initialize(url, resourceBundle);
    }

    public void setCommentData(RatingResponse rating)
    {
        if (rating != null)
        {
            userIdLabel.setText("شناسه کاربر:  " + rating.getUserId());
            ratingNumberLabel.setText(String.valueOf(rating.getRating()));
            commentTextLabel.setText("متن:  " + rating.getComment());
            commentImagesFlowPane.getChildren().clear();

            if (rating.getImageBase64() != null && !rating.getImageBase64().isEmpty())
                for (String imageBase64 : rating.getImageBase64())
                {
                    try {
                        ImageView imageView = new ImageView();
                        imageView.setFitWidth(400);
                        imageView.setFitHeight(400);
                        imageView.setPreserveRatio(true);
                        super.setProfileImage(imageView, imageBase64);
                        imageView.getStyleClass().add("comment-image-view");
                        commentImagesFlowPane.getChildren().add(imageView);
                    } catch (Exception e) {
                        System.err.println("Error decoding comment image: " + e.getMessage());
                    }
                }
        }
    }
}