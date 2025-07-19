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
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.Comparator;
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
    @FXML private BorderPane filterSidebar;
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

        ordersTableView.getSortOrder().clear();
        ordersTableView.getSortOrder().add(orderIdColumn);
        orderIdColumn.setSortType(TableColumn.SortType.ASCENDING);
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
                setText(empty ? "" : item.getName());
            }
        });
        customerComboBox.setButtonCell(new ListCell<UserResponse>() {
            @Override
            protected void updateItem(UserResponse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "انتخاب مشتری" : item.getName());
            }
        });
        customerComboBox.setConverter(new StringConverter<UserResponse>() {
            @Override
            public String toString(UserResponse user) {
                return user == null ? "" : user.getName();
            }

            @Override
            public UserResponse fromString(String string) {
                if (string == null || string.trim().isEmpty()) {
                    return null;
                }
                int lastParenIndex = string.lastIndexOf('(');
                if (lastParenIndex > 0 && string.endsWith(")")) {
                    String namePart = string.substring(0, lastParenIndex).trim();
                    String idPart = string.substring(lastParenIndex + 1, string.length() - 1).trim();
                    try {
                        Long id = Long.parseLong(idPart);
                        return allCustomers.stream()
                                .filter(u -> u.getId().equals(id) && u.getName().equalsIgnoreCase(namePart))
                                .findFirst()
                                .orElse(null);
                    } catch (NumberFormatException e) {
                        return allCustomers.stream()
                                .filter(user -> user.getName().equalsIgnoreCase(namePart))
                                .findFirst()
                                .orElse(null);
                    }
                }
                return allCustomers.stream()
                        .filter(user -> user.getName().equalsIgnoreCase(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        restaurantComboBox.setCellFactory(lv -> new ListCell<RestaurantResponse>() {
            @Override
            protected void updateItem(RestaurantResponse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName());
            }
        });
        restaurantComboBox.setButtonCell(new ListCell<RestaurantResponse>() {
            @Override
            protected void updateItem(RestaurantResponse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "انتخاب رستوران" : item.getName());
            }
        });
        restaurantComboBox.setConverter(new StringConverter<RestaurantResponse>() {
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
                        return allRestaurants.stream()
                                .filter(r -> r.getId().equals(id) && r.getName().equalsIgnoreCase(namePart))
                                .findFirst()
                                .orElse(null);
                    } catch (NumberFormatException e) {
                        return allRestaurants.stream()
                                .filter(restaurant -> restaurant.getName().equalsIgnoreCase(namePart))
                                .findFirst()
                                .orElse(null);
                    }
                }
                return allRestaurants.stream()
                        .filter(restaurant -> restaurant.getName().equalsIgnoreCase(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        courierComboBox.setCellFactory(lv -> new ListCell<UserResponse>() {
            @Override
            protected void updateItem(UserResponse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });

        courierComboBox.setButtonCell(new ListCell<UserResponse>() {
            @Override
            protected void updateItem(UserResponse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        courierComboBox.setConverter(new StringConverter<UserResponse>() {
            @Override
            public String toString(UserResponse user) {
                return user == null ? "" : user.getName();
            }

            @Override
            public UserResponse fromString(String string) {
                if (string == null || string.trim().isEmpty()) {
                    return null;
                }
                return allCouriers.stream()
                        .filter(user -> user.getName().equalsIgnoreCase(string))
                        .findFirst()
                        .orElse(null);
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
            UserResponse selected = comboBox.getSelectionModel().getSelectedItem();
            if (selected != null && selected.getName().equalsIgnoreCase(newValue)) {
                return;
            }

            if (newValue == null || newValue.isEmpty()) {
                comboBox.setItems(originalItems);
                comboBox.getSelectionModel().clearSelection();
                return;
            }

            comboBox.hide();
            ObservableList<UserResponse> filteredItems = originalItems.stream()
                    .filter(user -> user.getName().toLowerCase().contains(newValue.toLowerCase()) ||
                            String.valueOf(user.getId()).contains(newValue))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));

            comboBox.setItems(filteredItems);

            comboBox.show();
        });

        comboBox.setPromptText("انتخاب مشتری یا پیک");
        comboBox.setEditable(true);
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

    private void loadOrders(String search, String restaurant, String customer, String courier, String status) {
        if (token == null || token.isEmpty()) {
            showAlert("خطای احراز هویت", "ادمین وارد نشده است. لطفا ابتدا وارد شوید.", Alert.AlertType.ERROR, null);
            navigateToSignInView(ordersTableView);
            return;
        }

        adminDataService.getAllOrders(token, search, restaurant, customer, courier, status)
                .thenAccept(orders -> Platform.runLater(() -> {
                    if (orders != null && !orders.isEmpty()) {
                        orders.sort(Comparator.comparing(OrderResponse::getId));

                        ordersTableView.setItems(FXCollections.observableArrayList(orders));
                        ordersTableView.sort();
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
        String search = searchTextField.getText();
        UserResponse selectedCustomer = customerComboBox.getSelectionModel().getSelectedItem();
        String customerName = selectedCustomer != null ? selectedCustomer.getName() : null;

        RestaurantResponse selectedRestaurant = restaurantComboBox.getSelectionModel().getSelectedItem();
        String restaurantName = selectedRestaurant != null ? selectedRestaurant.getName() : null;

        UserResponse selectedCourier = courierComboBox.getSelectionModel().getSelectedItem();
        String courierName = selectedCourier != null ? selectedCourier.getName() : null;

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
                "/com/aut/shoomal/views/MainView.fxml",
                "/com/aut/shoomal/styles/MainView.css",
                TransitionType.SLIDE_LEFT,
                (MainController controller) -> {
                    controller.setLoggedInUser(loggedInUser);
                }
        );
    }
}