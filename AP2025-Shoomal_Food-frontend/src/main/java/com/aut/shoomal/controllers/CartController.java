package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.response.CartItemResponse;
import com.aut.shoomal.dto.request.RemoveItemFromCartRequest;
import com.aut.shoomal.dto.request.AddItemToCartRequest;
import com.aut.shoomal.exceptions.FrontendServiceException;
import com.aut.shoomal.service.CartService;
import com.aut.shoomal.service.RestaurantService;
import com.aut.shoomal.utils.PreferencesManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ButtonType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import java.net.URL;

import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CartController extends AbstractBaseController {

    @FXML private TableView<CartItemResponse> cartTableView;
    @FXML private TableColumn<CartItemResponse, String> foodNameColumn;
    @FXML private TableColumn<CartItemResponse, Integer> quantityColumn;
    @FXML private TableColumn<CartItemResponse, Integer> unitPriceColumn;
    @FXML private TableColumn<CartItemResponse, Integer> itemTotalPriceColumn;
    @FXML private TableColumn<CartItemResponse, Void> quantityControlColumn;
    @FXML private TableColumn<CartItemResponse, Void> removeColumn;
    @FXML private Label totalPriceLabel;
    @FXML private Button checkoutButton;
    @FXML private Button clearCartButton;
    @FXML private Button backButton;

    private CartService cartService;
    private RestaurantService restaurantService;
    private String token;
    private Long userId;
    private Integer restaurantId;

    private boolean isReadyToLoad = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        cartService = new CartService();
        restaurantService = new RestaurantService();
        token = PreferencesManager.getJwtToken();

        foodNameColumn.setCellValueFactory(new PropertyValueFactory<>("foodItemName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        unitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        itemTotalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("itemTotalPrice"));

        quantityControlColumn.setCellFactory(param -> new TableCell<>() {
            private final Button decreaseBtn = new Button("-");
            private final Button increaseBtn = new Button("+");
            private final HBox pane = new HBox(5, decreaseBtn, increaseBtn);

            {
                decreaseBtn.getStyleClass().add("quantity-button");
                increaseBtn.getStyleClass().add("quantity-button");
                pane.setAlignment(Pos.CENTER);

                decreaseBtn.setOnAction(event -> {
                    CartItemResponse item = getTableView().getItems().get(getIndex());
                    handleUpdateQuantity(item.getFoodItemId(), item.getQuantity() - 1);
                });

                increaseBtn.setOnAction(event -> {
                    CartItemResponse item = getTableView().getItems().get(getIndex());
                    handleUpdateQuantity(item.getFoodItemId(), item.getQuantity() + 1);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });

        removeColumn.setCellFactory(param -> new TableCell<>() {
            private final Button removeBtn = new Button("حذف");
            {
                removeBtn.getStyleClass().add("my-button3");
                removeBtn.setOnAction((ActionEvent event) -> {
                    CartItemResponse item = getTableView().getItems().get(getIndex());
                    handleRemoveItemWithConfirmation(item.getFoodItemId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeBtn);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    public void setRestaurantId(Integer restaurantId) {
        this.restaurantId = restaurantId;
        checkAndLoadCart();
    }

    public void setUserId(Long userId) {
        this.userId = userId;
        checkAndLoadCart();
    }

    private void checkAndLoadCart() {
        if (this.restaurantId != null && this.userId != null && !isReadyToLoad) {
            isReadyToLoad = true;
            loadCartItems();
        }
    }

    private void loadCartItems() {
        if (token == null || token.isEmpty()) {
            showAlert("Authentication Error", "کاربر وارد نشده است. لطفا ابتدا وارد شوید.", Alert.AlertType.ERROR, null);
            navigateToSignInView(backButton);
            return;
        }
        if (userId == null || restaurantId == null) {
            showAlert("Error", "شناسه کاربر یا رستوران تنظیم نشده است. نمی‌توان سبد خرید را بارگیری کرد.", Alert.AlertType.ERROR, null);
            return;
        }

        cartService.getCart(userId, restaurantId.longValue(), token)
                .thenAccept(cartResponse -> Platform.runLater(() -> {
                    if (cartResponse != null && cartResponse.getItems() != null) {
                        ObservableList<CartItemResponse> cartItems = FXCollections.observableArrayList(cartResponse.getItems());
                        cartTableView.setItems(cartItems);
                        totalPriceLabel.setText(cartResponse.getTotalPrice() + " تومان");
                    } else {
                        cartTableView.setItems(FXCollections.emptyObservableList());
                        totalPriceLabel.setText("0 تومان");
                        showAlert("اطلاعات", "سبد خرید شما خالی است.", Alert.AlertType.INFORMATION, null);
                    }
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        if (e.getCause() instanceof FrontendServiceException fsException) {
                            showAlert(fsException);
                        } else {
                            showAlert("خطا", "بارگیری سبد خرید با شکست مواجه شد: " + e.getMessage(), Alert.AlertType.ERROR, null);
                        }
                        cartTableView.setItems(FXCollections.emptyObservableList());
                        totalPriceLabel.setText("0 تومان");
                    });
                    return null;
                });
    }

    private void handleUpdateQuantity(Long foodItemId, Integer desiredNewQuantity) {
        if (token == null || token.isEmpty() || userId == null || restaurantId == null) {
            showAlert("Authentication Error", "کاربر وارد نشده است یا اطلاعات سبد خرید از دست رفته است. لطفا وارد شوید.", Alert.AlertType.ERROR, null);
            return;
        }

        if (desiredNewQuantity < 0) {
            showAlert("خطا", "تعداد آیتم نمی‌تواند کمتر از صفر باشد.", Alert.AlertType.ERROR, null);
            return;
        }

        Optional<CartItemResponse> currentItemOpt = cartTableView.getItems().stream()
                .filter(item -> item.getFoodItemId().equals(foodItemId))
                .findFirst();

        if (currentItemOpt.isEmpty()) {
            showAlert("خطا", "آیتم در سبد خرید یافت نشد.", Alert.AlertType.ERROR, null);
            return;
        }

        Integer currentQuantity = currentItemOpt.get().getQuantity();
        Integer quantityDelta = desiredNewQuantity - currentQuantity;

        if (quantityDelta == 0) {
            return;
        }

        if (desiredNewQuantity == 0) {
            performRemoveItem(foodItemId);
            return;
        }

        AddItemToCartRequest request = new AddItemToCartRequest();
        request.setRestaurantId(this.restaurantId.longValue());
        request.setFoodItemId(foodItemId);
        request.setQuantity(quantityDelta);

        cartService.addItemToCart(request, token)
                .thenAccept(cartResponse -> Platform.runLater(() -> {
                    if (cartResponse != null) {
                        showAlert("موفقیت", "تعداد آیتم با موفقیت به‌روزرسانی شد. مجموع: " + cartResponse.getTotalPrice() + " تومان", Alert.AlertType.INFORMATION, null);
                        loadCartItems();
                    } else {
                        showAlert("خطا", "خطا در به‌روزرسانی تعداد آیتم.", Alert.AlertType.ERROR, null);
                    }
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        if (e.getCause() instanceof FrontendServiceException fsException) {
                            showAlert(fsException);
                        } else {
                            showAlert("خطا", "خطای غیرمنتظره در به‌روزرسانی تعداد آیتم: " + e.getMessage(), Alert.AlertType.ERROR, null);
                        }
                    });
                    return null;
                });
    }

    private void performRemoveItem(Long foodItemId) {
        if (token == null || token.isEmpty() || userId == null || restaurantId == null) {
            showAlert("Authentication Error", "کاربر وارد نشده است یا اطلاعات سبد خرید از دست رفته است. لطفا وارد شوید.", Alert.AlertType.ERROR, null);
            return;
        }

        RemoveItemFromCartRequest request = new RemoveItemFromCartRequest();
        request.setRestaurantId(this.restaurantId.longValue());
        request.setFoodItemId(foodItemId);

        cartService.removeItemFromCart(request, token)
                .thenAccept(apiResponse -> Platform.runLater(() -> {
                    if (apiResponse.isSuccess()) {
                        showAlert("موفقیت", "آیتم با موفقیت از سبد خرید حذف شد.", Alert.AlertType.INFORMATION, null);
                        loadCartItems();
                    } else {
                        showAlert("خطا", "خطا در حذف آیتم: " + apiResponse.getError(), Alert.AlertType.ERROR, null);
                    }
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        if (e.getCause() instanceof FrontendServiceException fsException) {
                            showAlert(fsException);
                        } else {
                            showAlert("خطا", "خطای غیرمنتظره در حذف آیتم: " + e.getMessage(), Alert.AlertType.ERROR, null);
                        }
                    });
                    return null;
                });
    }

    private void handleRemoveItemWithConfirmation(Long foodItemId) {
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("تایید حذف");
        confirmationAlert.setHeaderText(null);
        confirmationAlert.setContentText("آیا مطمئن هستید که می‌خواهید این آیتم را از سبد خرید حذف کنید؟");
        confirmationAlert.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/com/aut/shoomal/styles/AlertStyles.css")).toExternalForm()
        );
        confirmationAlert.getDialogPane().getStyleClass().add("custom-alert");
        confirmationAlert.getDialogPane().getStyleClass().add("confirmation");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            performRemoveItem(foodItemId);
        }
    }

    @FXML
    private void handleCheckout(ActionEvent event) {
        if (token == null || token.isEmpty()) {
            showAlert("Authentication Error", "کاربر وارد نشده است. لطفا ابتدا وارد شوید.", Alert.AlertType.ERROR, null);
            return;
        }
        if (restaurantId == null) {
            showAlert("خطا", "اطلاعات رستوران بارگیری نشده است. نمی‌توان سفارش را ثبت کرد.", Alert.AlertType.ERROR, null);
            return;
        }
        if (cartTableView.getItems().isEmpty()) {
            showAlert("اطلاعات", "سبد خرید شما خالی است. نمی‌توانید سفارش ثبت کنید.", Alert.AlertType.INFORMATION, null);
            return;
        }

        restaurantService.getRestaurantById(token, restaurantId)
                .thenAccept(restaurantResponse -> Platform.runLater(() -> {
                    if (restaurantResponse != null) {
                        navigateTo(
                                (Node) event.getSource(),
                                "/com/aut/shoomal/views/SubmitOrderView.fxml",
                                "/com/aut/shoomal/styles/AdminDashboardStyles.css",
                                TransitionType.SLIDE_RIGHT,
                                controller -> {
                                    if (controller instanceof SubmitOrderController submitOrderController) {
                                        submitOrderController.setOrderDetails(
                                                restaurantId,
                                                cartTableView.getItems().stream().collect(Collectors.toList()),
                                                (Integer) (int)Double.parseDouble(totalPriceLabel.getText().replaceAll("[^\\d.]", "")),
                                                restaurantResponse.getTaxFee(),
                                                restaurantResponse.getAdditionalFee()
                                        );
                                    }
                                }
                        );
                    } else {
                        showAlert("خطا", "اطلاعات رستوران برای تکمیل سفارش یافت نشد.", Alert.AlertType.ERROR, null);
                    }
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        if (e.getCause() instanceof FrontendServiceException fsException) {
                            showAlert(fsException);
                        } else {
                            showAlert("خطا", "خطای غیرمنتظره در بارگیری اطلاعات رستوران: " + e.getMessage(), Alert.AlertType.ERROR, null);
                        }
                    });
                    return null;
                });
    }

    @FXML
    private void handleClearCart(ActionEvent event) {
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("تایید پاک کردن سبد خرید");
        confirmationAlert.setHeaderText(null);
        confirmationAlert.setContentText("آیا مطمئن هستید که می‌خواهید تمام آیتم‌های سبد خرید را پاک کنید؟");
        confirmationAlert.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/com/aut/shoomal/styles/AlertStyles.css")).toExternalForm()
        );
        confirmationAlert.getDialogPane().getStyleClass().add("custom-alert");
        confirmationAlert.getDialogPane().getStyleClass().add("confirmation");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (token == null || token.isEmpty() || userId == null || restaurantId == null) {
                showAlert("Authentication Error", "کاربر وارد نشده است یا اطلاعات سبد خرید از دست رفته است. لطفا وارد شوید.", Alert.AlertType.ERROR, null);
                return;
            }

            cartService.clearCart(userId, restaurantId.longValue(), token)
                    .thenAccept(apiResponse -> Platform.runLater(() -> {
                        if (apiResponse.isSuccess()) {
                            showAlert("موفقیت", "سبد خرید با موفقیت پاک شد.", Alert.AlertType.INFORMATION, null);
                            cartTableView.setItems(FXCollections.emptyObservableList());
                            totalPriceLabel.setText("0 تومان");
                        } else {
                            showAlert("خطا", "خطا در پاک کردن سبد خرید: " + apiResponse.getError(), Alert.AlertType.ERROR, null);
                        }
                    }))
                    .exceptionally(e -> {
                        Platform.runLater(() -> {
                            if (e.getCause() instanceof FrontendServiceException fsException) {
                                showAlert(fsException);
                            } else {
                                showAlert("خطا", "خطای غیرمنتظره در پاک کردن سبد خرید: " + e.getMessage(), Alert.AlertType.ERROR, null);
                            }
                        });
                        return null;
                    });
        }
    }
    @FXML
    private void handleBack(ActionEvent event) {
        navigateTo(
                (Node) event.getSource(),
                "/com/aut/shoomal/views/BuyerRestaurantView.fxml",
                "/com/aut/shoomal/styles/MainView.css",
                TransitionType.SLIDE_LEFT,
                controller -> {
                    if (controller instanceof BuyerShowRestaurantDetailsController detailsController) {
                        detailsController.setRestaurantId(restaurantId);
                    }
                }
        );
    }
}
