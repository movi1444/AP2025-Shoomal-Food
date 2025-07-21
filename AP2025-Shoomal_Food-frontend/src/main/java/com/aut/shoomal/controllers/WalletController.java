package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.request.WalletRequest;
import com.aut.shoomal.exceptions.FrontendServiceException;
import com.aut.shoomal.service.TransactionService;
import com.aut.shoomal.utils.PreferencesManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

public class WalletController extends AbstractBaseController
{

    @FXML private Label walletAmountLabel;
    @FXML private TextField amountField;
    @FXML private Button saveButton;
    @FXML private Button backButton;

    private String token;
    private TransactionService transactionService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        super.initialize(url, resourceBundle);
        token = PreferencesManager.getJwtToken();
        transactionService = new TransactionService();
        addTextDirectionListener(amountField);
        loadWalletAmount();
    }

    private void loadWalletAmount()
    {
        if (token == null || token.isEmpty())
        {
            showAlert("Authentication Error", "User not logged in. Please log in first.", Alert.AlertType.ERROR, null);
            navigateTo(saveButton, "/com/aut/shoomal/views/SignInView.fxml", "/com/aut/shoomal/styles/SignInUpStyles.css", TransitionType.SLIDE_LEFT);
            return;
        }

        transactionService.getWalletAmount(token)
                .thenAccept(amount -> Platform.runLater(() -> {
                   if (amount != null)
                       walletAmountLabel.setText("موجودی:     " + amount);
                   else
                       showAlert("Error", "Failed to load wallet amount.", Alert.AlertType.ERROR, null);
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
    public void handleSaveChanges(ActionEvent actionEvent)
    {
        if (token == null || token.isEmpty())
        {
            showAlert("Authentication Error", "User not logged in. Please log in first.", Alert.AlertType.ERROR, null);
            navigateTo(saveButton, "/com/aut/shoomal/views/SignInView.fxml", "/com/aut/shoomal/styles/SignInUpStyles.css", TransitionType.SLIDE_LEFT);
            return;
        }

        WalletRequest request = new WalletRequest();
        double value = 0.0;
        try {
            value = Double.parseDouble(amountField.getText());
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid amount.", Alert.AlertType.ERROR, null);
        }
        if (amountField != null)
            request.setAmount(BigDecimal.valueOf(value));

        transactionService.chargeWallet(token, request)
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.isSuccess())
                    {
                        showAlert("Success", "Wallet charged successfully.", Alert.AlertType.INFORMATION, null);
                        navigateToUserProfileView(actionEvent.getSource());
                    }
                    else
                        showAlert("Error", "Failed to charge wallet.", Alert.AlertType.ERROR, null);
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
    public void handleBackToPreviousPage(ActionEvent actionEvent)
    {
        navigateToUserProfileView(actionEvent.getSource());
    }
}