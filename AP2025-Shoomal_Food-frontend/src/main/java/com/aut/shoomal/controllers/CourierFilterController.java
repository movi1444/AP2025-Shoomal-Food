package com.aut.shoomal.controllers;

import com.aut.shoomal.service.CourierService;
import com.aut.shoomal.utils.PreferencesManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class CourierFilterController extends AbstractBaseController
{
    @FXML private TextField searchField;
    @FXML private ChoiceBox<String> vendorChoiceBox;
    @FXML private Button applyFilterButton;
    @FXML private Button cancelButton;

    private String token;
    private CourierService courierService;

    private String initialSearch;
    private String initialVendor;
    private String initialUser;

    private FilterCallback filterCallback;
    public interface FilterCallback
    {
        void onFilterApplied(String search, String vendor);
    }

    public void setFilterCallback(FilterCallback callback)
    {
        this.filterCallback = callback;
    }

    public void setInitialFilters(String search, String vendor)
    {
        this.initialSearch = search;
        this.initialVendor = vendor;
        if (search != null)
            searchField.setText(search);
        loadVendors();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        super.initialize(url, resourceBundle);
        token = PreferencesManager.getJwtToken();
        courierService = new CourierService();
        addTextDirectionListener(searchField);
        addChoiceBoxDirectionListener(vendorChoiceBox);
    }

    private void loadVendors()
    {
        handleError();

        CompletableFuture<List<String>> vendorsFuture = courierService.getVendorName(token)
                .exceptionally(e -> {
                    Platform.runLater(() -> showAlert("Error", "Failed to load vendors: " + e.getMessage(), Alert.AlertType.ERROR, null));
                    return null;
                });

        CompletableFuture.allOf(vendorsFuture)
                .thenAccept(v -> Platform.runLater(() -> {
                    try {
                        ObservableList<String> vendorNames = FXCollections.observableArrayList("All");
                        List<String> vendors = vendorsFuture.get();
                        if (vendors != null)
                            vendorNames.addAll(vendors);
                        vendorChoiceBox.setItems(vendorNames);
                        vendorChoiceBox.getSelectionModel().select(initialVendor != null ? initialVendor : "All");
                    } catch (Exception e) {
                        showAlert("Error", "Failed to process user/courier data: " + e.getMessage(), Alert.AlertType.ERROR, null);
                    }
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> showAlert("Unexpected Error", "An error occurred during filter data load: " + e.getMessage(), Alert.AlertType.ERROR, null));
                    return null;
                });
    }

    @FXML
    public void handleApplyFilter(ActionEvent actionEvent)
    {
        String search = searchField.getText();
        String vendor = vendorChoiceBox.getValue();
        if (search.isEmpty())
            search = null;
        if (vendor.equals("All"))
            vendor = null;

        if (filterCallback != null)
            filterCallback.onFilterApplied(search, vendor);
        closeWindow();
    }

    @FXML
    public void handleCancel(ActionEvent actionEvent)
    {
        closeWindow();
    }

    private void closeWindow()
    {
        Stage stage = (Stage) applyFilterButton.getScene().getWindow();
        stage.close();
    }

    private void handleError()
    {
        if (token == null || token.isEmpty())
        {
            showAlert("Authentication Error", "User not logged in. Please log in first.", Alert.AlertType.ERROR, null);
            navigateToSignInView(cancelButton);
        }
    }
}