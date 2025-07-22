package com.aut.shoomal.controllers;

import com.aut.shoomal.service.RestaurantService;
import com.aut.shoomal.utils.PreferencesManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

import java.net.URL;
import java.util.ResourceBundle;

public class BuyerShowMenuFoodsController extends AbstractBaseController
{
    @FXML private FlowPane foodsContainerFlowPane;
    @FXML private Label menuTitleLabel;
    @FXML private Button backButton;

    private RestaurantService restaurantService;
    private Integer restaurantId;
    private String title, token;
    private boolean ready = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        super.initialize(url, resourceBundle);
        restaurantService = new RestaurantService();
        token = PreferencesManager.getJwtToken();
    }

    private void loadFoods()
    {

    }

    public void setRestaurantId(Integer restaurantId)
    {
        this.restaurantId = restaurantId;
        isReadyToLoad();
    }

    public void setTitle(String title)
    {
        this.title = title;
        isReadyToLoad();
    }

    private void isReadyToLoad()
    {
        if (restaurantId != null && title != null && !title.isEmpty() && !ready)
        {
            ready = true;
            loadFoods();
        }
    }

    @FXML
    public void handleBack(ActionEvent actionEvent)
    {
        navigateTo(
                (Node) actionEvent.getSource(),
                "/com/aut/shoomal/views/BuyerRestaurantMenuView.fxml",
                "/com/aut/shoomal/styles/MainView.css",
                TransitionType.SLIDE_LEFT,
                controller -> {
                    if (controller instanceof BuyerShowListMenuController menuController)
                        menuController.setRestaurantId(restaurantId);
                }
        );
    }
}