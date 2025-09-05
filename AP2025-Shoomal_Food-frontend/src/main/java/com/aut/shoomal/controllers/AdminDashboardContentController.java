package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.response.OrderResponse;
import com.aut.shoomal.dto.response.TransactionResponse;
import com.aut.shoomal.dto.response.UserResponse;
import com.aut.shoomal.exceptions.FrontendServiceException;
import com.aut.shoomal.service.AdminDataService;
import com.aut.shoomal.utils.PreferencesManager;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public class AdminDashboardContentController extends AbstractBaseController {

    @FXML private VBox userChartPane;
    @FXML private VBox orderChartPane;
    @FXML private VBox transactionChartPane;

    private AdminDataService adminDataService;
    private String token;
    private UserResponse loggedInUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        adminDataService = new AdminDataService();
        token = PreferencesManager.getJwtToken();
        loadDashboardCharts();
    }

    public void setLoggedInUser(UserResponse user) {
        this.loggedInUser = user;
    }

    private void loadDashboardCharts() {
        if (token == null || token.isEmpty()) {
            showAlert("Authentication Error", "Admin not logged in. Please log in first.", Alert.AlertType.ERROR, null);
            return;
        }

        adminDataService.getAllUsers(token)
                .thenAccept(users -> Platform.runLater(() -> {
                    if (users != null && !users.isEmpty()) {
                        Map<String, Long> userCounts = users.stream()
                                .collect(Collectors.groupingBy(UserResponse::getRole, Collectors.counting()));
                        PieChart userRoleChart = new PieChart();
                        userRoleChart.setTitle("تعداد کاربران بر اساس نقش");
                        userCounts.forEach((role, count) -> {
                            PieChart.Data data = new PieChart.Data(role, count);
                            userRoleChart.getData().add(data);
                            data.nameProperty().bind(
                                    Bindings.format("%s (%.0f)", role, data.pieValueProperty())
                            );
                        });
                        if (userChartPane != null) {
                            userChartPane.getChildren().clear();
                            userChartPane.getChildren().add(userRoleChart);
                            userRoleChart.getStyleClass().add("chart-style");
                        }
                    } else {
                        System.out.println("User data is null or empty. No users to display in Pie Chart.");
                    }
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        if (e.getCause() instanceof FrontendServiceException fsException)
                            showAlert(fsException);
                        else
                            showAlert("Error", "Failed to load user role chart: " + e.getMessage(), Alert.AlertType.ERROR, null);
                    });
                    return null;
                });

        adminDataService.getAllOrders(token)
                .thenAccept(orders -> Platform.runLater(() -> {
                    if (orders != null && !orders.isEmpty()) {
                        Map<String, Long> orderCounts = orders.stream()
                                .collect(Collectors.groupingBy(OrderResponse::getStatus, Collectors.counting()));

                        CategoryAxis xAxis = new CategoryAxis();
                        NumberAxis yAxis = new NumberAxis();
                        yAxis.setTickUnit(1.0);
                        yAxis.setLowerBound(0);
                        yAxis.setMinorTickVisible(false);
                        yAxis.setTickLabelFormatter(new StringConverter<>() {
                            @Override
                            public String toString(Number object) {
                                if (object.doubleValue() == object.longValue()) {
                                    return String.format("%.0f", object.doubleValue());
                                }
                                return "";
                            }

                            @Override
                            public Number fromString(String string) {
                                return Double.parseDouble(string);
                            }
                        });


                        BarChart<String, Number> orderStatusChart = new BarChart<>(xAxis, yAxis);
                        orderStatusChart.setTitle("تعداد سفارشات بر اساس وضعیت");
                        xAxis.setLabel("وضعیت سفارش");
                        yAxis.setLabel("تعداد");

                        XYChart.Series<String, Number> series = new XYChart.Series<>();
                        orderCounts.forEach((status, count) -> series.getData().add(new XYChart.Data<>(status, count)));
                        orderStatusChart.getData().add(series);
                        orderStatusChart.setLegendVisible(false);

                        if (orderChartPane != null) {
                            orderChartPane.getChildren().clear();
                            orderChartPane.getChildren().add(orderStatusChart);
                            orderStatusChart.getStyleClass().add("chart-style");
                        }
                    }
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        if (e.getCause() instanceof FrontendServiceException fsException)
                            showAlert(fsException);
                        else
                            showAlert("Error", "Failed to load order status chart: " + e.getMessage(), Alert.AlertType.ERROR, null);
                    });
                    return null;
                });

        adminDataService.getAllTransactions(token)
                .thenAccept(transactions -> Platform.runLater(() -> {
                    if (transactions != null && !transactions.isEmpty()) {
                        Map<String, Long> transactionCounts = transactions.stream()
                                .collect(Collectors.groupingBy(TransactionResponse::getStatus, Collectors.counting()));

                        CategoryAxis xAxis = new CategoryAxis();
                        NumberAxis yAxis = new NumberAxis();
                        yAxis.setTickUnit(1.0);
                        yAxis.setLowerBound(0);
                        yAxis.setMinorTickVisible(false);
                        yAxis.setTickLabelFormatter(new StringConverter<>() {
                            @Override
                            public String toString(Number object) {
                                if (object.doubleValue() == object.longValue()) {
                                    return String.format("%.0f", object.doubleValue());
                                }
                                return "";
                            }

                            @Override
                            public Number fromString(String string) {
                                return Double.parseDouble(string);
                            }
                        });

                        BarChart<String, Number> transactionStatusChart = new BarChart<>(xAxis, yAxis);
                        transactionStatusChart.setTitle("تعداد تراکنش‌ها بر اساس وضعیت");
                        xAxis.setLabel("وضعیت تراکنش");
                        yAxis.setLabel("تعداد");

                        XYChart.Series<String, Number> series = new XYChart.Series<>();
                        transactionCounts.forEach((status, count) -> series.getData().add(new XYChart.Data<>(status, count)));
                        transactionStatusChart.getData().add(series);
                        transactionStatusChart.setLegendVisible(false);

                        if (transactionChartPane != null) {
                            transactionChartPane.getChildren().clear();
                            transactionChartPane.getChildren().add(transactionStatusChart);
                            transactionStatusChart.getStyleClass().add("chart-style");
                        }
                    }
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        if (e.getCause() instanceof FrontendServiceException fsException)
                            showAlert(fsException);
                        else
                            showAlert("Error", "Failed to load transaction status chart: " + e.getMessage(), Alert.AlertType.ERROR, null);
                    });
                    return null;
                });
    }

    @FXML
    private void handleViewAllOrders() {
        navigateTo(
                orderChartPane,
                "/com/aut/shoomal/views/AdminOrdersView.fxml",
                "/com/aut/shoomal/styles/AdminDashboardStyles.css",
                TransitionType.SLIDE_RIGHT,
                (AdminOrdersController controller) -> controller.setLoggedInUser(loggedInUser)
        );
    }

    @FXML
    private void handleViewTransactions() {
        navigateTo(
                transactionChartPane,
                "/com/aut/shoomal/views/AdminTransactionsView.fxml",
                "/com/aut/shoomal/styles/AdminDashboardStyles.css",
                TransitionType.SLIDE_RIGHT,
                (AdminTransactionsController controller) -> controller.setLoggedInUser(loggedInUser)
        );
    }

    @FXML
    public void handleListAllCoupons(javafx.event.ActionEvent actionEvent) {
        navigateTo(
                (MenuItem) actionEvent.getSource(),
                "/com/aut/shoomal/views/CouponListView.fxml",
                "/com/aut/shoomal/styles/AdminDashboardStyles.css",
                TransitionType.SLIDE_RIGHT,
                (CouponListController controller) -> controller.setLoggedInUser(this.loggedInUser)
        );
    }

    @FXML
    public void handleCreateNewCoupon(javafx.event.ActionEvent actionEvent) {
        navigateTo(
                (MenuItem) actionEvent.getSource(),
                "/com/aut/shoomal/views/CreateEditCouponView.fxml",
                "/com/aut/shoomal/styles/AdminDashboardStyles.css",
                TransitionType.SLIDE_RIGHT,
                (CreateEditCouponController controller) -> controller.setLoggedInUser(this.loggedInUser)
        );
    }

    public void handleManageUserStatus(javafx.event.ActionEvent actionEvent) {
        navigateTo(
                (MenuItem) actionEvent.getSource(),
                "/com/aut/shoomal/views/UserManagementView.fxml",
                "/com/aut/shoomal/styles/AdminDashboardStyles.css",
                TransitionType.SLIDE_RIGHT,
                (UserManagementController controller) -> controller.setLoggedInUser(this.loggedInUser)
        );
    }
}