package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.response.ListItemResponse;
import com.aut.shoomal.dto.response.RestaurantResponse;
import com.aut.shoomal.dto.response.UserResponse;
import com.aut.shoomal.exceptions.FrontendServiceException;
import com.aut.shoomal.service.BuyerService;
import com.aut.shoomal.utils.PreferencesManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
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

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class BuyerDashboardContentController extends AbstractBaseController {

    @FXML private TextField searchBar;
    @FXML private Button searchButton;
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

        if (restaurants != null && !restaurants.isEmpty()) {
            restaurantsHeader.setVisible(true);
            restaurantsHeader.setManaged(true);
            for (RestaurantResponse restaurant : restaurants) {
                popularRestaurantsVBox.getChildren().add(createRestaurantResultNode(restaurant));
            }
            if (isPopularSearch) {
                viewAllRestaurantsLink.setText("مشاهده همه رستوران‌ها");
                viewAllRestaurantsLink.setVisible(true);
                viewAllRestaurantsLink.setManaged(true);
            } else {
                viewAllRestaurantsLink.setVisible(false);
                viewAllRestaurantsLink.setManaged(false);
            }
        } else {
            restaurantsHeader.setVisible(false);
            restaurantsHeader.setManaged(false);
            viewAllRestaurantsLink.setVisible(false);
            viewAllRestaurantsLink.setManaged(false);
        }

        if (foods != null && !foods.isEmpty()) {
            foodsHeader.setVisible(true);
            foodsHeader.setManaged(true);
            for (ListItemResponse food : foods) {
                popularFoodsVBox.getChildren().add(createFoodResultNode(food));
            }
            if (isPopularSearch) {
                viewAllFoodsLink.setText("مشاهده همه غذاها");
                viewAllFoodsLink.setVisible(true);
                viewAllFoodsLink.setManaged(true);
            } else {
                viewAllFoodsLink.setVisible(false);
                viewAllFoodsLink.setManaged(false);
            }
        } else {
            foodsHeader.setVisible(false);
            foodsHeader.setManaged(false);
            viewAllFoodsLink.setVisible(false);
            viewAllFoodsLink.setManaged(false);
        }

        searchResultsContainer.setVisible(true);
        searchResultsContainer.setManaged(true);
    }

    private void clearSearchResults() {
        searchResultsContainer.setVisible(false);
        searchResultsContainer.setManaged(false);
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

        Label nameLabel = new Label(restaurant.getName());
        nameLabel.getStyleClass().add("search-result-name");

        ImageView icon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/aut/shoomal/images/restaurant_icon.png"))));
        icon.setFitWidth(20);
        icon.setFitHeight(20);
        hbox.getChildren().addAll(icon, nameLabel);
        return hbox;
    }

    private Node createFoodResultNode(ListItemResponse food) {
        HBox hbox = new HBox(10);
        hbox.getStyleClass().add("search-result-item");
        hbox.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        Label nameLabel = new Label(food.getName());
        nameLabel.getStyleClass().add("search-result-name");

        ImageView icon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/icon.png"))));
        icon.setFitWidth(20);
        icon.setFitHeight(20);

        hbox.getChildren().addAll(icon, nameLabel);
        return hbox;
    }
    public void setLoggedInUser(UserResponse currentUser) {
        this.loggedInUser = currentUser;
    }
}

