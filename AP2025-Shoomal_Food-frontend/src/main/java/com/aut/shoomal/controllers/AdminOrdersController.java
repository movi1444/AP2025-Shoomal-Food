package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.response.OrderResponse;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

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
    @FXML private TextField customerSearchField;
    @FXML private TextField vendorSearchField;
    @FXML private TextField courierSearchField;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private VBox filterSidebar;
    @FXML private Button filterButton;
    @FXML private Button backButton;

    private AdminDataService adminDataService;
    private String token;
    private UserResponse loggedInUser;
    private boolean isFilterSidebarVisible = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        adminDataService = new AdminDataService();
        token = PreferencesManager.getJwtToken();
        setupOrdersTable();
        setupStatusFilterComboBox();
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

    private void loadOrders(String search, String vendor, String customer, String courier, String status) {
        if (token == null || token.isEmpty()) {
            showAlert("خطای احراز هویت", "ادمین وارد نشده است. لطفا ابتدا وارد شوید.", Alert.AlertType.ERROR, null);
            navigateToSignInView(ordersTableView);
            return;
        }

        adminDataService.getAllOrders(token, search, vendor, customer, courier, status)
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
        String customer = customerSearchField.getText();
        String vendor = vendorSearchField.getText();
        String courier = courierSearchField.getText();
        String status = statusFilterComboBox.getValue();

        loadOrders(
                search != null && !search.isEmpty() ? search : null,
                vendor != null && !vendor.isEmpty() ? vendor : null,
                customer != null && !customer.isEmpty() ? customer : null,
                courier != null && !courier.isEmpty() ? courier : null,
                status != null && !status.isEmpty() && !status.equals("همه وضعیت‌ها") ? status : null
        );

        handleFilterOrders();
    }

    @FXML
    private void clearOrderFilters() {
        searchTextField.clear();
        customerSearchField.clear();
        vendorSearchField.clear();
        courierSearchField.clear();
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