package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.response.ApiResponse;
import com.aut.shoomal.dto.response.CouponResponse;
import com.aut.shoomal.dto.response.UserResponse;
import com.aut.shoomal.exceptions.FrontendServiceException;
import com.aut.shoomal.service.AdminDataService;
import com.aut.shoomal.utils.PreferencesManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class CouponListController extends AbstractBaseController {

    @FXML private TableView<CouponResponse> couponTable;
    @FXML private TableColumn<CouponResponse, Integer> idColumn;
    @FXML private TableColumn<CouponResponse, String> couponCodeColumn;
    @FXML private TableColumn<CouponResponse, String> typeColumn;
    @FXML private TableColumn<CouponResponse, Double> valueColumn;
    @FXML private TableColumn<CouponResponse, Integer> minPriceColumn;
    @FXML private TableColumn<CouponResponse, Integer> userCountColumn;
    @FXML private TableColumn<CouponResponse, String> startDateColumn;
    @FXML private TableColumn<CouponResponse, String> endDateColumn;
    @FXML private TableColumn<CouponResponse, Void> actionsColumn;
    @FXML private Button backButton;

    private AdminDataService adminDataService;
    private String token;
    private UserResponse loggedInUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        adminDataService = new AdminDataService();
        token = PreferencesManager.getJwtToken();

        setupTable();
        loadCoupons();
    }

    private void setupTable() {
        couponTable.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        couponTable.setEditable(false);

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        couponCodeColumn.setCellValueFactory(new PropertyValueFactory<>("couponCode"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        minPriceColumn.setCellValueFactory(new PropertyValueFactory<>("minPrice"));
        userCountColumn.setCellValueFactory(new PropertyValueFactory<>("userCount"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Hyperlink editLink;
            private final Hyperlink deleteLink;
            private final HBox pane;

            {
                ImageView editIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/aut/shoomal/images/edit_icon.png"))));
                editIcon.setFitHeight(20);
                editIcon.setFitWidth(20);
                editLink = new Hyperlink();
                editLink.setGraphic(editIcon);
                editLink.setTooltip(new Tooltip("ویرایش کوپن"));
                editLink.setOnAction(event -> {
                    CouponResponse coupon = getTableView().getItems().get(getIndex());
                    handleEditCoupon(coupon);
                });

                ImageView deleteIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/aut/shoomal/images/delete_icon.png"))));
                deleteIcon.setFitHeight(20);
                deleteIcon.setFitWidth(20);
                deleteLink = new Hyperlink();
                deleteLink.setGraphic(deleteIcon);
                deleteLink.setTooltip(new Tooltip("حذف کوپن"));
                deleteLink.setOnAction(event -> {
                    CouponResponse coupon = getTableView().getItems().get(getIndex());
                    handleDeleteCoupon(coupon.getId());
                });

                pane = new HBox(5, editLink, deleteLink);
                pane.setAlignment(javafx.geometry.Pos.CENTER);
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
    }

    private void loadCoupons() {
        if (token == null || token.isEmpty()) {
            showAlert("خطای احراز هویت", "ادمین وارد نشده است. لطفا ابتدا وارد شوید.", Alert.AlertType.ERROR, null);
            navigateToSignInView(couponTable);
            return;
        }

        adminDataService.getAllCoupons(token)
                .thenAccept(coupons -> Platform.runLater(() -> {
                    if (coupons != null && !coupons.isEmpty()) {
                        couponTable.setItems(FXCollections.observableArrayList(coupons));
                    } else {
                        couponTable.setItems(FXCollections.emptyObservableList());
                        showAlert("اطلاعات", "کوپنی یافت نشد.", Alert.AlertType.INFORMATION, null);
                    }
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        if (e.getCause() instanceof FrontendServiceException fsException)
                            showAlert(fsException);
                        else
                            showAlert("خطا", "لیست کوپن‌ها بارگیری نشد: " + e.getMessage(), Alert.AlertType.ERROR, null);
                    });
                    return null;
                });
    }

    private void handleEditCoupon(CouponResponse coupon) {
        navigateTo(
                couponTable,
                "/com/aut/shoomal/views/CreateEditCouponView.fxml",
                "/com/aut/shoomal/styles/AdminDashboardStyles.css",
                TransitionType.SLIDE_RIGHT,
                (CreateEditCouponController controller) -> {
                    controller.setCouponId(coupon.getId());
                    controller.setLoggedInUser(this.loggedInUser);
                }
        );
    }

    private void handleDeleteCoupon(Integer couponId) {
        if (token == null || token.isEmpty()) {
            showAlert("خطای احراز هویت", "ادمین وارد نشده است. لطفا ابتدا وارد شوید.", Alert.AlertType.ERROR, null);
            navigateToSignInView(couponTable);
            return;
        }

        adminDataService.deleteCoupon(token, couponId)
                .thenAccept(apiResponse -> Platform.runLater(() -> {
                    if (apiResponse.isSuccess()) {
                        showAlert("موفقیت", "کوپن با موفقیت حذف شد.", Alert.AlertType.INFORMATION, null);
                        loadCoupons();
                    } else {
                        showAlert("خطا", "خطا در حذف کوپن: " + apiResponse.getError(), Alert.AlertType.ERROR, null);
                    }
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        if (e.getCause() instanceof FrontendServiceException fsException)
                            showAlert(fsException);
                        else
                            showAlert("خطا", "خطا در حذف کوپن: " + e.getMessage(), Alert.AlertType.ERROR, null);
                    });
                    return null;
                });
    }

    @FXML
    private void handleBackToAdminDashboard() {
        navigateTo(
                backButton,
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