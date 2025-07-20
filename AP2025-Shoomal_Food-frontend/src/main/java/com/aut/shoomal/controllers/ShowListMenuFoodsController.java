package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.response.ListItemResponse;
import com.aut.shoomal.exceptions.FrontendServiceException;
import com.aut.shoomal.service.RestaurantService;
import com.aut.shoomal.utils.PreferencesManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class ShowListMenuFoodsController extends AbstractBaseController
{
    @FXML private TableView<ListItemResponse> foodTable;
    @FXML private TableColumn<ListItemResponse, Integer> idColumn;
    @FXML private TableColumn<ListItemResponse, String> nameColumn;
    @FXML private TableColumn<ListItemResponse, String> descriptionColumn;
    @FXML private TableColumn<ListItemResponse, Integer> priceColumn;
    @FXML private TableColumn<ListItemResponse, Integer> supplyColumn;
    @FXML private TableColumn<ListItemResponse, String> keywordsColumn;
    @FXML private TableColumn<ListItemResponse, Void> actionsColumn;
    @FXML private Button backButton;

    private RestaurantService restaurantService;
    private Integer restaurantId;
    private String token, title;
    private boolean ready = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        super.initialize(url, resourceBundle);
        restaurantService = new RestaurantService();
        token = PreferencesManager.getJwtToken();

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        supplyColumn.setCellValueFactory(new PropertyValueFactory<>("supply"));
        keywordsColumn.setCellValueFactory(new PropertyValueFactory<>("keywords"));
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("حذف از منو");
            private final HBox pane = new HBox(deleteButton);
            {
                deleteButton.getStyleClass().add("delete-button");
                deleteButton.setOnAction((ActionEvent event) -> {
                    ListItemResponse food = getTableView().getItems().get(getIndex());
                    handleDeleteFood(food.getId(), title);
                });
            }

            private void handleDeleteFood(Integer id, String title)
            {
                handleError();
                if (id == null)
                {
                    showAlert("Error", "Cannot delete food.", Alert.AlertType.ERROR, null);
                    return;
                }

                Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationAlert.setTitle("تایید حذف");
                confirmationAlert.setHeaderText(null);
                confirmationAlert.setContentText("آیا مطمئن هستید که می‌خواهید این غذا را حذف کنید؟");
                confirmationAlert.getDialogPane().getStylesheets().add(
                        Objects.requireNonNull(getClass().getResource("/com/aut/shoomal/styles/AlertStyles.css")).toExternalForm()
                );
                confirmationAlert.getDialogPane().getStyleClass().add("custom-alert");
                confirmationAlert.getDialogPane().getStyleClass().add("confirmation");

                confirmationAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK)
                    {
                        restaurantService.deleteItemFromMenu(token, restaurantId, title, id)
                                .thenAccept(apiResponse -> Platform.runLater(() -> {
                                    if (apiResponse.isSuccess())
                                    {
                                        showAlert("موفقیت", "غذا با موفقیت حذف شد.", Alert.AlertType.INFORMATION, null);
                                        loadFoods();
                                    }
                                    else
                                        showAlert("خطا", "خطا در حذف غذا: " + apiResponse.getMessage(), Alert.AlertType.ERROR, null);
                                }))
                                .exceptionally(e -> {
                                    Platform.runLater(() -> {
                                        if (e.getCause() instanceof FrontendServiceException fsException)
                                            showAlert(fsException);
                                        else
                                            showAlert("خطا", "خطای غیرمنتظره در حذف غذا: " + e.getMessage(), Alert.AlertType.ERROR, null);
                                    });
                                    return null;
                                });
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty)
            {
                super.updateItem(item, empty);
                if (empty)
                    setGraphic(null);
                else
                    setGraphic(pane);
            }
        });
    }

    private void loadFoods()
    {
        handleError();

        restaurantService.getFoodsByMenuTitle(token, restaurantId, title)
                .thenAccept(foods -> Platform.runLater(() -> {
                    if (foods != null)
                    {
                        ObservableList<ListItemResponse> foodList = FXCollections.observableArrayList(foods);
                        foodTable.setItems(foodList);
                    }
                    else
                        showAlert("Error", "Can not load food list.", Alert.AlertType.ERROR, null);
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
            showAlert("Authentication Error", "User not logged in. Please log in first.", Alert.AlertType.ERROR, null);
            navigateToSignInView(backButton);
            return;
        }
        if (restaurantId == null)
        {
            showAlert("Error", "No restaurant selected.", Alert.AlertType.ERROR, null);
            navigateToMainView(backButton);
        }

        if (title == null || title.isEmpty())
        {
            showAlert("Error", "No menu selected.", Alert.AlertType.ERROR, null);
            navigateToSignInView(backButton);
        }
    }

    @FXML
    public void handleBackButton(ActionEvent actionEvent)
    {
        navigateTo(
                (Node) actionEvent.getSource(),
                "/com/aut/shoomal/views/ListMenuView.fxml",
                "/com/aut/shoomal/styles/ListFoodsView.css",
                TransitionType.SLIDE_LEFT,
                controller -> {
                    if (controller instanceof ShowListMenuController showListMenuController)
                        showListMenuController.setRestaurantId(restaurantId);
                }
        );
    }

    public void setRestaurantId(Integer restaurantId)
    {
        this.restaurantId = restaurantId;
        canLoadFoods();
    }

    public void setTitle(String title)
    {
        this.title = title;
        canLoadFoods();
    }

    private void canLoadFoods()
    {
        if (restaurantId != null && title != null && !title.isEmpty() && !ready)
        {
            ready = true;
            loadFoods();
        }
    }
}