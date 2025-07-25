package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.request.OrderItemRequest;
import com.aut.shoomal.dto.request.PaymentRequest;
import com.aut.shoomal.dto.request.SubmitOrderRequest;
import com.aut.shoomal.dto.response.CartItemResponse;

import com.aut.shoomal.dto.response.UserResponse;
import com.aut.shoomal.exceptions.FrontendServiceException;
import com.aut.shoomal.service.CartService;
import com.aut.shoomal.service.OrderService;
import com.aut.shoomal.service.TransactionService;
import com.aut.shoomal.utils.PreferencesManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class SubmitOrderController extends AbstractBaseController {

    @FXML private TextField deliveryAddressField;
    @FXML private TextField couponCodeField;
    @FXML private Label orderSummaryLabel;
    @FXML private Label totalPriceLabel;
    @FXML private Button submitOrderButton;
    @FXML private Button backButton;
    @FXML private Button applyCouponButton;
    @FXML private Label couponStatusLabel;
    @FXML private ChoiceBox<String> paymentMethodChoiceBox;
    @FXML private Label walletBalanceLabel;

    private Integer restaurantId;
    private List<CartItemResponse> cartItems;
    private Integer cartItemsTotalPrice;
    private Integer originalTotalOrderPrice;
    private Integer currentDisplayPrice;
    private String token;
    private OrderService orderService;
    private CartService cartService;
    private TransactionService transactionService;
    private Integer appliedCouponId;
    private BigDecimal currentWalletBalance;
    private Integer restaurantTaxFee;
    private Integer restaurantAdditionalFee;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        orderService = new OrderService();
        cartService = new CartService();
        transactionService = new TransactionService();
        token = PreferencesManager.getJwtToken();
        addTextDirectionListener(deliveryAddressField);
        addTextDirectionListener(couponCodeField);

        UserResponse loggedInUser = PreferencesManager.getUserData();
        if (loggedInUser != null && loggedInUser.getAddress() != null) {
            deliveryAddressField.setText(loggedInUser.getAddress());
        }

        couponStatusLabel.setText("");
        couponStatusLabel.setTextFill(Color.BLACK);

        paymentMethodChoiceBox.getItems().addAll("wallet", "online");
        paymentMethodChoiceBox.setValue("wallet");
        addChoiceBoxDirectionListener(paymentMethodChoiceBox);

        paymentMethodChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if ("wallet".equals(newVal)) {
                walletBalanceLabel.setVisible(true);
                walletBalanceLabel.setManaged(true);
                loadWalletBalance();
            } else {
                walletBalanceLabel.setVisible(false);
                walletBalanceLabel.setManaged(false);
            }
        });

        loadWalletBalance();
    }

    public void setOrderDetails(Integer restaurantId, List<CartItemResponse> items, Integer cartItemsTotalPrice, Integer taxFee, Integer additionalFee) {
        this.restaurantId = restaurantId;
        this.cartItems = items;
        this.cartItemsTotalPrice = cartItemsTotalPrice;
        this.restaurantTaxFee = taxFee;
        this.restaurantAdditionalFee = additionalFee;

        this.originalTotalOrderPrice = cartItemsTotalPrice + this.restaurantTaxFee + this.restaurantAdditionalFee;
        this.currentDisplayPrice = this.originalTotalOrderPrice;

        StringBuilder summary = new StringBuilder();
        for (CartItemResponse item : items) {
            summary.append("- ").append(item.getFoodItemName()).append(" x ").append(item.getQuantity())
                    .append(" = ").append(item.getItemTotalPrice()).append(" تومان\n");
        }
        summary.append(" هزینه بسته‌بندی: ").append(this.restaurantTaxFee).append(" تومان ");
        summary.append(" هزینه اضافی: ").append(this.restaurantAdditionalFee).append(" تومان ");
        summary.append(" (توجه: هزینه پیک (معمولا 500 تومان) در مرحله نهایی به مبلغ کل اضافه خواهد شد.)\n");

        this.originalTotalOrderPrice += 500;

        orderSummaryLabel.setText(summary.toString());
        totalPriceLabel.setText("مبلغ کل: " + originalTotalOrderPrice + " تومان");
    }

    private void loadWalletBalance() {
        if (token == null || token.isEmpty()) {
            walletBalanceLabel.setText("موجودی کیف پول: N/A");
            return;
        }

        transactionService.getWalletAmount(token)
                .thenAccept(amount -> Platform.runLater(() -> {
                    if (amount != null) {
                        currentWalletBalance = amount;
                        walletBalanceLabel.setText("موجودی کیف پول: " + amount + " تومان");
                    } else {
                        walletBalanceLabel.setText("موجودی کیف پول: N/A");
                    }
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        walletBalanceLabel.setText("موجودی کیف پول: خطا");
                        if (e.getCause() instanceof FrontendServiceException fsException) {
                            System.err.println("Error loading wallet balance: " + fsException.getMessage());
                        } else {
                            System.err.println("Unexpected error loading wallet balance: " + e.getMessage());
                        }
                    });
                    return null;
                });
    }

    @FXML
    private void handleApplyCoupon(ActionEvent event) {
        String couponCode = couponCodeField.getText().trim();
        if (couponCode.isEmpty()) {
            couponStatusLabel.setText("لطفا کد تخفیف را وارد کنید.");
            couponStatusLabel.setTextFill(Color.ORANGE);
            appliedCouponId = null;
            currentDisplayPrice = originalTotalOrderPrice;
            totalPriceLabel.setText("مبلغ کل: " + originalTotalOrderPrice + " تومان");
            return;
        }

        if (token == null || token.isEmpty()) {
            showAlert("خطای احراز هویت", "کاربر وارد نشده است. لطفا ابتدا وارد شوید.", Alert.AlertType.ERROR, null);
            return;
        }

        orderService.checkCouponValidity(token, couponCode)
                .thenAccept(couponResponse -> Platform.runLater(() -> {
                    if (couponResponse != null && couponResponse.getId() != null) {
                        if (originalTotalOrderPrice < couponResponse.getMinPrice()) {
                            couponStatusLabel.setText("کد تخفیف معتبر نیست: حداقل مبلغ سفارش " + couponResponse.getMinPrice() + " تومان است.");
                            couponStatusLabel.setTextFill(Color.RED);
                            appliedCouponId = null;
                            currentDisplayPrice = originalTotalOrderPrice;
                            totalPriceLabel.setText("مبلغ کل: " + originalTotalOrderPrice + " تومان");
                            return;
                        }

                        double discountAmount = 0;
                        if ("fixed".equalsIgnoreCase(couponResponse.getType())) {
                            discountAmount = couponResponse.getValue();
                        } else if ("percent".equalsIgnoreCase(couponResponse.getType())) {
                            discountAmount = originalTotalOrderPrice * (couponResponse.getValue() / 100.0);
                        }

                        currentDisplayPrice = (int) Math.max(0, originalTotalOrderPrice - discountAmount);

                        couponStatusLabel.setText("کد تخفیف معتبر است! مبلغ تخفیف: " + (int)discountAmount + " تومان");
                        couponStatusLabel.setTextFill(Color.GREEN);
                        totalPriceLabel.setText("مبلغ کل (با تخفیف): " + currentDisplayPrice + " تومان");
                        appliedCouponId = couponResponse.getId();
                    } else {
                        couponStatusLabel.setText("کد تخفیف نامعتبر است.");
                        couponStatusLabel.setTextFill(Color.RED);
                        appliedCouponId = null;
                        currentDisplayPrice = originalTotalOrderPrice;
                        totalPriceLabel.setText("مبلغ کل: " + originalTotalOrderPrice + " تومان");
                    }
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        if (e.getCause() instanceof FrontendServiceException fsException) {
                            couponStatusLabel.setText("خطا: " + fsException.getMessage());
                            couponStatusLabel.setTextFill(Color.RED);
                        } else {
                            couponStatusLabel.setText("خطای غیرمنتظره در بررسی کد تخفیف.");
                            couponStatusLabel.setTextFill(Color.RED);
                        }
                        appliedCouponId = null;
                        currentDisplayPrice = originalTotalOrderPrice;
                        totalPriceLabel.setText("مبلغ کل: " + originalTotalOrderPrice + " تومان");
                    });
                    return null;
                });
    }

    @FXML
    private void handleSubmitOrder(ActionEvent event) {
        if (token == null || token.isEmpty()) {
            showAlert("خطای احراز هویت", "کاربر وارد نشده است. لطفا ابتدا وارد شوید.", Alert.AlertType.ERROR, null);
            navigateToSignInView(submitOrderButton);
            return;
        }

        String deliveryAddress = deliveryAddressField.getText().trim();
        if (deliveryAddress.isEmpty()) {
            showAlert("خطای ورودی", "لطفا آدرس تحویل را وارد کنید.", Alert.AlertType.WARNING, null);
            return;
        }

        String selectedPaymentMethod = paymentMethodChoiceBox.getValue();
        if (selectedPaymentMethod == null || selectedPaymentMethod.isEmpty()) {
            showAlert("خطای ورودی", "لطفا روش پرداخت را انتخاب کنید.", Alert.AlertType.WARNING, null);
            return;
        }

        if ("wallet".equals(selectedPaymentMethod) && currentWalletBalance != null && currentWalletBalance.intValue() < currentDisplayPrice) {
            showAlert("خطای پرداخت", "موجودی کیف پول کافی نیست.", Alert.AlertType.ERROR, null);
            return;
        }

        List<OrderItemRequest> orderItems = cartItems.stream()
                .map(cartItem -> new OrderItemRequest(cartItem.getFoodItemId().intValue(), cartItem.getQuantity()))
                .collect(Collectors.toList());

        SubmitOrderRequest submitOrderRequest = new SubmitOrderRequest();
        submitOrderRequest.setDeliveryAddress(deliveryAddress);
        submitOrderRequest.setVendorId(restaurantId);
        submitOrderRequest.setItems(orderItems);
        submitOrderRequest.setCouponId(appliedCouponId);

        orderService.submitOrder(token, submitOrderRequest)
                .thenAccept(orderResponse -> Platform.runLater(() -> {
                    if (orderResponse != null && orderResponse.getId() != null) {
                        PaymentRequest paymentRequest = new PaymentRequest();
                        paymentRequest.setOrderId(orderResponse.getId());
                        paymentRequest.setMethod(selectedPaymentMethod);

                        transactionService.onlinePayment(token, paymentRequest)
                                .thenAccept(transactionResponse -> Platform.runLater(() -> {
                                    if (transactionResponse != null && "completed".equalsIgnoreCase(transactionResponse.getStatus())) {
                                        showAlert("موفقیت", "سفارش شما با موفقیت ثبت و پرداخت شد! شناسه سفارش: " + orderResponse.getId(), Alert.AlertType.INFORMATION, null);
                                        UserResponse currentUser = PreferencesManager.getUserData();
                                        if (currentUser != null && currentUser.getId() != null) {
                                            cartService.clearCart(currentUser.getId(), (long) restaurantId, token)
                                                    .thenAccept(apiResponse -> {
                                                        if (!apiResponse.isSuccess()) {
                                                            System.err.println("Failed to clear cart after successful order: " + apiResponse.getError());
                                                        }
                                                    });
                                        }
                                        navigateToMainView(submitOrderButton);
                                    } else {
                                        showAlert("خطا", "پرداخت سفارش با شکست مواجه شد. وضعیت تراکنش: " + (transactionResponse != null ? transactionResponse.getStatus() : "نامشخص"), Alert.AlertType.ERROR, null);
                                    }
                                }))
                                .exceptionally(e -> {
                                    Platform.runLater(() -> {
                                        if (e.getCause() instanceof FrontendServiceException fsException) {
                                            showAlert(fsException);
                                        } else {
                                            showAlert("خطا", "خطای غیرمنتظره در پردازش پرداخت: " + e.getMessage(), Alert.AlertType.ERROR, null);
                                        }
                                    });
                                    return null;
                                });
                    } else {
                        showAlert("خطا", "ثبت سفارش با شکست مواجه شد.", Alert.AlertType.ERROR, null);
                    }
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        if (e.getCause() instanceof FrontendServiceException fsException) {
                            showAlert(fsException);
                        } else {
                            showAlert("خطا", "خطای غیرمنتظره در ثبت سفارش: " + e.getMessage(), Alert.AlertType.ERROR, null);
                        }
                    });
                    return null;
                });
    }

    @FXML
    private void handleBack(ActionEvent event) {
        navigateTo(
                (Node) event.getSource(),
                "/com/aut/shoomal/views/CartView.fxml",
                "/com/aut/shoomal/styles/AdminDashboardStyles.css",
                TransitionType.SLIDE_LEFT,
                controller -> {
                    if (controller instanceof CartController cartController) {
                        cartController.setRestaurantId(restaurantId);
                        cartController.setUserId(PreferencesManager.getUserData().getId());
                    }
                }
        );
    }
}
