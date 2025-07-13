package com.aut.shoomal.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import java.net.URL;
import java.util.ResourceBundle;
import com.aut.shoomal.dto.response.UserResponse;
import com.aut.shoomal.utils.PreferencesManager;

public class MainController extends AbstractBaseController {

    @FXML private Label welcomeUserLabel;
    @FXML private Button logoutButton;
    @FXML private StackPane contentStackPane;

    @FXML private ScrollPane buyerDashboardScrollPane;
    @FXML private ScrollPane sellerDashboardScrollPane;
    @FXML private ScrollPane courierDashboardScrollPane;
    @FXML private ScrollPane adminDashboardScrollPane;
    @FXML private VBox defaultView;

    private UserResponse currentUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        hideAllDashboards();
        if (defaultView != null) {
            defaultView.setVisible(true);
            defaultView.setManaged(true);
        }

        if (logoutButton != null) {
            logoutButton.setOnAction(event -> navigateToSignInView(logoutButton, TransitionType.SLIDE_UP));
        }
    }

    public void setLoggedInUser(UserResponse user) {
        this.currentUser = user;
        if (user != null) {
            if (welcomeUserLabel != null) {
                welcomeUserLabel.setText("Welcome, " + user.getName() + "!");
            }
            displayDashboardForRole(user.getRole());
        } else {
            if (welcomeUserLabel != null) {
                welcomeUserLabel.setText("Welcome, Guest!");
            }
            displayDashboardForRole(null);
        }
    }

    private void hideAllDashboards() {
        if (buyerDashboardScrollPane != null) {
            buyerDashboardScrollPane.setVisible(false);
            buyerDashboardScrollPane.setManaged(false);
        }
        if (sellerDashboardScrollPane != null) {
            sellerDashboardScrollPane.setVisible(false);
            sellerDashboardScrollPane.setManaged(false);
        }
        if (courierDashboardScrollPane != null) {
            courierDashboardScrollPane.setVisible(false);
            courierDashboardScrollPane.setManaged(false);
        }
        if (adminDashboardScrollPane != null) {
            adminDashboardScrollPane.setVisible(false);
            adminDashboardScrollPane.setManaged(false);
        }
        if (defaultView != null) {
            defaultView.setVisible(false);
            defaultView.setManaged(false);
        }
    }

    private void displayDashboardForRole(String role) {
        hideAllDashboards();

        ScrollPane targetPane = null;
        switch (role != null ? role.toLowerCase() : "") {
            case "buyer":
                targetPane = buyerDashboardScrollPane;
                break;
            case "seller":
                targetPane = sellerDashboardScrollPane;
                break;
            case "courier":
                targetPane = courierDashboardScrollPane;
                break;
            case "admin":
                targetPane = adminDashboardScrollPane;
                break;
            default:
                if (defaultView != null) {
                    defaultView.setVisible(true);
                    defaultView.setManaged(true);
                }
                break;
        }

        if (targetPane != null) {
            targetPane.setVisible(true);
            targetPane.setManaged(true);
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        System.out.println("Logout button clicked!");
        PreferencesManager.clearAuthInfo();
        if (logoutButton != null) {
            navigateToSignInView(logoutButton);
        }
    }
}