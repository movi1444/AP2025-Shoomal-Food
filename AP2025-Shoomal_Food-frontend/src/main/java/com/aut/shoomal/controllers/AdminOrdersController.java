package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.response.OrderResponse;
import com.aut.shoomal.dto.response.RestaurantResponse;
import com.aut.shoomal.dto.response.UserResponse;
import com.aut.shoomal.exceptions.FrontendServiceException;
import com.aut.shoomal.service.AdminDataService;
import com.aut.shoomal.utils.PreferencesManager;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminOrdersController extends AbstractBaseController {

    @FXML private TableView<OrderResponse> ordersTableView;
    @FXML private TableColumn<OrderResponse, Integer> orderIdColumn;
    @FXML private TableColumn<OrderResponse, String> orderStatusColumn;
    @FXML private TableColumn<OrderResponse, Integer> orderCustomerColumn;
    @FXML private TableColumn<OrderResponse, Integer> orderVendorColumn;
    @FXML private TableColumn<OrderResponse, Integer> orderCourierColumn;
    @FXML private TableColumn<OrderResponse, String> orderDeliveryAddressColumn;
    @FXML private TableColumn<OrderResponse, Integer> orderRawPriceColumn;
    @FXML private TableColumn<OrderResponse, Integer> orderPayPriceColumn;
    @FXML private TableColumn<OrderResponse, String> orderCreatedAtColumn;
    @FXML private TextField searchTextField;
    @FXML private ComboBox<UserResponse> customerComboBox;
    @FXML private ComboBox<RestaurantResponse> restaurantComboBox;
    @FXML private ComboBox<UserResponse> courierComboBox;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private VBox filterSidebar;
    @FXML private Button filterButton;
    @FXML private Button backButton;

    private AdminDataService adminDataService;
    private String token;
    private UserResponse loggedInUser;
    private boolean isFilterSidebarVisible = false;

    private ObservableList<UserResponse> allCustomers = FXCollections.observableArrayList();
    private ObservableList<RestaurantResponse> allRestaurants = FXCollections.observableArrayList();
    private ObservableList<UserResponse> allCouriers = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        adminDataService = new AdminDataService();
        token = PreferencesManager.getJwtToken();
        setupOrdersTable();
        setupStatusFilterComboBox();
        setupFilterComboBoxes();
        loadOrders(null, null, null, null, null);

        if (filterSidebar != null) {
            filterSidebar.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            filterSidebar.setVisible(false);
            filterSidebar.setManaged(false);
        }
    }

    public void setLoggedInUser(UserResponse user) {
        this.loggedInUser = user;
    }

    private void setupOrdersTable() {
        ordersTableView.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        ordersTableView.setEditable(false);

        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        orderStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        orderCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        orderVendorColumn.setCellValueFactory(new PropertyValueFactory<>("vendorId"));
        orderCourierColumn.setCellValueFactory(new PropertyValueFactory<>("courierId"));
        orderDeliveryAddressColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryAddress"));
        orderRawPriceColumn.setCellValueFactory(new PropertyValueFactory<>("rawPrice"));
        orderPayPriceColumn.setCellValueFactory(new PropertyValueFactory<>("payPrice"));
        orderCreatedAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
    }

    private void setupStatusFilterComboBox() {
        ObservableList<String> statuses = FXCollections.observableArrayList(
                "همه وضعیت‌ها",
                "submitted",
                "unpaid and cancelled",
                "waiting vendor",
                "cancelled",
                "finding courier",
                "on the way",
                "completed"
        );
        statusFilterComboBox.setItems(statuses);
        statusFilterComboBox.getSelectionModel().selectFirst();
    }

    private void setupFilterComboBoxes() {
        customerComboBox.setCellFactory(lv -> new ListCell<UserResponse>() {
            @Override
            protected void updateItem(UserResponse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName() + " (" + item.getId() + ")");
            }
        });
        customerComboBox.setButtonCell(new ListCell<UserResponse>() {
            @Override
            protected void updateItem(UserResponse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "انتخاب مشتری" : item.getName() + " (" + item.getId() + ")");
            }
        });

        restaurantComboBox.setCellFactory(lv -> new ListCell<RestaurantResponse>() {
            @Override
            protected void updateItem(RestaurantResponse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName() + " (" + item.getId() + ")");
            }
        });
        restaurantComboBox.setButtonCell(new ListCell<RestaurantResponse>() {
            @Override
            protected void updateItem(RestaurantResponse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "انتخاب رستوران" : item.getName() + " (" + item.getId() + ")");
            }
        });

        courierComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(UserResponse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        courierComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(UserResponse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        loadAllFilterDataForComboBoxes();
    }

    private void loadAllFilterDataForComboBoxes() {
        if (token == null || token.isEmpty()) {
            showAlert("خطای احراز هویت", "ادمین وارد نشده است. لطفا ابتدا وارد شوید.", Alert.AlertType.ERROR, null);
            return;
        }

        adminDataService.getAllUsers(token)
                .thenAccept(users -> Platform.runLater(() -> {
                    if (users != null && !users.isEmpty()) {
                        allCustomers.setAll(users.stream().filter(u -> "Buyer".equals(u.getRole())).collect(Collectors.toList()));
                        allCouriers.setAll(users.stream().filter(u -> "Courier".equals(u.getRole())).collect(Collectors.toList()));

                        customerComboBox.setItems(allCustomers);
                        courierComboBox.setItems(allCouriers);

                        addAutoCompleteForUsers(customerComboBox, allCustomers);
                        addAutoCompleteForUsers(courierComboBox, allCouriers);
                    } else {
                        System.out.println("No users found for populating filter dropdowns.");
                    }
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        if (e.getCause() instanceof FrontendServiceException fsException)
                            showAlert(fsException);
                        else
                            showAlert("خطا", "Failed to load users for filter dropdowns: " + e.getMessage(), Alert.AlertType.ERROR, null);
                    });
                    return null;
                });

        adminDataService.getAllRestaurants(token)
                .thenAccept(restaurants -> Platform.runLater(() -> {
                    if (restaurants != null && !restaurants.isEmpty()) {
                        allRestaurants.setAll(restaurants);
                        restaurantComboBox.setItems(allRestaurants);
                        addAutoCompleteForRestaurants(restaurantComboBox, allRestaurants);
                    } else {
                        System.out.println("No restaurants found for populating filter dropdowns.");
                    }
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        if (e.getCause() instanceof FrontendServiceException fsException)
                            showAlert(fsException);
                        else
                            showAlert("خطا", "Failed to load restaurants for filter dropdowns: " + e.getMessage(), Alert.AlertType.ERROR, null);
                    });
                    return null;
                });
    }

    private void addAutoCompleteForUsers(ComboBox<UserResponse> comboBox, ObservableList<UserResponse> originalItems) {
        comboBox.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                comboBox.setItems(originalItems);
                return;
            }

            comboBox.hide();
            comboBox.setItems(originalItems.stream()
                    .filter(user -> user.getName().toLowerCase().contains(newValue.toLowerCase()) ||
                            String.valueOf(user.getId()).contains(newValue))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList)));
            comboBox.show();
        });

        comboBox.setPromptText(comboBox.getPromptText());
        comboBox.setEditable(true);
    }

    private void addAutoCompleteForRestaurants(ComboBox<RestaurantResponse> comboBox, ObservableList<RestaurantResponse> originalItems) {
        comboBox.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                comboBox.setItems(originalItems);
                return;
            }

            comboBox.hide();
            comboBox.setItems(originalItems.stream()
                    .filter(restaurant -> restaurant.getName().toLowerCase().contains(newValue.toLowerCase()) ||
                            String.valueOf(restaurant.getId()).contains(newValue))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList)));
            comboBox.show();
        });

        comboBox.setPromptText(comboBox.getPromptText());
        comboBox.setEditable(true);
    }

    private void loadOrders(String search, String restaurant, String customer, String courier, String status) {
        if (token == null || token.isEmpty()) {
            showAlert("خطای احراز هویت", "ادمین وارد نشده است. لطفا ابتدا وارد شوید.", Alert.AlertType.ERROR, null);
            navigateToSignInView(ordersTableView);
            return;
        }

        adminDataService.getAllOrders(token, search, restaurant, customer, courier, status)
                .thenAccept(orders -> Platform.runLater(() -> {
                    if (orders != null && !orders.isEmpty()) {
                        ordersTableView.setItems(FXCollections.observableArrayList(orders));
                    } else {
                        ordersTableView.setItems(FXCollections.emptyObservableList());
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
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), filterSidebar);
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
        String search = searchTextField.getText();
        String customerName = customerComboBox.getSelectionModel().getSelectedItem() != null ? customerComboBox.getSelectionModel().getSelectedItem().getName() : null;
        String restaurantName = restaurantComboBox.getSelectionModel().getSelectedItem() != null ? restaurantComboBox.getSelectionModel().getSelectedItem().getName() : null;
        String courierName = courierComboBox.getSelectionModel().getSelectedItem() != null ? courierComboBox.getSelectionModel().getSelectedItem().getName() : null;
        String status = statusFilterComboBox.getValue();

        loadOrders(
                search != null && !search.isEmpty() ? search : null,
                restaurantName,
                customerName,
                courierName,
                status != null && !status.isEmpty() && !status.equals("همه وضعیت‌ها") ? status : null
        );

        handleFilterOrders();
    }

    @FXML
    private void clearOrderFilters() {
        searchTextField.clear();

        customerComboBox.getSelectionModel().clearSelection();
        customerComboBox.setItems(allCustomers);
        customerComboBox.getEditor().clear();

        restaurantComboBox.getSelectionModel().clearSelection();
        restaurantComboBox.setItems(allRestaurants);
        restaurantComboBox.getEditor().clear();

        courierComboBox.getSelectionModel().clearSelection();
        courierComboBox.setItems(allCouriers);
        courierComboBox.getEditor().clear();

        statusFilterComboBox.getSelectionModel().selectFirst();
        loadOrders(null, null, null, null, null);
        handleFilterOrders();
    }

    @FXML
    private void handleBackToAdminDashboard() {
        navigateTo(
                backButton,
                "/com/aut/shoomal/views/AdminDashboardContent.fxml",
                "/com/aut/shoomal/styles/AdminDashboardStyles.css",
                TransitionType.SLIDE_LEFT,
                (AdminDashboardContentController controller) -> {
                    controller.setLoggedInUser(loggedInUser);
                }
        );
    }
}