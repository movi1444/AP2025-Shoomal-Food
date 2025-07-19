package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.request.CreateCouponRequest;
import com.aut.shoomal.dto.request.UpdateCouponRequest;
import com.aut.shoomal.dto.response.UserResponse;
import com.aut.shoomal.exceptions.FrontendServiceException;
import com.aut.shoomal.service.AdminDataService;
import com.aut.shoomal.utils.PreferencesManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class CreateEditCouponController extends AbstractBaseController {

    @FXML private Label titleLabel;
    @FXML private TextField couponCodeField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField valueField;
    @FXML private TextField minPriceField;
    @FXML private TextField userCountField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private AdminDataService adminDataService;
    private String token;
    private Integer couponId;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private UserResponse loggedInUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        adminDataService = new AdminDataService();
        token = PreferencesManager.getJwtToken();

        typeComboBox.setItems(FXCollections.observableArrayList("fixed", "percent"));

        addTextDirectionListener(couponCodeField);
        addTextDirectionListener(valueField);
        addTextDirectionListener(minPriceField);
        addTextDirectionListener(userCountField);
        addComboBoxDirectionListener(typeComboBox);
    }

    public void setCouponId(Integer couponId) {
        this.couponId = couponId;
        if (this.couponId != null) {
            titleLabel.setText("ویرایش کوپن");
            loadCouponDetails();
        } else {
            titleLabel.setText("ساخت کوپن جدید");
        }
    }

    private void loadCouponDetails() {
        if (token == null || token.isEmpty()) {
            showAlert("خطای احراز هویت", "ادمین وارد نشده است. لطفا ابتدا وارد شوید.", Alert.AlertType.ERROR, null);
            navigateToSignInView(saveButton);
            return;
        }
        if (couponId == null) {
            showAlert("خطا", "شناسه کوپن برای بارگذاری جزئیات وجود ندارد.", Alert.AlertType.ERROR, null);
            return;
        }

        adminDataService.getCouponById(token, couponId)
                .thenAccept(coupon -> Platform.runLater(() -> {
                    if (coupon != null) {
                        couponCodeField.setText(coupon.getCouponCode());
                        typeComboBox.setValue(coupon.getType());
                        valueField.setText(String.valueOf(coupon.getValue()));
                        minPriceField.setText(String.valueOf(coupon.getMinPrice()));
                        userCountField.setText(String.valueOf(coupon.getUserCount()));
                        if (coupon.getStartDate() != null) {
                            String startDateOnly = coupon.getStartDate().split("T")[0];
                            startDatePicker.setValue(LocalDate.parse(startDateOnly, DATE_FORMATTER));
                        }
                        if (coupon.getEndDate() != null) {
                            String endDateOnly = coupon.getEndDate().split("T")[0];
                            endDatePicker.setValue(LocalDate.parse(endDateOnly, DATE_FORMATTER));
                        }
                    } else {
                        showAlert("خطا", "جزئیات کوپن بارگیری نشد.", Alert.AlertType.ERROR, null);
                    }
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        if (e.getCause() instanceof FrontendServiceException fsException)
                            showAlert(fsException);
                        else
                            showAlert("خطا", "خطا در بارگیری جزئیات کوپن: " + e.getMessage(), Alert.AlertType.ERROR, null);
                    });
                    return null;
                });
    }

    @FXML
    private void handleSaveCoupon() {
        if (token == null || token.isEmpty()) {
            showAlert("خطای احراز هویت", "ادمین وارد نشده است. لطفا ابتدا وارد شوید.", Alert.AlertType.ERROR, null);
            navigateToSignInView(saveButton);
            return;
        }

        String couponCode = couponCodeField.getText();
        String type = typeComboBox.getValue();
        Double value;
        Integer minPrice;
        Integer userCount;
        String startDate = startDatePicker.getValue() != null ? startDatePicker.getValue().format(DATE_FORMATTER) : null;
        String endDate = endDatePicker.getValue() != null ? endDatePicker.getValue().format(DATE_FORMATTER) : null;

        if (couponCode.isEmpty() || type == null || valueField.getText().isEmpty() || minPriceField.getText().isEmpty() || userCountField.getText().isEmpty() || startDate == null || endDate == null) {
            showAlert("خطای ورودی", "لطفا تمام فیلدهای اجباری را پر کنید.", Alert.AlertType.WARNING, null);
            return;
        }

        try {
            value = Double.parseDouble(valueField.getText());
            minPrice = Integer.parseInt(minPriceField.getText());
            userCount = Integer.parseInt(userCountField.getText());
        } catch (NumberFormatException e) {
            showAlert("خطای ورودی", "مقدار، حداقل قیمت و تعداد کاربر باید اعداد معتبر باشند.", Alert.AlertType.WARNING, null);
            return;
        }

        if (couponId == null) {
            CreateCouponRequest request = new CreateCouponRequest();
            request.setCouponCode(couponCode);
            request.setType(type);
            request.setValue(value);
            request.setMinPrice(minPrice);
            request.setUserCount(userCount);
            request.setStartDate(startDate);
            request.setEndDate(endDate);

            adminDataService.createCoupon(token, request)
                    .thenAccept(couponResponse -> Platform.runLater(() -> {
                        if (couponResponse != null) {
                            showAlert("موفقیت", "کوپن با موفقیت ایجاد شد.", Alert.AlertType.INFORMATION, null);
                            handleCancel();
                        } else {
                            showAlert("خطا", "خطا در ایجاد کوپن.", Alert.AlertType.ERROR, null);
                        }
                    }))
                    .exceptionally(e -> {
                        Platform.runLater(() -> {
                            if (e.getCause() instanceof FrontendServiceException fsException)
                                showAlert(fsException);
                            else
                                showAlert("خطا", "خطا در ایجاد کوپن: " + e.getMessage(), Alert.AlertType.ERROR, null);
                        });
                        return null;
                    });
        } else { 
            UpdateCouponRequest request = new UpdateCouponRequest();
            request.setCouponCode(couponCode);
            request.setType(type);
            request.setValue(value);
            request.setMinPrice(minPrice);
            request.setUserCount(userCount);
            request.setStartDate(startDate);
            request.setEndDate(endDate);

            adminDataService.updateCoupon(token, couponId, request)
                    .thenAccept(couponResponse -> Platform.runLater(() -> {
                        if (couponResponse != null) {
                            showAlert("موفقیت", "کوپن با موفقیت به‌روزرسانی شد.", Alert.AlertType.INFORMATION, null);
                            handleCancel();
                        } else {
                            showAlert("خطا", "خطا در به‌روزرسانی کوپن.", Alert.AlertType.ERROR, null);
                        }
                    }))
                    .exceptionally(e -> {
                        Platform.runLater(() -> {
                            if (e.getCause() instanceof FrontendServiceException fsException)
                                showAlert(fsException);
                            else
                                showAlert("خطا", "خطا در به‌روزرسانی کوپن: " + e.getMessage(), Alert.AlertType.ERROR, null);
                        });
                        return null;
                    });
        }
    }

    @FXML
    private void handleCancel() {
        navigateTo(
                cancelButton,
                "/com/aut/shoomal/views/MainView.fxml",
                "/com/aut/shoomal/styles/MainView.css",
                TransitionType.SLIDE_LEFT,
                (MainController controller) -> {
                    controller.setLoggedInUser(loggedInUser);
                }
        );
    }

    public void setLoggedInUser(UserResponse user) {
        this.loggedInUser = user;
    }
}