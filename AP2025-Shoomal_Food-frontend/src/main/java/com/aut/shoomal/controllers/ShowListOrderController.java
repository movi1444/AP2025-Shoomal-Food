package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.request.UpdateOrderStatusRequest;
import com.aut.shoomal.dto.response.OrderResponse;
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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ShowListOrderController extends AbstractBaseController implements OrderFilterController.FilterCallback
{
    @FXML private TableView<OrderResponse> orderTable;
    @FXML private TableColumn<OrderResponse, String> deliveryColumn;
    @FXML private TableColumn<OrderResponse, Integer> idColumn;
    @FXML private TableColumn<OrderResponse, Integer> customerIdColumn;
    @FXML private TableColumn<OrderResponse, Integer> vendorIdColumn;
    @FXML private TableColumn<OrderResponse, Integer> couponIdColumn;
    @FXML private TableColumn<OrderResponse, List<Integer>> itemIdsColumn;
    @FXML private TableColumn<OrderResponse, Integer> rawPriceColumn;
    @FXML private TableColumn<OrderResponse, Integer> taxFeeColumn;
    @FXML private TableColumn<OrderResponse, Integer> additionalFeeColumn;
    @FXML private TableColumn<OrderResponse, Integer> courierFeeColumn;
    @FXML private TableColumn<OrderResponse, Integer> payPriceColumn;
    @FXML private TableColumn<OrderResponse, Integer> courierIdColumn;
    @FXML private TableColumn<OrderResponse, String> statusColumn;
    @FXML private TableColumn<OrderResponse, String> createdAtColumn;
    @FXML private TableColumn<OrderResponse, String> updatedAtColumn;
    @FXML private TableColumn<OrderResponse, Void> actionsColumn;
    @FXML private Button addFilterButton;
    @FXML private  Button backButton;

    private RestaurantService restaurantService;
    private String token;
    private Integer restaurantId;

    private String currentSearch = null;
    private String currentUser = null;
    private String currentCourier = null;
    private String currentStatus = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        super.initialize(url, resourceBundle);
        restaurantService = new RestaurantService();
        token = PreferencesManager.getJwtToken();
        setColumnsValueFactory();
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button statusButton = new Button("ویرایش وضعیت");
            private final ChoiceBox<String> statusChoiceBox = new ChoiceBox<>();
            private final HBox pane = new HBox(5, statusButton, statusChoiceBox);

            private static final ObservableList<String> ALL_STATUSES =
                    FXCollections.observableArrayList("accepted", "rejected");
            private static final ObservableList<String> SERVED_STATUS_ONLY =
                    FXCollections.observableArrayList("served");
            {
                statusButton.getStyleClass().add("table-cell-button");
                statusChoiceBox.getStyleClass().add("table-cell-choice-box");
                statusChoiceBox.setItems(FXCollections.observableArrayList());
                addChoiceBoxDirectionListener(statusChoiceBox);

                statusButton.setOnAction((ActionEvent event) -> {
                   OrderResponse order = getTableView().getItems().get(getIndex());
                   String status = statusChoiceBox.getValue();
                   if (status == null || status.isEmpty())
                   {
                       showAlert("خطا", "لطفا یک وضعیت را برای بروزرسانی سفارش انتخاب کنید.", Alert.AlertType.WARNING, null);
                       return;
                   }
                   handleChangeStatus(order.getId(), status);
                });
            }

            private void handleChangeStatus(Integer orderId, String status)
            {
                handleError();
                if (orderId == null || status == null || status.isEmpty())
                {
                    showAlert("Error", "Cannot change order status.", Alert.AlertType.ERROR, null);
                    return;
                }

                UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
                request.setStatus(status);
                restaurantService.changeOrderStatus(token, orderId, request)
                        .thenAccept(response -> Platform.runLater(() -> {
                            if (response.isSuccess())
                            {
                                showAlert("Success", "Status updated successfully.", Alert.AlertType.INFORMATION, null);
                                loadOrders(null, null, null, null);
                            }
                            else
                                showAlert("Error", "Status update failed.", Alert.AlertType.ERROR, null);
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

            @Override
            protected void updateItem(Void item, boolean empty)
            {
                super.updateItem(item, empty);
                if (empty)
                    setGraphic(null);
                else
                {
                    OrderResponse order = getTableView().getItems().get(getIndex());
                    if (order != null)
                    {
                        String currentStatus = order.getStatus();
                        if (currentStatus.equalsIgnoreCase("submitted"))
                            statusChoiceBox.setItems(ALL_STATUSES);
                        else if (currentStatus.equalsIgnoreCase("waiting vendor"))
                            statusChoiceBox.setItems(SERVED_STATUS_ONLY);
                    }
                    setGraphic(pane);
                }
            }
        });
    }

    private void loadOrders(String status, String search, String user, String courier)
    {
        handleError();

        restaurantService.getOrderList(token, restaurantId, status, search, user, courier)
                .thenAccept(orders -> Platform.runLater(() -> {
                    if (orders != null)
                    {
                        ObservableList<OrderResponse> ordersList = FXCollections.observableArrayList(orders);
                        orderTable.setItems(ordersList);
                    }
                    else
                        showAlert("Error", "Cannot load order list.", Alert.AlertType.ERROR, null);
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
    public void handleAddFilter(ActionEvent actionEvent)
    {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aut/shoomal/views/OrderFilterView.fxml"));
            Parent root = loader.load();

            OrderFilterController filterController = loader.getController();
            filterController.setFilterCallback(this);
            filterController.setInitialFilters(currentSearch, currentUser, currentCourier, currentStatus, restaurantId);

            Stage filterStage = new Stage();
            filterStage.initModality(Modality.APPLICATION_MODAL);
            filterStage.setTitle("فیلتر سفارشات");
            filterStage.setScene(new Scene(root));
            filterStage.showAndWait();
        } catch (Exception e) {
            showAlert("خطا در باز کردن فیلتر", "مشکلی در بارگذاری صفحه فیلتر پیش آمد: " + e.getMessage(), Alert.AlertType.ERROR, null);
            e.printStackTrace();
        }
    }

    @FXML
    public void handleBackButton(ActionEvent actionEvent)
    {
        navigateToMainView((Node) actionEvent.getSource());
    }

    private void setColumnsValueFactory()
    {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        deliveryColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryAddress"));
        customerIdColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        vendorIdColumn.setCellValueFactory(new PropertyValueFactory<>("vendorId"));
        couponIdColumn.setCellValueFactory(new PropertyValueFactory<>("couponId"));
        itemIdsColumn.setCellValueFactory(new PropertyValueFactory<>("items"));
        rawPriceColumn.setCellValueFactory(new PropertyValueFactory<>("rawPrice"));
        taxFeeColumn.setCellValueFactory(new PropertyValueFactory<>("taxFee"));
        additionalFeeColumn.setCellValueFactory(new PropertyValueFactory<>("additionalFee"));
        courierFeeColumn.setCellValueFactory(new PropertyValueFactory<>("courierFee"));
        payPriceColumn.setCellValueFactory(new PropertyValueFactory<>("payPrice"));
        courierIdColumn.setCellValueFactory(new PropertyValueFactory<>("courierId"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        updatedAtColumn.setCellValueFactory(new PropertyValueFactory<>("updatedAt"));
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
        loadOrders(currentStatus, currentSearch, currentUser, currentCourier);
    }

    @Override
    public void onFilterApplied(String search, String user, String courier, String status)
    {
        this.currentSearch = search;
        this.currentUser = user;
        this.currentCourier = courier;
        this.currentStatus = status;

        loadOrders(this.currentStatus, this.currentSearch, this.currentUser, this.currentCourier);
    }
}