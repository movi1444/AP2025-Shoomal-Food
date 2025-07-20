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
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class ShowListMenuController extends AbstractBaseController
{
    @FXML private TableView<MenuTitleResponse> menuTable;
    @FXML private TableColumn<MenuTitleResponse, String> titleColumn;
    @FXML private TableColumn<MenuTitleResponse, Void> actionsColumn;
    @FXML private Button addMenuButton;
    @FXML private Button backButton;

    private RestaurantService restaurantService;
    private String token;
    private Integer restaurantId;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        super.initialize(url, resourceBundle);
        restaurantService = new RestaurantService();
        token = PreferencesManager.getJwtToken();

        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("ویرایش");
            private final Button listFoodsButton = new Button("غذا های منو");
            private final Button deleteButton = new Button("حذف منو");
            private final HBox pane = new HBox(10, editButton, listFoodsButton, deleteButton);
            {
                editButton.getStyleClass().add("table-cell-button");
                listFoodsButton.getStyleClass().add("table-cell-button");
                deleteButton.getStyleClass().add("delete-button");

                editButton.setOnAction((ActionEvent event) -> {
                   MenuTitleResponse menu = getTableView().getItems().get(getIndex());
                   handleEditMenu(menu.getTitle(), (Node) event.getSource());
                });

                listFoodsButton.setOnAction(event -> {
                   MenuTitleResponse menu = getTableView().getItems().get(getIndex());
                   handleShowMenuFoods(menu.getTitle(), (Node) event.getSource());
                });

                deleteButton.setOnAction(event -> {
                    MenuTitleResponse menu = getTableView().getItems().get(getIndex());
                    handleDeleteMenu(menu.getTitle());
                });
            }

            private void handleEditMenu(String title, Node source)
            {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aut/shoomal/views/MenuDetailsView.fxml"));
                    EditMenuController editMenuController = new EditMenuController();
                    loader.setController(editMenuController);
                    Parent root = loader.load();

                    editMenuController.setRestaurantId(restaurantId);
                    editMenuController.setTitle(title);

                    Stage stage = (Stage) source.getScene().getWindow();
                    Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
                    scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/aut/shoomal/styles/MainView.css")).toExternalForm());
                    stage.setScene(scene);
                    stage.setTitle("Add menu");
                    stage.show();
                } catch (Exception e) {
                    System.err.println("Failed to load MenuDetailsView.fxml for Edit Food: " + e.getMessage());
                    e.printStackTrace();
                    showAlert("Navigation Error", "Failed to load edit menu page.", Alert.AlertType.ERROR, null);
                }
            }

            private void handleDeleteMenu(String title)
            {
                handleError(title);
                Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationAlert.setTitle("تایید حذف");
                confirmationAlert.setHeaderText(null);
                confirmationAlert.setContentText("آیا مطمئن هستید که می‌خواهید این منو را حذف کنید؟");
                confirmationAlert.getDialogPane().getStylesheets().add(
                        Objects.requireNonNull(getClass().getResource("/com/aut/shoomal/styles/AlertStyles.css")).toExternalForm()
                );
                confirmationAlert.getDialogPane().getStyleClass().add("custom-alert");
                confirmationAlert.getDialogPane().getStyleClass().add("confirmation");

                confirmationAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK)
                    {
                        restaurantService.deleteMenu(token, restaurantId, title)
                                .thenAccept(apiResponse -> Platform.runLater(() -> {
                                    if (apiResponse.isSuccess())
                                    {
                                        showAlert("موفقیت", "منو با موفقیت حذف شد.", Alert.AlertType.INFORMATION, null);
                                        loadMenus();
                                    }
                                    else
                                        showAlert("خطا", "خطا در حذف منو: " + apiResponse.getMessage(), Alert.AlertType.ERROR, null);
                                }))
                                .exceptionally(e -> {
                                    Platform.runLater(() -> {
                                        if (e.getCause() instanceof FrontendServiceException fsException)
                                            showAlert(fsException);
                                        else
                                            showAlert("خطا", "خطای غیرمنتظره در حذف منو: " + e.getMessage(), Alert.AlertType.ERROR, null);
                                    });
                                    return null;
                                });
                    }
                });
            }

            private void handleShowMenuFoods(String title, Node source)
            {
                handleError(title);
                navigateTo(
                        source,
                        "/com/aut/shoomal/views/ListMenuFoodsView.fxml",
                        "/com/aut/shoomal/styles/ListFoodsView.css",
                        TransitionType.SLIDE_LEFT,
                        controller -> {
                            if (controller instanceof ShowListMenuFoodsController showListMenuFoodsController)
                            {
                                showListMenuFoodsController.setRestaurantId(restaurantId);
                                showListMenuFoodsController.setTitle(title);
                            }
                        }
                );
            }

            private void handleError(String title)
            {
                if (token == null || token.isEmpty())
                {
                    showAlert("Authentication Error", "User not logged in. Please log in first.", Alert.AlertType.ERROR, null);
                    navigateToSignInView(deleteButton);
                    return;
                }

                if (title == null || title.isEmpty())
                    showAlert("Error", "No menu selected.", Alert.AlertType.ERROR, null);
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

    public void loadMenus()
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
                            showAlert("خطا", "خطای غیرمنتظره در حذف غذا: " + e.getMessage(), Alert.AlertType.ERROR, null);
                    });
                    return null;
                });
    }

    @FXML
    public void handleBackButton(ActionEvent actionEvent)
    {
        navigateToMainView((Node) actionEvent.getSource());
    }

    public void setRestaurantId(Integer restaurantId)
    {
        this.restaurantId = restaurantId;
        loadMenus();
    }

    @FXML
    public void handleAddMenu(ActionEvent actionEvent)
    {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aut/shoomal/views/MenuDetailsView.fxml"));
            AddMenuController addMenuController = new AddMenuController();
            loader.setController(addMenuController);
            Parent root = loader.load();

            addMenuController.setRestaurantId(restaurantId);

            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/aut/shoomal/styles/MainView.css")).toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Add menu");
            stage.show();
        } catch (Exception e) {
            System.err.println("Failed to load MenuDetailsView.fxml for Add Food: " + e.getMessage());
            e.printStackTrace();
            showAlert("Navigation Error", "Failed to load add menu page.", Alert.AlertType.ERROR, null);
        }
    }
}