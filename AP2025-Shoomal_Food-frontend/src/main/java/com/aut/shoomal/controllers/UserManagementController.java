package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.response.UserResponse;
import com.aut.shoomal.dto.response.AdminUserResponse;
import com.aut.shoomal.exceptions.FrontendServiceException;
import com.aut.shoomal.service.AdminDataService;
import com.aut.shoomal.dto.request.UpdateApprovalRequest;
import com.aut.shoomal.utils.PreferencesManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Button;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.geometry.NodeOrientation;
import javafx.beans.property.SimpleStringProperty;


import java.net.URL;
import java.util.ResourceBundle;
import java.util.Map;
import java.util.HashMap;

public class UserManagementController extends AbstractBaseController {
    @FXML private TableView<AdminUserResponse> userTable;
    @FXML private TableColumn<AdminUserResponse, String> approvalStatusColumn;
    @FXML private Button saveChangesButton;

    @FXML private TableColumn<AdminUserResponse, Long> idColumn;

    private AdminDataService adminDataService;
    private String token;
    private final Map<Long, String> pendingApprovalChanges = new HashMap<>();
    private UserResponse loggedInUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        adminDataService = new AdminDataService();
        token = PreferencesManager.getJwtToken();

        setupTableColumns();
        loadUsers();
    }

    public void setLoggedInUser(UserResponse user) {
        this.loggedInUser = user;
    }

    private void setupTableColumns() {
        userTable.setEditable(true);
        userTable.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        idColumn = (TableColumn<AdminUserResponse, Long>) userTable.getColumns().getFirst();
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setStyle("-fx-alignment: center-right;");

        TableColumn<AdminUserResponse, String> fullNameColumn = (TableColumn<AdminUserResponse, String>) userTable.getColumns().get(1);
        fullNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
        fullNameColumn.setStyle("-fx-alignment: center-right;");

        TableColumn<AdminUserResponse, String> phoneColumn = (TableColumn<AdminUserResponse, String>) userTable.getColumns().get(2);
        phoneColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getPhoneNumber()));
        phoneColumn.setStyle("-fx-alignment: center-right;");

        TableColumn<AdminUserResponse, String> emailColumn = (TableColumn<AdminUserResponse, String>) userTable.getColumns().get(3);
        emailColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getEmail()));
        emailColumn.setStyle("-fx-alignment: center-right;");

        TableColumn<AdminUserResponse, String> roleColumn = (TableColumn<AdminUserResponse, String>) userTable.getColumns().get(4);
        roleColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getRole()));
        roleColumn.setStyle("-fx-alignment: center-right;");

        approvalStatusColumn.setCellValueFactory(param -> {
            AdminUserResponse user = param.getValue();
            String role = user.getRole();
            if ("seller".equalsIgnoreCase(role) || "courier".equalsIgnoreCase(role)) {
                return new SimpleStringProperty(pendingApprovalChanges.getOrDefault(user.getId(), user.getUserStatus()));
            } else {
                return new SimpleStringProperty("N/A");
            }
        });

        approvalStatusColumn.setCellFactory(column -> {
            ComboBoxTableCell<AdminUserResponse, String> cell = new ComboBoxTableCell<>(FXCollections.observableArrayList("approved", "rejected")) {
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        AdminUserResponse user = getTableView().getItems().get(getIndex());
                        String role = user.getRole();
                        if ("seller".equalsIgnoreCase(role) || "courier".equalsIgnoreCase(role)) {
                            setEditable(true);
                            if (item == null || item.isEmpty()) {
                                setText(pendingApprovalChanges.containsKey(user.getId()) ? pendingApprovalChanges.get(user.getId()) : user.getUserStatus());
                                if (getText() == null || getText().isEmpty() || "N/A".equals(getText())) {
                                    setText("انتخاب کنید");
                                }
                            } else {
                                setText(item);
                            }
                        } else {
                            setEditable(false);
                            setText("N/A");
                        }
                        this.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                    }
                }
            };
            cell.setStyle("-fx-alignment: center-right;");
            return cell;
        });

        approvalStatusColumn.setOnEditCommit(event -> {
            AdminUserResponse user = event.getRowValue();
            String newValue = event.getNewValue();
            if (newValue != null) {
                pendingApprovalChanges.put(user.getId(), newValue);
                userTable.refresh();
            }
        });
    }

    private void loadUsers() {
        if (token == null || token.isEmpty()) {
            showAlert("خطای احراز هویت", "ادمین وارد نشده است. لطفا ابتدا وارد شوید.", Alert.AlertType.ERROR, null);
            navigateTo(userTable, "/com/aut/shoomal/views/SignInView.fxml", "/com/aut/shoomal/styles/SignInUpStyles.css", TransitionType.SLIDE_LEFT);
            return;
        }

        adminDataService.getAllUsers(token)
                .thenAccept(users -> Platform.runLater(() -> {
                    if (users != null && !users.isEmpty()) {
                        ObservableList<AdminUserResponse> nonAdminUsers = FXCollections.observableArrayList();
                        for (AdminUserResponse user : users) {
                            if (!"admin".equalsIgnoreCase(user.getRole())) {
                                nonAdminUsers.add(user);
                            }
                        }
                        userTable.setItems(nonAdminUsers);
                    } else {
                        System.out.println("کاربر غیر ادمینی برای نمایش وجود ندارد.");
                    }
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        if (e.getCause() instanceof FrontendServiceException fsException)
                            showAlert(fsException);
                        else
                            showAlert("خطا", "لیست کاربران بارگیری نشد: " + e.getMessage(), Alert.AlertType.ERROR, null);
                    });
                    return null;
                });
    }

    @FXML
    private void handleSaveChanges() {
        if (pendingApprovalChanges.isEmpty()) {
            showAlert("بدون تغییر", "هیچ تغییری برای ذخیره وجود ندارد.", Alert.AlertType.INFORMATION, null);
            return;
        }

        Map<Long, String> changesToApply = new HashMap<>(pendingApprovalChanges);

        for (Map.Entry<Long, String> entry : changesToApply.entrySet()) {
            Long userId = entry.getKey();
            String statusString = entry.getValue();

            UpdateApprovalRequest request = new UpdateApprovalRequest();
            request.setStatus(statusString);

            adminDataService.updateUserApprovalStatus(String.valueOf(userId), request, token)
                    .thenAccept(apiResponse -> Platform.runLater(() -> {
                        if (apiResponse.isSuccess()) {
                            showAlert("موفقیت", "وضعیت کاربر با موفقیت به " + statusString.toLowerCase() + " تغییر یافت.", Alert.AlertType.INFORMATION, null);
                            pendingApprovalChanges.remove(userId);
                            loadUsers();
                        } else {
                            showAlert("خطا", "خطا در به‌روزرسانی وضعیت کاربر " + userId + ": " + apiResponse.getError(), Alert.AlertType.ERROR, null);
                        }
                    }))
                    .exceptionally(e -> {
                        Platform.runLater(() -> {
                            if (e.getCause() instanceof FrontendServiceException fsException)
                                showAlert(fsException);
                            else
                                showAlert("خطا", "خطا در به‌روزرسانی وضعیت کاربر " + userId + ": " + e.getMessage(), Alert.AlertType.ERROR, null);
                        });
                        return null;
                    });
        }
    }

    @FXML
    private void handleBackButton() {
        navigateTo(
                userTable,
                "/com/aut/shoomal/views/MainView.fxml",
                "/com/aut/shoomal/styles/MainView.css",
                TransitionType.SLIDE_LEFT,
                (MainController controller) -> controller.setLoggedInUser(loggedInUser)
        );
    }
}