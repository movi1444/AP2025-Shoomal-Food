package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.response.ListItemResponse;
import com.aut.shoomal.dto.response.RestaurantResponse;
import com.aut.shoomal.dto.response.UserResponse;
import com.aut.shoomal.exceptions.FrontendServiceException;
import com.aut.shoomal.service.BuyerService;
import com.aut.shoomal.utils.PreferencesManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Pos;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

public class BuyerDashboardContentController extends AbstractBaseController {

    @FXML private TextField searchBar;
    @FXML private Button searchButton;
    @FXML private ScrollPane searchResultsScrollPane;
    @FXML private VBox searchResultsContainer;
    @FXML private Label restaurantsHeader;
    @FXML private VBox popularRestaurantsVBox;
    @FXML private Label foodsHeader;
    @FXML private VBox popularFoodsVBox;
    @FXML private Hyperlink viewAllRestaurantsLink;
    @FXML private Hyperlink viewAllFoodsLink;

    private BuyerService buyerService;
    private String token;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private UserResponse loggedInUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        buyerService = new BuyerService();
        token = PreferencesManager.getJwtToken();

        if (searchButton != null) {
            searchButton.setOnAction(event -> handleSearch());
        }
        if (searchBar != null) {
            addTextDirectionListener(searchBar);
            searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.isEmpty()) {
                    clearSearchResults();
                }
            });
        }
        clearSearchResults();

        if (searchResultsContainer != null) {
            searchResultsContainer.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            searchResultsContainer.setAlignment(Pos.CENTER);
            searchResultsContainer.setMaxWidth(400);
        }


        if (viewAllRestaurantsLink != null) {
            viewAllRestaurantsLink.setOnAction(event -> handleViewAllRestaurants());
        }
        if (viewAllFoodsLink != null) {
            viewAllFoodsLink.setOnAction(event -> handleViewAllFoods());
        }
    }

    private void handleSearch() {
        String searchText = searchBar.getText().trim();
        if (searchText.isEmpty()) {
            clearSearchResults();
            showAlert("جستجو", "لطفاً کلمه کلیدی برای جستجو وارد کنید.", Alert.AlertType.WARNING, null);
            return;
        }

        if (token == null || token.isEmpty()) {
            showAlert("خطای احراز هویت", "کاربر وارد نشده است. لطفا ابتدا وارد شوید.", Alert.AlertType.ERROR, null);
            return;
        }

        buyerService.searchPopularRestaurantsAndFoods(token, searchText)
                .thenAccept(results -> Platform.runLater(() -> {
                    List<RestaurantResponse> restaurants = ((List<Map<String, Object>>) results.get("restaurants")).stream()
                            .map(json -> objectMapper.convertValue(json, RestaurantResponse.class))
                            .filter(Objects::nonNull)
                            .toList();

                    List<ListItemResponse> foods = ((List<Map<String, Object>>) results.get("foods")).stream()
                            .map(json -> objectMapper.convertValue(json, ListItemResponse.class))
                            .filter(Objects::nonNull)
                            .toList();

                    displaySearchResults(restaurants, foods, true);
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        if (e.getCause() instanceof FrontendServiceException fsException) {
                            showAlert(fsException);
                        } else {
                            showAlert("خطا", "خطای غیرمنتظره در جستجو: " + e.getMessage(), Alert.AlertType.ERROR, null);
                        }
                    });
                    return null;
                });
    }

    private void handleViewAllRestaurants() {
        String searchText = searchBar.getText().trim();
        if (searchText.isEmpty()) {
            showAlert("جستجو", "لطفاً کلمه کلیدی برای جستجو وارد کنید.", Alert.AlertType.WARNING, null);
            return;
        }
        if (token == null || token.isEmpty()) {
            showAlert("خطای احراز هویت", "کاربر وارد نشده است. لطفا ابتدا وارد شوید.", Alert.AlertType.ERROR, null);
            return;
        }

        buyerService.searchAllRestaurants(token, searchText)
                .thenAccept(restaurants -> Platform.runLater(() -> {
                    displaySearchResults(restaurants, List.of(), false);
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        if (e.getCause() instanceof FrontendServiceException fsException) {
                            showAlert(fsException);
                        } else {
                            showAlert("خطا", "خطای غیرمنتظره در مشاهده همه رستوران‌ها: " + e.getMessage(), Alert.AlertType.ERROR, null);
                        }
                    });
                    return null;
                });
    }

    private void handleViewAllFoods() {
        String searchText = searchBar.getText().trim();
        if (searchText.isEmpty()) {
            showAlert("جستجو", "لطفاً کلمه کلیدی برای جستجو وارد کنید.", Alert.AlertType.WARNING, null);
            return;
        }
        if (token == null || token.isEmpty()) {
            showAlert("خطای احراز هویت", "کاربر وارد نشده است. لطفا ابتدا وارد شوید.", Alert.AlertType.ERROR, null);
            return;
        }

        buyerService.searchAllFoods(token, searchText)
                .thenAccept(foods -> Platform.runLater(() -> {
                    displaySearchResults(List.of(), foods, false);
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        if (e.getCause() instanceof FrontendServiceException fsException) {
                            showAlert(fsException);
                        } else {
                            showAlert("خطا", "خطای غیرمنتظره در مشاهده همه غذاها: " + e.getMessage(), Alert.AlertType.ERROR, null);
                        }
                    });
                    return null;
                });
    }

    private void displaySearchResults(List<RestaurantResponse> restaurants, List<ListItemResponse> foods, boolean isPopularSearch) {
        popularRestaurantsVBox.getChildren().clear();
        popularFoodsVBox.getChildren().clear();

        restaurantsHeader.setVisible(true);
        restaurantsHeader.setManaged(true);
        foodsHeader.setVisible(true);
        foodsHeader.setManaged(true);
        viewAllRestaurantsLink.setVisible(true);
        viewAllRestaurantsLink.setManaged(true);
        viewAllFoodsLink.setVisible(true);
        viewAllFoodsLink.setManaged(true);

        if (restaurants != null && !restaurants.isEmpty()) {
            for (RestaurantResponse restaurant : restaurants) {
                popularRestaurantsVBox.getChildren().add(createRestaurantResultNode(restaurant));
            }
            viewAllRestaurantsLink.setText("مشاهده همه رستوران‌ها");
        }

        if (foods != null && !foods.isEmpty()) {
            for (ListItemResponse food : foods) {
                popularFoodsVBox.getChildren().add(createFoodResultNode(food));
            }
            viewAllFoodsLink.setText("مشاهده همه غذاها");
        }

        searchResultsScrollPane.setVisible(true);
        searchResultsScrollPane.setManaged(true);
    }


    private void clearSearchResults() {
        if (searchResultsScrollPane != null) {
            searchResultsScrollPane.setVisible(false);
            searchResultsScrollPane.setManaged(false);
        }
        popularRestaurantsVBox.getChildren().clear();
        popularFoodsVBox.getChildren().clear();
        restaurantsHeader.setVisible(false);
        restaurantsHeader.setManaged(false);
        foodsHeader.setVisible(false);
        foodsHeader.setManaged(false);
        viewAllRestaurantsLink.setVisible(false);
        viewAllRestaurantsLink.setManaged(false);
        viewAllFoodsLink.setVisible(false);
        viewAllFoodsLink.setManaged(false);
    }

    private Node createRestaurantResultNode(RestaurantResponse restaurant) {
        HBox hbox = new HBox(10);
        hbox.getStyleClass().add("search-result-item");
        hbox.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        ImageView icon = new ImageView();
        if (restaurant.getLogoBase64() != null && !restaurant.getLogoBase64().isEmpty()) {
            setProfileImage(icon, restaurant.getLogoBase64());
        } else {
            icon.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/aut/shoomal/images/icon.png"))));
        }
        icon.setFitWidth(40);
        icon.setFitHeight(40);

        Label nameLabel = new Label(restaurant.getName());
        nameLabel.getStyleClass().add("search-result-name");

        hbox.getChildren().addAll(icon, nameLabel);

        hbox.setOnMouseClicked(event -> {
            System.out.println("Restaurant clicked: " + restaurant.getName() + " (ID: " + restaurant.getId() + ")");
            showAlert("رستوران", "صفحه جزئیات رستوران " + restaurant.getName() + " هنوز پیاده‌سازی نشده است.", Alert.AlertType.INFORMATION, null);
        });

        return hbox;
    }

    private Node createFoodResultNode(ListItemResponse food) {
        HBox hbox = new HBox(10);
        hbox.getStyleClass().add("search-result-item");
        hbox.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        ImageView icon = new ImageView();
        if (food.getImageBase64() != null && !food.getImageBase64().isEmpty()) {
            setProfileImage(icon, food.getImageBase64());
        } else {
            icon.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/aut/shoomal/images/icon.png"))));
        }
        icon.setFitWidth(40);
        icon.setFitHeight(40);

        Label nameLabel = new Label(food.getName());
        nameLabel.getStyleClass().add("search-result-name");

        hbox.getChildren().addAll(icon, nameLabel);

        hbox.setOnMouseClicked(event -> {
            System.out.println("Food clicked: " + food.getName() + " (ID: " + food.getId() + ", Vendor ID: " + food.getVendorId() + ")");
            showAlert("غذا", "صفحه جزئیات غذا " + food.getName() + " هنوز پیاده‌سازی نشده است.", Alert.AlertType.INFORMATION, null);
        });

        return hbox;
    }
    public void setLoggedInUser(UserResponse currentUser) {
        this.loggedInUser = currentUser;
    }
}