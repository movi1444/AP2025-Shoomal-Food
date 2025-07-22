package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.response.ItemRatingResponse;
import com.aut.shoomal.dto.response.ListItemResponse;
import com.aut.shoomal.dto.response.RatingResponse;
import com.aut.shoomal.exceptions.FrontendServiceException;
import com.aut.shoomal.service.BuyerRatingService;
import com.aut.shoomal.service.RestaurantService;
import com.aut.shoomal.utils.PreferencesManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class FoodCommentsController extends AbstractBaseController
{
    @FXML private ImageView foodLogoView = new ImageView();
    @FXML private Label foodNameLabel;
    @FXML private Label averageRatingLabel;
    @FXML private FlowPane commentsContainerVBox;
    @FXML private Button backButton;

    private Integer foodId;
    private String token;
    private BuyerRatingService buyerRatingService;
    private RestaurantService restaurantService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        super.initialize(url, resourceBundle);
        token = PreferencesManager.getJwtToken();
        buyerRatingService = new BuyerRatingService();
        restaurantService = new RestaurantService();
    }

    public void setFoodId(Integer foodId)
    {
        this.foodId = foodId;
        loadComments();
    }

    private void loadComments()
    {
        handleError();

        CompletableFuture<ListItemResponse> foodDetailsFuture = restaurantService.getFoodById(token, foodId);
        CompletableFuture<ItemRatingResponse> itemRatingsFuture = buyerRatingService.getItemRating(token, foodId);
        CompletableFuture.allOf(foodDetailsFuture, itemRatingsFuture)
                .thenAccept(v -> Platform.runLater(() -> {
                    try {
                        ListItemResponse food = foodDetailsFuture.get();
                        ItemRatingResponse itemRatings = itemRatingsFuture.get();
                        averageRatingLabel.setText(String.format("میانگین امتیاز: %.2f", itemRatings.getAvgRating()));

                        if (food != null)
                        {
                            foodNameLabel.setText(food.getName());
                            String base64 = food.getImageBase64();
                            if (base64 != null && !base64.isEmpty())
                            {
                                try {
                                    super.setProfileImage(foodLogoView, base64);
                                } catch (Exception e) {
                                    System.err.println("Error decoding food image for comments: " + e.getMessage());
                                    foodLogoView.setImage(null);
                                }
                            }
                            else
                                foodLogoView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/aut/shoomal/images/ShoomalFoodMahi.png"))));
                        }
                        else
                        {
                            showAlert("خطا", "خطا در بارگذاری اطلاعات غذا", Alert.AlertType.ERROR, null);
                            foodNameLabel.setText("غذا یافت نشد");
                        }

                        commentsContainerVBox.getChildren().clear();
                        if (!itemRatings.getComments().isEmpty())
                            for (RatingResponse rating : itemRatings.getComments())
                            {
                                try {
                                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aut/shoomal/views/components/CommentCard.fxml"));
                                    VBox commentCard = loader.load();
                                    CommentCardController commentCardController = loader.getController();

                                    commentCardController.setCommentData(rating);
                                    commentsContainerVBox.getChildren().add(commentCard);
                                } catch (Exception e) {
                                    System.err.println("Failed to load comment card: " + e.getMessage());
                                    e.printStackTrace();
                                    showAlert("خطا", "خطا در بارگذاری نظرات", Alert.AlertType.ERROR, null);
                                }
                            }
                        else
                            commentsContainerVBox.getChildren().add(new Label("هیچ نظری برای این غذا ثبت نشده است."));
                    } catch (Exception e) {
                        System.err.println("Error processing loaded data: " + e.getMessage());
                        e.printStackTrace();
                        showAlert("Error", "Failed to process comments data.", Alert.AlertType.ERROR, null);
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

    private void handleError()
    {
        if (token == null || token.isEmpty())
        {
            showAlert("Authentication Error", "User not logged in.", Alert.AlertType.ERROR, null);
            navigateToSignInView(backButton);
            return;
        }
        if (foodId == null)
            showAlert("خطا", "هیچ غذایی انتخاب نشده است", Alert.AlertType.ERROR, null);
    }

    @FXML
    public void handleBack(ActionEvent actionEvent)
    {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }
}