package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.request.UpdateDeliveryStatusRequest;
import com.aut.shoomal.dto.response.OrderResponse;
import com.aut.shoomal.exceptions.FrontendServiceException;
import com.aut.shoomal.service.CourierService;
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
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class CourierAvailableController extends AbstractBaseController
{
    @FXML
    private TableView<OrderResponse> orderTable;
    @FXML private TableColumn<OrderResponse, String> deliveryColumn;
    @FXML private TableColumn<OrderResponse, Integer> idColumn;
    @FXML private TableColumn<OrderResponse, String> customerNameColumn;
    @FXML private TableColumn<OrderResponse, String> vendorNameColumn;
    @FXML private TableColumn<OrderResponse, Integer> couponIdColumn;
    @FXML private TableColumn<OrderResponse, List<String>> itemNamesColumn;
    @FXML private TableColumn<OrderResponse, Integer> rawPriceColumn;
    @FXML private TableColumn<OrderResponse, Integer> taxFeeColumn;
    @FXML private TableColumn<OrderResponse, Integer> additionalFeeColumn;
    @FXML private TableColumn<OrderResponse, Integer> courierFeeColumn;
    @FXML private TableColumn<OrderResponse, Integer> payPriceColumn;
    @FXML private TableColumn<OrderResponse, String> courierNameColumn;
    @FXML private TableColumn<OrderResponse, String> statusColumn;
    @FXML private TableColumn<OrderResponse, String> createdAtColumn;
    @FXML private TableColumn<OrderResponse, String> updatedAtColumn;
    @FXML private TableColumn<OrderResponse, Void> actionsColumn;
    @FXML private Button backButton;

    private CourierService courierService;
    private String token;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        super.initialize(url, resourceBundle);
        courierService = new CourierService();
        token = PreferencesManager.getJwtToken();
        setColumnsValueFactory();

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button statusButton = new Button("ویرایش وضعیت");
            private final ChoiceBox<String> statusChoiceBox = new ChoiceBox<>();
            private final HBox pane = new HBox(5, statusButton, statusChoiceBox);
            private static final ObservableList<String> FIRST =
                    FXCollections.observableArrayList("accepted");
            private static final ObservableList<String> SECOND =
                    FXCollections.observableArrayList("received", "delivered");

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
                if (token == null || token.isEmpty())
                {
                    showAlert("Authentication Error", "User not logged in. Please log in first.", Alert.AlertType.ERROR, null);
                    navigateToSignInView(backButton);
                    return;
                }
                if (orderId == null || status == null || status.isEmpty())
                {
                    showAlert("Error", "Cannot change order status.", Alert.AlertType.ERROR, null);
                    return;
                }

                UpdateDeliveryStatusRequest request = new UpdateDeliveryStatusRequest();
                request.setStatus(status);
                courierService.changeDeliveryStatus(token, orderId, request)
                        .thenAccept(response -> Platform.runLater(() -> {
                            if (response != null)
                            {
                                showAlert("Success", "Status changed successfully.", Alert.AlertType.INFORMATION, null);
                                loadAvailableOrders();
                            }
                            else
                                showAlert("Error", "Change status failed.", Alert.AlertType.ERROR, null);
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
                else {
                    OrderResponse order = getTableView().getItems().get(getIndex());
                    if (order != null)
                    {
                        String currentStatus = order.getStatus();
                        if (currentStatus.equalsIgnoreCase("finding courier"))
                            statusChoiceBox.setItems(FIRST);
                        else if (currentStatus.equalsIgnoreCase("on the way"))
                            statusChoiceBox.setItems(SECOND);
                    }
                    setGraphic(pane);
                }
            }
        });
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
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        vendorNameColumn.setCellValueFactory(new PropertyValueFactory<>("vendorName"));
        couponIdColumn.setCellValueFactory(new PropertyValueFactory<>("couponId"));
        itemNamesColumn.setCellValueFactory(new PropertyValueFactory<>("items"));
        rawPriceColumn.setCellValueFactory(new PropertyValueFactory<>("rawPrice"));
        taxFeeColumn.setCellValueFactory(new PropertyValueFactory<>("taxFee"));
        additionalFeeColumn.setCellValueFactory(new PropertyValueFactory<>("additionalFee"));
        courierFeeColumn.setCellValueFactory(new PropertyValueFactory<>("courierFee"));
        payPriceColumn.setCellValueFactory(new PropertyValueFactory<>("payPrice"));
        courierNameColumn.setCellValueFactory(new PropertyValueFactory<>("courierName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        updatedAtColumn.setCellValueFactory(new PropertyValueFactory<>("updatedAt"));
    }

    public void loadAvailableOrders()
    {
        if (token == null || token.isEmpty())
        {
            showAlert("Authentication Error", "User not logged in. Please log in first.", Alert.AlertType.ERROR, null);
            navigateToSignInView(backButton);
            return;
        }

        courierService.getAvailableOrders(token)
                .thenAccept(orders -> Platform.runLater(() -> {
                    if (orders != null)
                    {
                        ObservableList<OrderResponse> ordersList = FXCollections.observableArrayList(orders);
                        ordersList.sort(Comparator.comparing(OrderResponse::getId));
                        orderTable.setItems(ordersList);
                        orderTable.sort();
                    }
                    else
                        showAlert("Error", "Cannot load available orders.", Alert.AlertType.ERROR, null);
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
}