package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.response.TransactionResponse;
import com.aut.shoomal.dto.response.UserResponse;
import com.aut.shoomal.exceptions.FrontendServiceException;
import com.aut.shoomal.service.AdminDataService;
import com.aut.shoomal.utils.PreferencesManager;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminTransactionsController extends AbstractBaseController {

    @FXML private TableView<TransactionResponse> transactionsTableView;
    @FXML private TableColumn<TransactionResponse, Integer> transactionIdColumn;
    @FXML private TableColumn<TransactionResponse, String> userNameColumn;
    @FXML private TableColumn<TransactionResponse, Integer> orderIdColumn;
    @FXML private TableColumn<TransactionResponse, String> statusColumn;
    @FXML private TableColumn<TransactionResponse, String> methodColumn;
    @FXML private TableColumn<TransactionResponse, String> dateColumn;
    @FXML private TableColumn<TransactionResponse, BigDecimal> amountColumn;

    @FXML private TextField searchTextField;
    @FXML private ComboBox<UserResponse> userComboBox;
    @FXML private ComboBox<String> methodFilterComboBox;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private VBox filterSidebar;
    @FXML private Button backButton;

    private AdminDataService adminDataService;
    private String token;
    private UserResponse loggedInUser;
    private boolean isFilterSidebarVisible = false;

    private final ObservableList<UserResponse> allUsers = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        adminDataService = new AdminDataService();
        token = PreferencesManager.getJwtToken();

        setupTransactionsTable();
        setupFilterComboBoxes();
        loadTransactions(null, null, null, null);

        if (filterSidebar != null) {
            filterSidebar.setVisible(false);
            filterSidebar.setManaged(false);
        }
    }

    public void setLoggedInUser(UserResponse user) {
        this.loggedInUser = user;
    }

    private void setupTransactionsTable() {
        transactionsTableView.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        transactionsTableView.setEditable(false);

        transactionIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        methodColumn.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));

        transactionsTableView.getSortOrder().clear();
        transactionsTableView.getSortOrder().add(transactionIdColumn);
        transactionIdColumn.setSortType(TableColumn.SortType.ASCENDING);
    }

    private void setupFilterComboBoxes() {
        userComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(UserResponse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName());
            }
        });
        userComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(UserResponse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "انتخاب کاربر" : item.getName());
            }
        });
        userComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(UserResponse user) {
                return user == null ? "" : user.getName();
            }

            @Override
            public UserResponse fromString(String string) {
                if (string == null || string.trim().isEmpty()) {
                    return null;
                }
                return allUsers.stream()
                        .filter(user -> user.getName().equalsIgnoreCase(string))
                        .findFirst()
                        .orElse(null);
            }
        });
        ObservableList<String> methods = FXCollections.observableArrayList(
                "همه روش‌ها",
                "wallet",
                "online"
        );
        methodFilterComboBox.setItems(methods);
        methodFilterComboBox.getSelectionModel().selectFirst();

        ObservableList<String> statuses = FXCollections.observableArrayList(
                "همه وضعیت‌ها",
                "Completed",
                "Failed",
                "Refunded"
        );
        statusFilterComboBox.setItems(statuses);
        statusFilterComboBox.getSelectionModel().selectFirst();

        loadAllFilterDataForComboBoxes();
    }

    private void loadAllFilterDataForComboBoxes() {
        if (token == null || token.isEmpty()) {
            showAlert("خطای احراز هویت", "ادمین وارد نشده است. لطفا ابتدا وارد شوید.", Alert.AlertType.ERROR, null);
            return;
        }

        adminDataService.getAllUsers(token)
                .thenAccept(users -> Platform.runLater(() -> {
                    if (users != null && !users.isEmpty()) {
                        allUsers.setAll(users.stream()
                                .map(user -> new UserResponse(user.getId(), user.getName(), user.getPhoneNumber(), user.getEmail(), user.getRole(), user.getAddress(), user.getProfileImageBase64(), user.getBank()))
                                .collect(Collectors.toList()));
                        userComboBox.setItems(allUsers);
                        addAutoCompleteForUsers(userComboBox, allUsers);
                    } else {
                        System.out.println("No users found for populating filter dropdowns.");
                    }
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        if (e.getCause() instanceof FrontendServiceException fsException)
                            showAlert(fsException);
                        else
                            showAlert("خطا", "Failed to load users for filter dropdowns: " + e.getMessage(), Alert.AlertType.ERROR, null);
                    });
                    return null;
                });
    }

    private void addAutoCompleteForUsers(ComboBox<UserResponse> comboBox, ObservableList<UserResponse> originalItems) {
        comboBox.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            UserResponse selected = comboBox.getSelectionModel().getSelectedItem();
            if (selected != null && selected.getName().equalsIgnoreCase(newValue)) {
                return;
            }

            if (newValue == null || newValue.isEmpty()) {
                comboBox.setItems(originalItems);
                comboBox.getSelectionModel().clearSelection();
                return;
            }

            comboBox.hide();
            ObservableList<UserResponse> filteredItems = originalItems.stream()
                    .filter(user -> user.getName().toLowerCase().contains(newValue.toLowerCase()) ||
                            String.valueOf(user.getId()).contains(newValue))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));

            comboBox.setItems(filteredItems);
            comboBox.show();
        });

        comboBox.setPromptText("انتخاب کاربر");
        comboBox.setEditable(true);
    }

    private void loadTransactions(String search, String userId, String method, String status) {
        if (token == null || token.isEmpty()) {
            showAlert("خطای احراز هویت", "ادمین وارد نشده است. لطفا ابتدا وارد شوید.", Alert.AlertType.ERROR, null);
            navigateToSignInView(transactionsTableView);
            return;
        }

        adminDataService.getAllTransactions(token, search, userId, method, status)
                .thenAccept(transactions -> Platform.runLater(() -> {
                    if (transactions != null && !transactions.isEmpty()) {
                        ObservableList<TransactionResponse> filteredTransactions = FXCollections.observableArrayList(transactions);
                        filteredTransactions.sort(Comparator.comparing(TransactionResponse::getId));
                        transactionsTableView.setItems(filteredTransactions);
                        transactionsTableView.sort();
                    } else {
                        transactionsTableView.setItems(FXCollections.emptyObservableList());
                        showAlert("اطلاعات", "تراکنشی با این فیلترها یافت نشد.", Alert.AlertType.INFORMATION, null);
                    }
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        if (e.getCause() instanceof FrontendServiceException fsException)
                            showAlert(fsException);
                        else
                            showAlert("خطا", "لیست تراکنش‌ها بارگیری نشد: " + e.getMessage(), Alert.AlertType.ERROR, null);
                    });
                    return null;
                });
    }

    @FXML
    private void handleFilterTransactions() {
        TranslateTransition transition = new TranslateTransition(Duration.millis(500), filterSidebar);
        if (isFilterSidebarVisible) {
            transition.setToX(filterSidebar.getWidth());
            transition.setOnFinished(event -> {
                filterSidebar.setVisible(false);
                filterSidebar.setManaged(false);
            });
        } else {
            filterSidebar.setVisible(true);
            filterSidebar.setManaged(true);
            transition.setFromX(filterSidebar.getWidth());
            transition.setToX(0);
        }
        transition.play();
        isFilterSidebarVisible = !isFilterSidebarVisible;
    }

    @FXML
    private void applyTransactionFilters() {
        String search = searchTextField.getText();
        UserResponse selectedUser = userComboBox.getSelectionModel().getSelectedItem();
        String userId = selectedUser != null ? String.valueOf(selectedUser.getId()) : null;
        String method = methodFilterComboBox.getValue();
        String status = statusFilterComboBox.getValue();

        loadTransactions(
                search != null && !search.isEmpty() ? search : null,
                userId,
                method,
                status
        );
        handleFilterTransactions();
    }

    @FXML
    private void clearTransactionFilters() {
        searchTextField.clear();
        userComboBox.getSelectionModel().clearSelection();
        userComboBox.setItems(allUsers);
        userComboBox.getEditor().clear();
        methodFilterComboBox.getSelectionModel().selectFirst();
        statusFilterComboBox.getSelectionModel().selectFirst();
        loadTransactions(null, null, null, null);
        handleFilterTransactions();
    }

    @FXML
    private void handleBackToAdminDashboard() {
        navigateTo(
                backButton,
                "/com/aut/shoomal/views/MainView.fxml",
                "/com/aut/shoomal/styles/MainView.css",
                TransitionType.SLIDE_LEFT,
                (MainController controller) -> controller.setLoggedInUser(loggedInUser)
        );
    }
}