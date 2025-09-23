package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.request.SubmitRatingRequest;
import com.aut.shoomal.dto.request.UpdateRatingRequest;
import com.aut.shoomal.dto.response.RatingResponse;
import com.aut.shoomal.exceptions.FrontendServiceException;
import com.aut.shoomal.service.BuyerRatingService;
import com.aut.shoomal.utils.PreferencesManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;

public class RatingController extends AbstractBaseController
{
    @FXML private TextArea commentTextArea;
    @FXML private ChoiceBox<String> ratingChoiceBox;
    @FXML private Button uploadImageButton;
    @FXML private FlowPane imagesFlowPane;
    @FXML private Button submitButton;
    @FXML private Button cancelButton;

    private Integer orderId;
    private String token, mode;
    private BuyerRatingService buyerRatingService;
    private final List<String> uploadedImageBase64s = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        super.initialize(url, resourceBundle);
        token = PreferencesManager.getJwtToken();
        buyerRatingService = new BuyerRatingService();
        addTextAreaDirectionListener(commentTextArea);
        addChoiceBoxDirectionListener(ratingChoiceBox);
        ratingChoiceBox.setItems(FXCollections.observableArrayList("1", "2", "3", "4", "5"));
        ratingChoiceBox.getSelectionModel().selectFirst();
    }

    public void setRatingContext(Integer orderId, String mode)
    {
        this.orderId = orderId;
        this.mode = mode;

        if (mode.equalsIgnoreCase("ADD"))
            clearForm();
        else if (mode.equalsIgnoreCase("UPDATE"))
            loadData();
    }

    private void loadData()
    {
        if (token == null || token.isEmpty())
        {
            showAlert("Authentication Error", "User not logged in.", Alert.AlertType.ERROR, null);
            navigateToSignInView(cancelButton);
            return;
        }

        buyerRatingService.getRatingsByOrderId(token, orderId)
                .thenAccept(ratings -> Platform.runLater(() -> {
                    if (ratings != null && !ratings.isEmpty())
                    {
                        commentTextArea.setText(ratings.getFirst().getComment());
                        ratingChoiceBox.getSelectionModel().select(String.valueOf(ratings.getFirst().getRating()));
                        uploadedImageBase64s.clear();
                        for (RatingResponse rating : ratings)
                            if (rating.getImageBase64() != null)
                            {
                                uploadedImageBase64s.addAll(rating.getImageBase64());
                                displayImages();
                            }
                    }
                    else
                    {
                        showAlert("خطا", "هیچ نظری برای این سفارش پیدا نشد", Alert.AlertType.WARNING, null);
                        closePage(cancelButton);
                    }
                }))
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
    private void handleUploadImage(ActionEvent event)
    {
        String base64Image = handleImageUploadAndConvert(uploadImageButton, null);

        if (base64Image != null && !base64Image.isEmpty())
        {
            uploadedImageBase64s.add(base64Image);
            displayImages();
        }
    }

    private void displayImages()
    {
        imagesFlowPane.getChildren().clear();

        Set<String> distinctImageBase64s = new HashSet<>(uploadedImageBase64s);
        uploadedImageBase64s.clear();
        uploadedImageBase64s.addAll(distinctImageBase64s);
        for (int i = 0; i < uploadedImageBase64s.size(); i++)
        {
            String base64 = uploadedImageBase64s.get(i);
            try {
                ImageView imageView = new ImageView();
                imageView.setFitWidth(400);
                imageView.setFitHeight(400);
                imageView.setPreserveRatio(true);
                super.setProfileImage(imageView, base64);

                Button removeButton = new Button("X");
                removeButton.getStyleClass().add("remove-image-button");
                final int indexToRemove = i;
                removeButton.setOnAction(e -> {
                    uploadedImageBase64s.remove(indexToRemove);
                    displayImages();
                });

                StackPane imageStack = new StackPane(imageView, removeButton);
                StackPane.setAlignment(removeButton, Pos.TOP_RIGHT);
                imagesFlowPane.getChildren().add(imageStack);
            } catch (Exception e) {
                System.err.println("Error displaying image: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void clearForm()
    {
        commentTextArea.clear();
        ratingChoiceBox.getSelectionModel().selectFirst();
        uploadedImageBase64s.clear();
        imagesFlowPane.getChildren().clear();
    }

    private void closePage(Button button)
    {
        Stage stage = (Stage) button.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void handleCancel(ActionEvent event)
    {
        closePage(cancelButton);
    }

    @FXML
    public void handleSubmitRating(ActionEvent actionEvent)
    {
        if (token == null || token.isEmpty())
        {
            showAlert("Authentication Error", "User not logged in.", Alert.AlertType.ERROR, null);
            navigateToSignInView(cancelButton);
            return;
        }

        String comment = commentTextArea.getText().trim();
        Integer ratingNumber = Integer.parseInt(ratingChoiceBox.getValue());
        if (comment.isEmpty())
        {
            showAlert("خطا", "لطفا نظری برای امتیاز خود بنویسید.", Alert.AlertType.ERROR, null);
            return;
        }

        if (mode.equalsIgnoreCase("ADD"))
        {
            SubmitRatingRequest request = new SubmitRatingRequest(orderId, ratingNumber, comment, uploadedImageBase64s);
            buyerRatingService.submitRating(token, request)
                    .thenAccept(response -> Platform.runLater(() -> {
                        if (response.isSuccess())
                        {
                            showAlert("موفقیت", "نظر شما با موفقیت ثبت شد.", Alert.AlertType.INFORMATION, null);
                            closePage(submitButton);
                        }
                        else
                            showAlert("خطا", "در ثبت نظر خطایی رخ داد", Alert.AlertType.ERROR, null);
                    }))
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
        else if (mode.equalsIgnoreCase("UPDATE"))
        {
            UpdateRatingRequest request = new UpdateRatingRequest(ratingNumber, comment, uploadedImageBase64s);
            buyerRatingService.updateRating(token, orderId, request)
                    .thenAccept(response -> Platform.runLater(() -> {
                        if (response != null)
                        {
                            showAlert("موفقیت", "نظر شما با موفقیت تغییر کرد.", Alert.AlertType.INFORMATION, null);
                            closePage(submitButton);
                        }
                        else
                            showAlert("خطا", "خطا در بروزرسانی نظر", Alert.AlertType.ERROR, null);
                    }))
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
        else
            showAlert("خطا", "حالت نامعتبر برای ثبت امتیاز.", Alert.AlertType.ERROR, null);
        closePage(submitButton);
    }
}