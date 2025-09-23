package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.response.ListItemResponse;
import com.aut.shoomal.dto.response.RestaurantResponse;
import com.aut.shoomal.dto.response.UserResponse;
import com.aut.shoomal.exceptions.FrontendServiceException;
import com.aut.shoomal.service.BuyerFavoriteService;
import com.aut.shoomal.service.BuyerService;
import com.aut.shoomal.utils.PreferencesManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

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

    @FXML private Hyperlink viewActiveOrdersLink;
    @FXML private AnchorPane activeOrdersPanel;

    @FXML private FlowPane buyerFavoriteRestaurantsFlowPane;
    @FXML private FlowPane buyerOtherRestaurantsFlowPane;

    private BuyerService buyerService;
    private BuyerFavoriteService buyerFavoriteService;
    private String token;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private UserResponse loggedInUser;

    private BuyerActiveOrdersController activeOrdersController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        buyerService = new BuyerService();
        buyerFavoriteService = new BuyerFavoriteService();
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

        if (viewActiveOrdersLink != null) {
            viewActiveOrdersLink.setOnAction(event -> handleViewActiveOrders());
        }

        if (activeOrdersPanel != null) {
            activeOrdersPanel.setTranslateX(activeOrdersPanel.getPrefWidth());
            activeOrdersPanel.setVisible(false);
            activeOrdersPanel.setManaged(false);
        }

        if (buyerFavoriteRestaurantsFlowPane != null) {
            buyerFavoriteRestaurantsFlowPane.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        }
        if (buyerOtherRestaurantsFlowPane != null) {
            buyerOtherRestaurantsFlowPane.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        }
    }

    public void setLoggedInUser(UserResponse user) {
        this.loggedInUser = user;
    }

    public void loadBuyerDashboardContent(Void unused) {
        if (token == null || token.isEmpty()) {
            showAlert("Authentication Error", "User not logged in. Please log in first.", Alert.AlertType.ERROR, null);
            return;
        }

        if (buyerFavoriteRestaurantsFlowPane == null || buyerOtherRestaurantsFlowPane == null) {
            System.err.println("FlowPanes are not initialized. Check FXML for BuyerDashboardContentController.");
            return;
        }

        buyerFavoriteRestaurantsFlowPane.getChildren().clear();
        buyerOtherRestaurantsFlowPane.getChildren().clear();
        buyerFavoriteService.getFavoriteRestaurants(token)
                .thenCombine(buyerFavoriteService.getAllRestaurants(token), (favorites, allRestaurants) -> {
                    Platform.runLater(() -> {
                        if (favorites != null && !favorites.isEmpty()) {
                            for (RestaurantResponse restaurant : favorites) {
                                addRestaurantCard(restaurant, buyerFavoriteRestaurantsFlowPane, true);
                            }
                        } else {
                            buyerFavoriteRestaurantsFlowPane.getChildren().add(new Label("شما رستوران مورد علاقه‌ای ندارید."));
                        }

                        if (allRestaurants != null && !allRestaurants.isEmpty()) {
                            Set<Integer> favoriteIds = (favorites != null)
                                    ? favorites.stream().map(RestaurantResponse::getId).collect(Collectors.toSet())
                                    : new HashSet<>();

                            List<RestaurantResponse> otherRestaurants = allRestaurants.stream()
                                    .filter(r -> !favoriteIds.contains(r.getId()))
                                    .toList();
                            if (!otherRestaurants.isEmpty()) {
                                for (RestaurantResponse restaurant : otherRestaurants) {
                                    addRestaurantCard(restaurant, buyerOtherRestaurantsFlowPane, false);
                                }
                            } else {
                                buyerOtherRestaurantsFlowPane.getChildren().add(new Label("سایر رستوران‌ها در دسترس نیستند."));
                            }
                        } else {
                            buyerOtherRestaurantsFlowPane.getChildren().add(new Label("هیچ رستورانی در دسترس نیست."));
                        }
                    });
                    return null;
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        if (e.getCause() instanceof FrontendServiceException fsException) {
                            showAlert(fsException);
                        } else {
                            showAlert("Unexpected Error", "An unexpected error occurred while loading favorite restaurants: " + e.getMessage(), Alert.AlertType.ERROR, null);
                        }
                    });
                    return null;
                });
    }

    private void addRestaurantCard(RestaurantResponse restaurant, FlowPane targetFlowPane, boolean isFavorite) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aut/shoomal/views/components/FavoriteRestaurantCard.fxml"));
            VBox card = loader.load();
            FavoriteRestaurantCardController cardController = loader.getController();

            cardController.setRestaurantData(restaurant, (restaurantId) -> {
                if (loggedInUser != null) {
                    navigateTo(
                            card,
                            "/com/aut/shoomal/views/BuyerRestaurantView.fxml",
                            "/com/aut/shoomal/styles/MainView.css",
                            TransitionType.SLIDE_RIGHT,
                            controller -> {
                                if (controller instanceof BuyerShowRestaurantDetailsController detailsController) {
                                    detailsController.setLoggedInUser(loggedInUser);
                                    detailsController.setRestaurantId(restaurantId);
                                }
                            }
                    );
                } else {
                    showAlert("Authentication Error", "User not logged in. Cannot view restaurant details.", Alert.AlertType.ERROR, null);
                }
            }, isFavorite, this::loadBuyerDashboardContent);

            targetFlowPane.getChildren().add(card);
        } catch (IOException e) {
            System.err.println("Failed to load restaurant card: " + e.getMessage());
            e.printStackTrace();
            showAlert("Load Error", "Could not load a restaurant card.", Alert.AlertType.ERROR, null);
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
                    List<Map<String, Object>> restaurantsMap = (List<Map<String, Object>>) results.get("restaurants");
                    List<RestaurantResponse> restaurants = restaurantsMap.stream()
                            .map(json -> objectMapper.convertValue(json, RestaurantResponse.class))
                            .filter(Objects::nonNull)
                            .toList();

                    List<Map<String, Object>> foodsMap = (List<Map<String, Object>>) results.get("foods");
                    List<ListItemResponse> foods = foodsMap.stream()
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
                    if (restaurants != null && !restaurants.isEmpty())
                        displaySearchResults(restaurants, List.of(), false);
                    else
                        showAlert("خالی", "هیچ رستورانی با این کلمات پیدا نشد.", Alert.AlertType.INFORMATION, null);
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
                    if (foods != null && !foods.isEmpty())
                        displaySearchResults(List.of(), foods, false);
                    else
                        showAlert("خالی", "هیچ غذایی با این کلمات پیدا نشد.", Alert.AlertType.INFORMATION, null);
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
        hbox.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        hbox.setAlignment(Pos.CENTER_RIGHT);

        ImageView icon = new ImageView();
        if (restaurant.getLogoBase64() != null && !restaurant.getLogoBase64().isEmpty()) {
            setProfileImage(icon, restaurant.getLogoBase64());
        } else {
            icon.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/aut/shoomal/images/restaurant_icon.png"))));
        }
        icon.setFitWidth(45);
        icon.setFitHeight(45);

        Label nameLabel = new Label(restaurant.getName());
        nameLabel.getStyleClass().add("search-result-name");
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        nameLabel.setAlignment(Pos.CENTER);
        hbox.getChildren().addAll(nameLabel, icon);

        hbox.setOnMouseClicked(event -> {
            System.out.println("Restaurant clicked: " + restaurant.getName() + " (ID: " + restaurant.getId() + ")");
            if (loggedInUser != null) {
                navigateTo(
                        (Node) event.getSource(),
                        "/com/aut/shoomal/views/BuyerRestaurantView.fxml",
                        "/com/aut/shoomal/styles/MainView.css",
                        TransitionType.SLIDE_RIGHT,
                        controller -> {
                            if (controller instanceof BuyerShowRestaurantDetailsController detailsController) {
                                detailsController.setLoggedInUser(loggedInUser);
                                detailsController.setRestaurantId(restaurant.getId());
                            }
                        }
                );
            } else {
                showAlert("Authentication Error", "User not logged in. Cannot view restaurant details.", Alert.AlertType.ERROR, null);
            }
        });

        return hbox;
    }

    private Node createFoodResultNode(ListItemResponse food) {
        HBox hbox = new HBox(10);
        hbox.getStyleClass().add("search-result-item");
        hbox.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        hbox.setAlignment(Pos.CENTER_RIGHT);

        ImageView icon = new ImageView();
        if (food.getImageBase64() != null && !food.getImageBase64().isEmpty()) {
            setProfileImage(icon, food.getImageBase64());
        } else {
            icon.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/aut/shoomal/images/food_icon.png"))));
        }
        icon.setFitWidth(45);
        icon.setFitHeight(45);

        Label nameLabel = new Label(food.getName());
        nameLabel.getStyleClass().add("search-result-name");
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        nameLabel.setAlignment(Pos.CENTER);
        hbox.getChildren().addAll(nameLabel, icon);

        hbox.setOnMouseClicked(event -> {
            System.out.println("Food clicked: " + food.getName() + " (ID: " + food.getId() + ", Vendor ID: " + food.getVendorId() + ")");
            if (loggedInUser != null)
                navigateTo(
                        (Node) event.getSource(),
                        "/com/aut/shoomal/views/BuyerRestaurantView.fxml",
                        "/com/aut/shoomal/styles/MainView.css",
                        TransitionType.SLIDE_RIGHT,
                        controller -> {
                            if (controller instanceof BuyerShowRestaurantDetailsController detailsController) {
                                detailsController.setLoggedInUser(loggedInUser);
                                detailsController.setRestaurantId(food.getVendorId());
                            }
                        }
                );
        });
        return hbox;
    }

    @FXML
    private void handleViewActiveOrders() {
        if (token == null || token.isEmpty()) {
            showAlert("خطای احراز هویت", "کاربر وارد نشده است. لطفا ابتدا وارد شوید.", Alert.AlertType.ERROR, null);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/com/aut/shoomal/views/BuyerActiveOrdersView.fxml")));
            Node activeOrdersContent = loader.load();
            activeOrdersController = loader.getController();

            if (activeOrdersController != null) {
                activeOrdersController.setCloseCallback(this::closeActiveOrdersPanel);
            }

            activeOrdersPanel.getChildren().setAll(activeOrdersContent);
            activeOrdersPanel.setVisible(true);
            activeOrdersPanel.setManaged(true);

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(500), activeOrdersPanel);
            slideIn.setFromX(activeOrdersPanel.getPrefWidth());
            slideIn.setToX(0);
            slideIn.play();

            if (activeOrdersController != null) {
                activeOrdersController.loadActiveOrders();
            }

        } catch (IOException e) {
            System.err.println("Failed to load BuyerActiveOrdersView.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert("خطا", "خطا در بارگذاری صفحه سفارشات فعال.", Alert.AlertType.ERROR, null);
        }
    }

    public void closeActiveOrdersPanel(Void v) {
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(500), activeOrdersPanel);
        slideOut.setFromX(0);
        slideOut.setToX(activeOrdersPanel.getPrefWidth());
        slideOut.setOnFinished(event -> {
            activeOrdersPanel.setVisible(false);
            activeOrdersPanel.setManaged(false);
            activeOrdersPanel.getChildren().clear();
        });
        slideOut.play();
    }
}