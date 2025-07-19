package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.request.AddMenuItemRequest;
import com.aut.shoomal.dto.response.ListItemResponse;
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

public class ShowListFoodController extends AbstractBaseController
{
    @FXML private TableView<ListItemResponse> foodTable;
    @FXML private TableColumn<ListItemResponse, Integer> idColumn;
    @FXML private TableColumn<ListItemResponse, String> nameColumn;
    @FXML private TableColumn<ListItemResponse, String> descriptionColumn;
    @FXML private TableColumn<ListItemResponse, Integer> priceColumn;
    @FXML private TableColumn<ListItemResponse, Integer> supplyColumn;
    @FXML private TableColumn<ListItemResponse, String> keywordsColumn;
    @FXML private TableColumn<ListItemResponse, Void> actionsColumn;
    @FXML private Button addFoodButton;
    @FXML private Button backButton;

    private RestaurantService restaurantService;
    private String token;
    private Integer restaurantId;
    private ObservableList<MenuTitleResponse> menuTitles;

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
            private final Button editButton = new Button("ویرایش");
            private final ChoiceBox<String> menuChoiceBox = new ChoiceBox<>();
            private final Button addToMenuButton = new Button("افزودن به منو");
            private final HBox pane = new HBox(5, editButton, menuChoiceBox, addToMenuButton);
            {
                editButton.getStyleClass().add("table-cell-button");
                addToMenuButton.getStyleClass().add("table-cell-button");
                menuChoiceBox.getStyleClass().add("table-cell-choice-box");

                menuChoiceBox.setItems(FXCollections.observableArrayList());
                addChoiceBoxDirectionListener(menuChoiceBox);

                editButton.setOnAction((ActionEvent event) -> {
                    ListItemResponse food = getTableView().getItems().get(getIndex());
                    handleEditFood(food.getId(), (Node) event.getSource());
                });

                addToMenuButton.setOnAction((ActionEvent event) -> {
                    ListItemResponse food = getTableView().getItems().get(getIndex());
                    String selectedMenuTitle = menuChoiceBox.getValue();
                    if (selectedMenuTitle == null || selectedMenuTitle.isEmpty() || selectedMenuTitle.equals("انتخاب منو"))
                    {
                        showAlert("خطا", "لطفا یک منو را انتخاب کنید.", Alert.AlertType.WARNING, null);
                        return;
                    }
                    handleAddFoodToMenu(food.getId(), selectedMenuTitle);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty)
            {
                super.updateItem(item, empty);
                if (empty)
                    setGraphic(null);
                else
                {
                    setGraphic(pane);
                    if (menuTitles != null && !menuTitles.isEmpty())
                    {
                        ObservableList<String> titles = FXCollections.observableArrayList();
                        for (MenuTitleResponse menu : menuTitles)
                            titles.add(menu.getTitle());
                        menuChoiceBox.setItems(titles);
                        menuChoiceBox.getSelectionModel().clearSelection();
                    }
                    else
                        menuChoiceBox.setItems(FXCollections.emptyObservableList());
                }
            }
        });
    }

    private void handleAddFoodToMenu(Integer foodId, String title)
    {
        handleError();
        if (foodId == null || title == null || title.isEmpty())
        {
            showAlert("Error", "Cannot add food to the menu.", Alert.AlertType.ERROR, null);
            return;
        }

        AddMenuItemRequest request = new AddMenuItemRequest();
        request.setItemId(foodId);
        restaurantService.addItemToMenu(request, token, restaurantId, title)
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess())
                    {
                        showAlert("Success", "Food added to the menu.", Alert.AlertType.INFORMATION, null);
                    }
                    else
                        showAlert("Error", "failed to add food to menu titles.", Alert.AlertType.ERROR, null);
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        if (e.getCause() instanceof FrontendServiceException fsException)
                            showAlert(fsException);
                        else
                            showAlert("Error", "Failed to add food to menu: " + e.getMessage(), Alert.AlertType.ERROR, null);
                    });
                    this.menuTitles = FXCollections.emptyObservableList();
                    return null;
                });
    }

    public void setRestaurantId(Integer restaurantId)
    {
        this.restaurantId = restaurantId;
        loadFoods();
        loadMenuTitles();
    }

    private void loadMenuTitles()
    {
        handleError();

        restaurantService.getMenusByRestaurantId(token, restaurantId)
                .thenAccept(menus -> Platform.runLater(() -> {
                    if (menus != null)
                        menuTitles = FXCollections.observableArrayList(menus);
                    else
                    {
                        this.menuTitles = FXCollections.emptyObservableList();
                        showAlert("Error", "failed to load menu titles.", Alert.AlertType.ERROR, null);
                    }
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        if (e.getCause() instanceof FrontendServiceException fsException)
                            showAlert(fsException);
                        else
                            showAlert("Error", "Failed to load menu titles: " + e.getMessage(), Alert.AlertType.ERROR, null);
                    });
                    this.menuTitles = FXCollections.emptyObservableList();
                    return null;
                });
    }

    private void loadFoods()
    {
        handleError();

        restaurantService.getFoodsByRestaurantId(token, restaurantId)
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

    @FXML
    public void handleBackButton(ActionEvent actionEvent)
    {
        navigateToMainView((Node) actionEvent.getSource());
    }

    public void handleEditFood(Integer foodId, Node editButton)
    {
        handleError();
        if (foodId == null)
        {
            showAlert("Error", "Food id missing.", Alert.AlertType.ERROR, null);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aut/shoomal/views/FoodDetailsView.fxml"));
            EditFoodController editFoodController = new EditFoodController();
            loader.setController(editFoodController);
            Parent root = loader.load();

            editFoodController.setRestaurantId(restaurantId);
            editFoodController.setFoodId(foodId);

            Stage stage = (Stage) editButton.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/aut/shoomal/styles/MainView.css")).toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Edit food");
            stage.show();
        } catch (Exception e) {
            System.err.println("Failed to load FoodDetailsView.fxml for Edit Food: " + e.getMessage());
            e.printStackTrace();
            showAlert("Navigation Error", "Failed to load edit food page.", Alert.AlertType.ERROR, null);
        }
    }

    @FXML
    public void handleAddFood(ActionEvent actionEvent)
    {
        handleError();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aut/shoomal/views/FoodDetailsView.fxml"));
            AddFoodController addFoodController = new AddFoodController();
            loader.setController(addFoodController);
            Parent root = loader.load();

            addFoodController.setRestaurantId(restaurantId);

            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/aut/shoomal/styles/MainView.css")).toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Edit food");
            stage.show();
        } catch (Exception e) {
            System.err.println("Failed to load FoodDetailsView.fxml for Edit Food: " + e.getMessage());
            e.printStackTrace();
            showAlert("Navigation Error", "Failed to load edit food page.", Alert.AlertType.ERROR, null);
        }
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
}