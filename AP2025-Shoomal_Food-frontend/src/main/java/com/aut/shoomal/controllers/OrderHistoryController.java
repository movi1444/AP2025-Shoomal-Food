package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.response.OrderResponse;
import com.aut.shoomal.dto.response.RestaurantResponse;
import com.aut.shoomal.dto.response.UserResponse;
import com.aut.shoomal.exceptions.FrontendServiceException;
import com.aut.shoomal.service.BuyerFavoriteService;
import com.aut.shoomal.service.BuyerRatingService;
import com.aut.shoomal.service.OrderService;
import com.aut.shoomal.service.BuyerService;
import com.aut.shoomal.utils.PreferencesManager;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class OrderHistoryController extends AbstractBaseController {

    @FXML private TableView<OrderResponse> orderTable;
    @FXML private TableColumn<OrderResponse, Integer> idColumn;
    @FXML private TableColumn<OrderResponse, String> deliveryAddressColumn;
    @FXML private TableColumn<OrderResponse, Integer> vendorIdColumn;
    @FXML private TableColumn<OrderResponse, Integer> rawPriceColumn;
    @FXML private TableColumn<OrderResponse, String> statusColumn;
    @FXML private TableColumn<OrderResponse, String> createdAtColumn;
    @FXML private TableColumn<OrderResponse, Void> ratingActionsColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<RestaurantResponse> vendorFilterComboBox;
    @FXML private VBox filterSidebar;
    @FXML private Button backButton;
    @FXML private Button filterButton;
    @FXML private Button clearFilterButton;


    private OrderService orderService;
    private BuyerService buyerService;
    private BuyerRatingService buyerRatingService;
    private String token;
    private UserResponse loggedInUser;
    private boolean isFilterSidebarVisible = false;

    private ObservableList<RestaurantResponse> allVendors = FXCollections.observableArrayList();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        orderService = new OrderService();
        buyerService = new BuyerService();
        buyerRatingService = new BuyerRatingService();
        token = PreferencesManager.getJwtToken();

        setupOrderTable();
        setupFilterComboBoxes();
        loadOrders(null, null);

        if (filterSidebar != null) {
            filterSidebar.setVisible(false);
            filterSidebar.setManaged(false);
        }
    }

    public void setLoggedInUser(UserResponse user) {
        this.loggedInUser = user;
    }

    private void setupOrderTable() {
        orderTable.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        orderTable.setEditable(false);

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        deliveryAddressColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryAddress"));
        vendorIdColumn.setCellValueFactory(new PropertyValueFactory<>("vendorId"));
        rawPriceColumn.setCellValueFactory(new PropertyValueFactory<>("rawPrice"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        orderTable.getSortOrder().clear();
        orderTable.getSortOrder().add(idColumn);
        idColumn.setSortType(TableColumn.SortType.ASCENDING);

        ratingActionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button addRatingButton = new Button("افزودن");
            private final Button updateRatingButton = new Button("مشاهده و ویرایش");
            private final Button deleteRatingButton = new Button("حذف");
            private final HBox pane = new HBox(5, addRatingButton, updateRatingButton, deleteRatingButton);
            {
                addRatingButton.getStyleClass().add("table-cell-button");
                updateRatingButton.getStyleClass().add("table-cell-button");
                deleteRatingButton.getStyleClass().add("delete-button");

                addRatingButton.setOnAction(event -> {
                    OrderResponse order = getTableView().getItems().get(getIndex());
                    handleOpenRatingPage(order.getId(), "ADD");
                });

                updateRatingButton.setOnAction(event -> {
                    OrderResponse order = getTableView().getItems().get(getIndex());
                    handleOpenRatingPage(order.getId(), "UPDATE");
                });

                deleteRatingButton.setOnAction(event -> {
                    OrderResponse order = getTableView().getItems().get(getIndex());
                    handleDeleteRating(order.getId());
                });
            }

            private void handleDeleteRating(Integer orderId)
            {
                if (token == null || token.isEmpty())
                {
                    showAlert("خطای احراز هویت", "کاربر وارد نشده است. لطفا ابتدا وارد شوید.", Alert.AlertType.ERROR, null);
                    navigateToSignInView(orderTable);
                    return;
                }

                Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationAlert.setTitle("تایید حذف");
                confirmationAlert.setHeaderText(null);
                confirmationAlert.setContentText("آیا مطمئن هستید که می‌خواهید نظر خود را حذف کنید؟");
                confirmationAlert.getDialogPane().getStylesheets().add(
                        Objects.requireNonNull(getClass().getResource("/com/aut/shoomal/styles/AlertStyles.css")).toExternalForm()
                );
                confirmationAlert.getDialogPane().getStyleClass().add("custom-alert");
                confirmationAlert.getDialogPane().getStyleClass().add("confirmation");

                confirmationAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK)
                    {
                        buyerRatingService.deleteRating(token, orderId)
                                .thenAccept(result -> Platform.runLater(() -> {
                                    if (result.isSuccess()) {
                                        showAlert("موفقیت", "نظر شما با موفقیت حذف شد.", Alert.AlertType.INFORMATION, null);
                                        loadOrders(null, null);
                                    } else
                                        showAlert("خطا", "خطا در حذف نظر", Alert.AlertType.ERROR, null);
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
                });
            }

            private void handleOpenRatingPage(Integer orderId, String mode)
            {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aut/shoomal/views/RatingView.fxml"));
                    Parent root = loader.load();

                    RatingController ratingController = loader.getController();
                    if (ratingController != null)
                        ratingController.setRatingContext(orderId, mode);

                    Stage stage = new Stage();
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setTitle("امتیازدهی و نظر");
                    stage.setScene(new Scene(root));
                    stage.showAndWait();

                    loadOrders(null, null);
                } catch (IOException e) {
                    System.err.println("Failed to load RatingView.fxml: " + e.getMessage());
                    e.printStackTrace();
                    showAlert("Error", "Failed to open rating page.", Alert.AlertType.ERROR, null);
                }
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

    private void setupFilterComboBoxes() {
        vendorFilterComboBox.setCellFactory(lv -> new ListCell<RestaurantResponse>() {
            @Override
            protected void updateItem(RestaurantResponse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName());
            }
        });
        vendorFilterComboBox.setButtonCell(new ListCell<RestaurantResponse>() {
            @Override
            protected void updateItem(RestaurantResponse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "انتخاب رستوران" : item.getName());
            }
        });
        vendorFilterComboBox.setConverter(new StringConverter<RestaurantResponse>() {
            @Override
            public String toString(RestaurantResponse restaurant) {
                return restaurant == null ? "" : restaurant.getName();
            }

            @Override
            public RestaurantResponse fromString(String string) {
                if (string == null || string.trim().isEmpty()) {
                    return null;
                }
                int lastParenIndex = string.lastIndexOf('(');
                if (lastParenIndex > 0 && string.endsWith(")")) {
                    String namePart = string.substring(0, lastParenIndex).trim();
                    String idPart = string.substring(lastParenIndex + 1, string.length() - 1).trim();
                    try {
                        Integer id = Integer.parseInt(idPart);
                        return allVendors.stream()
                                .filter(r -> r.getId().equals(id) && r.getName().equalsIgnoreCase(namePart))
                                .findFirst()
                                .orElse(null);
                    } catch (NumberFormatException e) {
                        return allVendors.stream()
                                .filter(restaurant -> restaurant.getName().equalsIgnoreCase(namePart))
                                .findFirst()
                                .orElse(null);
                    }
                }
                return allVendors.stream()
                        .filter(restaurant -> restaurant.getName().equalsIgnoreCase(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        loadAllFilterDataForComboBoxes();
    }

    private void loadAllFilterDataForComboBoxes() {
        if (token == null || token.isEmpty()) {
            showAlert("خطای احراز هویت", "کاربر وارد نشده است. لطفا ابتدا وارد شوید.", Alert.AlertType.ERROR, null);
            return;
        }

        buyerService.getAllRestaurants(token)
                .thenAccept(restaurants -> Platform.runLater(() -> {
                    if (restaurants != null && !restaurants.isEmpty()) {
                        allVendors.setAll(restaurants);
                        vendorFilterComboBox.setItems(allVendors);
                        addAutoCompleteForRestaurants(vendorFilterComboBox, allVendors);
                    } else {
                        System.out.println("No vendors found for populating filter dropdowns.");
                    }
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        if (e.getCause() instanceof FrontendServiceException fsException)
                            showAlert(fsException);
                        else
                            showAlert("خطا", "Failed to load vendors for filter dropdowns: " + e.getMessage(), Alert.AlertType.ERROR, null);
                    });
                    return null;
                });
    }

    private void addAutoCompleteForRestaurants(ComboBox<RestaurantResponse> comboBox, ObservableList<RestaurantResponse> originalItems) {
        comboBox.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            RestaurantResponse selected = comboBox.getSelectionModel().getSelectedItem();
            if (selected != null && selected.getName().equalsIgnoreCase(newValue)) {
                return;
            }

            if (newValue == null || newValue.isEmpty()) {
                comboBox.setItems(originalItems);
                comboBox.getSelectionModel().clearSelection();
                return;
            }

            comboBox.hide();
            ObservableList<RestaurantResponse> filteredItems = originalItems.stream()
                    .filter(restaurant -> restaurant.getName().toLowerCase().contains(newValue.toLowerCase()) ||
                            String.valueOf(restaurant.getId()).contains(newValue))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));

            comboBox.setItems(filteredItems);
            comboBox.show();
        });

        comboBox.setPromptText("انتخاب رستوران");
        comboBox.setEditable(true);
    }


    private void loadOrders(String search, String vendor) {
        if (token == null || token.isEmpty()) {
            showAlert("خطای احراز هویت", "کاربر وارد نشده است. لطفا ابتدا وارد شوید.", Alert.AlertType.ERROR, null);
            navigateToSignInView(orderTable);
            return;
        }

        orderService.getBuyerOrderHistory(token, search, vendor)
                .thenAccept(orders -> Platform.runLater(() -> {
                    if (orders != null && !orders.isEmpty()) {
                        orders.sort(Comparator.comparing(OrderResponse::getId));
                        orderTable.setItems(FXCollections.observableArrayList(orders));
                        orderTable.sort();
                    } else {
                        orderTable.setItems(FXCollections.emptyObservableList());
                        showAlert("اطلاعات", "سفارشی با این فیلترها یافت نشد.", Alert.AlertType.INFORMATION, null);
                    }
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        if (e.getCause() instanceof FrontendServiceException fsException)
                            showAlert(fsException);
                        else
                            showAlert("خطا", "لیست سفارشات بارگیری نشد: " + e.getMessage(), Alert.AlertType.ERROR, null);
                    });
                    return null;
                });
    }

    @FXML
    private void handleFilterOrders() {
        TranslateTransition transition = new TranslateTransition(Duration.millis(500), filterSidebar);
        if (isFilterSidebarVisible) {
            transition.setToX(filterSidebar.getWidth());
            transition.setOnFinished(event -> {
                filterSidebar.setVisible(false);
                filterSidebar.setManaged(false);
            });
        } else {
            filterSidebar.setVisible(true);
            filterSidebar.setManaged(true);
            transition.setFromX(filterSidebar.getWidth());
            transition.setToX(0);
        }
        transition.play();
        isFilterSidebarVisible = !isFilterSidebarVisible;
    }

    @FXML
    private void applyOrderFilters() {
        String search = searchField.getText();
        RestaurantResponse selectedVendor = vendorFilterComboBox.getSelectionModel().getSelectedItem();
        String vendorName = selectedVendor != null ? selectedVendor.getName() : null;

        loadOrders(
                search != null && !search.isEmpty() ? search : null,
                vendorName
        );

        handleFilterOrders();
    }

    @FXML
    private void clearOrderFilters() {
        searchField.clear();

        vendorFilterComboBox.getSelectionModel().clearSelection();
        vendorFilterComboBox.setItems(allVendors);
        vendorFilterComboBox.getEditor().clear();

        loadOrders(null, null);
        handleFilterOrders();
    }

    public void handleBackButton(ActionEvent actionEvent)
    {
        navigateToUserProfileView(actionEvent.getSource());
    }
}