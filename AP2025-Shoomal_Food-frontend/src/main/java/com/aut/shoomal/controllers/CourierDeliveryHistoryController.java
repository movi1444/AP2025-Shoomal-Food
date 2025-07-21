package com.aut.shoomal.controllers;

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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class CourierDeliveryHistoryController extends AbstractBaseController
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
    @FXML private Button addFilterButton;
    @FXML private  Button backButton;

    private CourierService courierService;
    private String token;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        super.initialize(url, resourceBundle);
        courierService = new CourierService();
        token = PreferencesManager.getJwtToken();
        setColumnsValueFactory();
        loadDeliveryHistory(null, null, null);
    }

    public void loadDeliveryHistory(String search, String vendor, String user)
    {
        if (token == null || token.isEmpty())
        {
            showAlert("Authentication Error", "User not logged in. Please log in first.", Alert.AlertType.ERROR, null);
            navigateToSignInView(backButton);
            return;
        }

        courierService.getDeliveryHistory(token, search, vendor, user)
                .thenAccept(orders -> Platform.runLater(() -> {
                    if (orders != null)
                    {
                        ObservableList<OrderResponse> ordersList = FXCollections.observableArrayList(orders);
                        orderTable.setItems(ordersList);
                    }
                    else
                        showAlert("Error", "Cannot load delivery history.", Alert.AlertType.ERROR, null);
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

    @FXML
    public void handleAddFilter(ActionEvent actionEvent)
    {

    }

    @FXML
    public void handleBackButton(ActionEvent actionEvent)
    {
        navigateToMainView((Node) actionEvent.getSource());
    }
}