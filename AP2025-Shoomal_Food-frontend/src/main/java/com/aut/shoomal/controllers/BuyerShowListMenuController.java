package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.response.MenuTitleResponse;
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
import java.util.ResourceBundle;

public class BuyerShowListMenuController extends AbstractBaseController
{
    @FXML private Button backButton;
    @FXML private TableView<MenuTitleResponse> menuTable;
    @FXML private TableColumn<MenuTitleResponse, String> titleColumn;
    @FXML private TableColumn<MenuTitleResponse, Void> actionsColumn;

    private RestaurantService restaurantService;
    private Integer restaurantId;
    private String token;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        super.initialize(url, resourceBundle);
        restaurantService = new RestaurantService();
        token = PreferencesManager.getJwtToken();
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button listFoodsButton = new Button("لیست غذا ها");
            private final HBox pane = new HBox(listFoodsButton);
            {
                listFoodsButton.getStyleClass().add("table-cell-button");
                listFoodsButton.setOnAction(event -> {
                    MenuTitleResponse menu = getTableView().getItems().get(getIndex());
                    handleShowMenuFoods(menu.getTitle(), (Node) event.getSource());
                });
            }

            private void handleShowMenuFoods(String title, Node source)
            {
                navigateTo(
                        source,
                        "/com/aut/shoomal/views/BuyerMenuFoodsView.fxml",
                        "/com/aut/shoomal/styles/MainView.css",
                        TransitionType.SLIDE_LEFT,
                        controller -> {
                            if (controller instanceof BuyerShowMenuFoodsController menuFoodsController)
                            {
                                menuFoodsController.setRestaurantId(restaurantId);
                                menuFoodsController.setTitle(title);
                            }
                        }
                );
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

    @FXML
    public void handleBackButton(ActionEvent actionEvent)
    {
        navigateTo(
                (Node) actionEvent.getSource(),
                "/com/aut/shoomal/views/BuyerRestaurantView.fxml",
                "/com/aut/shoomal/styles/MainView.css",
                TransitionType.SLIDE_LEFT,
                controller -> {
                    if (controller instanceof BuyerShowRestaurantDetailsController detailsController)
                        detailsController.setRestaurantId(restaurantId);
                }
        );
    }

    private void loadMenus()
    {
        handleError();

        restaurantService.getMenusByRestaurantId(token, restaurantId)
                .thenAccept(menus -> Platform.runLater(() -> {
                    if (menus != null)
                    {
                        ObservableList<MenuTitleResponse> menuList = FXCollections.observableArrayList(menus);
                        menuTable.setItems(menuList);
                    }
                    else
                        showAlert("Error", "Cannot show menus.", Alert.AlertType.ERROR, null);
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        if (e.getCause() instanceof FrontendServiceException fsException)
                            showAlert(fsException);
                        else
                            showAlert("خطا", "خطای غیرمنتظره در نمایش منو ها: " + e.getMessage(), Alert.AlertType.ERROR, null);
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
    }

    public void setRestaurantId(Integer restaurantId)
    {
        this.restaurantId = restaurantId;
        loadMenus();
    }
}