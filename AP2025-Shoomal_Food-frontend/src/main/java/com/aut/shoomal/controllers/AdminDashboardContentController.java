package com.aut.shoomal.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminDashboardContentController extends AbstractBaseController {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        System.out.println("AdminDashboardContentController initialized.");
    }

    @FXML
    private void handleViewAllUsers() {
        System.out.println("View All Users clicked!");
        showAlert("Admin Action", "Displaying all users. (Implementation needed)", Alert.AlertType.INFORMATION, null);
    }

    @FXML
    private void handleManageUserStatus() {
        System.out.println("Manage User Status clicked!");
        showAlert("Admin Action", "Managing user approval status. (Implementation needed)", Alert.AlertType.INFORMATION, null);
    }

    @FXML
    private void handleViewAllOrders() {
        System.out.println("View All Orders clicked!");
        showAlert("Admin Action", "Displaying all orders. (Implementation needed)", Alert.AlertType.INFORMATION, null);
    }

    @FXML
    private void handleViewTransactions() {
        System.out.println("View Transactions clicked!");
        showAlert("Admin Action", "Displaying financial transactions. (Implementation needed)", Alert.AlertType.INFORMATION, null);
    }

    @FXML
    private void handleListAllCoupons() {
        System.out.println("List All Coupons clicked!");
        showAlert("Admin Action", "Displaying all coupons. (Implementation needed)", Alert.AlertType.INFORMATION, null);
    }

    @FXML
    private void handleCreateNewCoupon() {
        System.out.println("Create New Coupon clicked!");
        showAlert("Admin Action", "Form for creating a new coupon. (Implementation needed)", Alert.AlertType.INFORMATION, null);
    }

    @FXML
    private void handleManageCoupons() {
        System.out.println("Manage Coupons clicked!");
        showAlert("Admin Action", "Managing existing coupons (update/delete). (Implementation needed)", Alert.AlertType.INFORMATION, null);
    }
}