package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.response.OrderResponse;
import com.aut.shoomal.exceptions.FrontendServiceException;
import com.aut.shoomal.service.OrderService;
import com.aut.shoomal.utils.PreferencesManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BuyerActiveOrdersController extends AbstractBaseController {

    @FXML private VBox ordersContainer;
    @FXML private Button backButton;

    private OrderService orderService;
    private String token;

    private Consumer<Void> closeCallback;

    private static final List<String> INACTIVE_STATUSES = List.of("completed", "cancelled", "unpaid and cancelled");


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        orderService = new OrderService();
        token = PreferencesManager.getJwtToken();

        if (backButton != null) {
            backButton.setOnAction(event -> handleBackButton(event));
        }
    }

    public void setCloseCallback(Consumer<Void> callback) {
        this.closeCallback = callback;
    }

    public void loadActiveOrders() {
        if (token == null || token.isEmpty()) {
            showAlert("خطای احراز هویت", "کاربر وارد نشده است. لطفا ابتدا وارد شوید.", Alert.AlertType.ERROR, null);
            if (closeCallback != null) {
                closeCallback.accept(null);
            }
            return;
        }

        ordersContainer.getChildren().clear();

        orderService.getBuyerOrderHistory(token, null, null)
                .thenAccept(orders -> Platform.runLater(() -> {
                    if (orders != null && !orders.isEmpty()) {
                        List<OrderResponse> activeOrders = orders.stream()
                                .filter(order -> !INACTIVE_STATUSES.contains(order.getStatus().toLowerCase()))
                                .collect(Collectors.toList());

                        if (activeOrders.isEmpty()) {
                            ordersContainer.getChildren().add(createNoOrdersMessage("سفارش فعال یا در حال انجامی یافت نشد."));
                        } else {
                            for (OrderResponse order : activeOrders) {
                                ordersContainer.getChildren().add(createOrderCard(order));
                            }
                        }
                    } else {
                        ordersContainer.getChildren().add(createNoOrdersMessage("سفارش فعال یا در حال انجامی یافت نشد."));
                    }
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        if (e.getCause() instanceof FrontendServiceException fsException)
                            showAlert(fsException);
                        else
                            showAlert("خطای غیرمنتظره", "خطای غیرمنتظره‌ای رخ داد: " + e.getMessage(), Alert.AlertType.ERROR, null);
                    });
                    return null;
                });
    }

    private Node createOrderCard(OrderResponse order) {
        VBox card = new VBox(10);
        card.getStyleClass().add("order-card");
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.TOP_RIGHT);
        VBox.setVgrow(card, Priority.ALWAYS);

        Label idLabel = new Label("شناسه سفارش: " + order.getId());
        idLabel.getStyleClass().add("card-text-primary");

        Label statusLabel = new Label("وضعیت: " + convertStatusToPersian(order.getStatus()));
        statusLabel.getStyleClass().add("card-text-secondary");

        Label addressLabel = new Label("آدرس تحویل: " + order.getDeliveryAddress());
        addressLabel.getStyleClass().add("card-text");

        Label vendorLabel = new Label("شناسه فروشنده: " + order.getVendorId());
        vendorLabel.getStyleClass().add("card-text");

        Label priceLabel = new Label("قیمت نهایی: " + order.getPayPrice() + " تومان");
        priceLabel.getStyleClass().add("card-text");

        Label createdAtLabel = new Label("تاریخ ثبت: " + (order.getCreatedAt() != null ? order.getCreatedAt().substring(0, 10) : "N/A"));
        createdAtLabel.getStyleClass().add("card-text");

        card.getChildren().addAll(idLabel, statusLabel, addressLabel, vendorLabel, priceLabel, createdAtLabel);

        if (order.getItems() != null && !order.getItems().isEmpty()) {
            Label itemsLabel = new Label("شناسه آیتم‌ها: " +
                    order.getItems().stream().map(String::valueOf).collect(Collectors.joining(", ")));
            itemsLabel.getStyleClass().add("card-text");
            card.getChildren().add(itemsLabel);
        }

        return card;
    }

    private Node createNoOrdersMessage(String message) {
        Label noOrdersLabel = new Label(message);
        noOrdersLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #555555; -fx-alignment: center;");
        VBox container = new VBox(noOrdersLabel);
        container.setAlignment(Pos.CENTER);
        VBox.setVgrow(container, Priority.ALWAYS);
        return container;
    }

    private String convertStatusToPersian(String status) {
        return switch (status.toLowerCase()) {
            case "submitted" -> "ثبت شده";
            case "unpaid and cancelled" -> "پرداخت نشده و لغو شده";
            case "waiting vendor" -> "در انتظار تایید فروشنده";
            case "cancelled" -> "لغو شده";
            case "finding courier" -> "در حال یافتن پیک";
            case "on the way" -> "در مسیر تحویل";
            case "completed" -> "تکمیل شده";
            case "accepted" -> "پذیرفته شده";
            case "rejected" -> "رد شده";
            case "served" -> "آماده تحویل توسط پیک";
            case "received" -> "دریافت شده توسط پیک";
            case "delivered" -> "تحویل داده شده";
            default -> status;
        };
    }

    @FXML
    public void handleBackButton(ActionEvent actionEvent) {
        if (closeCallback != null) {
            closeCallback.accept(null);
        } else {
            navigateToUserProfileView(actionEvent.getSource());
        }
    }
}