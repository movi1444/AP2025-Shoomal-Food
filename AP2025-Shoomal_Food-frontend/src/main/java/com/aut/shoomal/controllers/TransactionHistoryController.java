package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.response.TransactionResponse;
import com.aut.shoomal.exceptions.FrontendServiceException;
import com.aut.shoomal.service.TransactionService;
import com.aut.shoomal.utils.PreferencesManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;


public class TransactionHistoryController extends AbstractBaseController
{
    @FXML private TableView<TransactionResponse> transactionTable;
    @FXML private TableColumn<TransactionResponse, Integer> userIdColumn;
    @FXML private TableColumn<TransactionResponse, String> methodColumn;
    @FXML private TableColumn<TransactionResponse, String> statusColumn;
    @FXML private TableColumn<TransactionResponse, Integer> orderIdColumn;
    //amount and date

    @FXML private Button backButton;

    private TransactionService transactionService;
    private String token;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        transactionService = new TransactionService();
        token = PreferencesManager.getJwtToken();
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        methodColumn.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));

        loadTransactionHistory();
    }

    private void loadTransactionHistory()
    {
        if (token == null || token.isEmpty())
        {
            showAlert("Authentication Error", "User not logged in. Please log in first.", Alert.AlertType.ERROR, null);
            navigateToSignInView(backButton);
            return;
        }

        transactionService.getTransactions(token)
                .thenAccept(transactions -> Platform.runLater(() -> {
                    if (transactions != null)
                    {
                        ObservableList<TransactionResponse> transactionList = FXCollections.observableArrayList(transactions);
                        transactionTable.setItems(transactionList);
                    }
                    else
                        showAlert("Error", "Failed to load transaction history.", Alert.AlertType.ERROR, null);
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
        navigateToUserProfileView(actionEvent.getSource());
    }
}