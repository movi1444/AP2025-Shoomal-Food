package com.aut.shoomal.controllers;

import com.aut.shoomal.service.UserService;
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

public class OrderFilterController extends AbstractBaseController
{
    @FXML private TextField searchField;
    @FXML private ChoiceBox<String> userChoiceBox;
    @FXML private ChoiceBox<String> courierChoiceBox;
    @FXML private ChoiceBox<String> statusChoiceBox;
    @FXML private Button applyFilterButton;
    @FXML private Button cancelButton;

    private String token;
    private UserService userService;
    private Integer restaurantId;

    private String initialSearch;
    private String initialUser;
    private String initialCourier;
    private String initialStatus;

    private FilterCallback filterCallback;
    public interface FilterCallback
    {
        void onFilterApplied(String search, String user, String courier, String status);
    }

    public void setFilterCallback(FilterCallback callback)
    {
        this.filterCallback = callback;
    }

    public void setInitialFilters(String search, String user, String courier, String status, Integer restaurantId)
    {
        this.initialSearch = search;
        this.initialUser = user;
        this.initialCourier = courier;
        this.initialStatus = status;
        this.restaurantId = restaurantId;

        if (search != null)
            searchField.setText(search);
        loadUsersAndCouriers();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        super.initialize(url, resourceBundle);
        userService = new UserService();
        token = PreferencesManager.getJwtToken();
        addTextDirectionListener(searchField);
        addChoiceBoxDirectionListener(userChoiceBox);
        addChoiceBoxDirectionListener(courierChoiceBox);
        addChoiceBoxDirectionListener(statusChoiceBox);

        ObservableList<String> statuses = FXCollections.observableArrayList(
                "All", "submitted", "unpaid and cancelled", "waiting vendor", "cancelled", "finding courier", "on the way", "completed"
        );
        statusChoiceBox.setItems(statuses);
        statusChoiceBox.getSelectionModel().selectFirst();
    }

    private void loadUsersAndCouriers()
    {
        handleError();

        CompletableFuture<List<String>> usersFuture = userService.getCustomersWithOrder(token, restaurantId)
                .exceptionally(e -> {
                    Platform.runLater(() -> showAlert("Error", "Failed to load customers: " + e.getMessage(), Alert.AlertType.ERROR, null));
                    return null;
                });
        CompletableFuture<List<String>> couriersFuture = userService.getCouriersWithOrder(token, restaurantId)
                .exceptionally(e -> {
                    Platform.runLater(() -> showAlert("Error", "Failed to load couriers: " + e.getMessage(), Alert.AlertType.ERROR, null));
                    return null;
                });

        CompletableFuture.allOf(usersFuture, couriersFuture)
                .thenAccept(v -> Platform.runLater(() -> {
                    try {
                        ObservableList<String> userNames = FXCollections.observableArrayList("All");
                        List<String> users = usersFuture.get();
                        if (users != null)
                            userNames.addAll(users.stream().toList());
                        userChoiceBox.setItems(userNames);
                        userChoiceBox.getSelectionModel().select(initialUser != null ? initialUser : "All");

                        ObservableList<String> courierNames = FXCollections.observableArrayList("All");
                        List<String> couriers = couriersFuture.get();
                        if (couriers != null)
                            courierNames.addAll(couriers.stream().toList());
                        courierChoiceBox.setItems(courierNames);
                        courierChoiceBox.getSelectionModel().select(initialCourier != null ? initialCourier : "All");
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
        String search = searchField.getText().trim();
        String user = userChoiceBox.getValue();
        String courier = courierChoiceBox.getValue();
        String status = statusChoiceBox.getValue();

        if ("All".equals(user))
            user = null;
        if ("All".equals(courier))
            courier = null;
        if ("All".equals(status))
            status = null;
        if (search.isEmpty())
            search = null;


        if (filterCallback != null)
            filterCallback.onFilterApplied(search, user, courier, status);
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