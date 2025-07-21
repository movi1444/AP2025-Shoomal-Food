package com.aut.shoomal.controllers;

import com.aut.shoomal.utils.PreferencesManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class CourierFilterController extends AbstractBaseController
{
    @FXML private TextField searchField;
    @FXML private ChoiceBox<String> vendorChoiceBox;
    @FXML private ChoiceBox<String> userChoiceBox;
    @FXML private Button applyFilterButton;
    @FXML private Button cancelButton;

    private String token;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        super.initialize(url, resourceBundle);
        token = PreferencesManager.getJwtToken();
        addTextDirectionListener(searchField);
        addChoiceBoxDirectionListener(userChoiceBox);
        addChoiceBoxDirectionListener(vendorChoiceBox);
    }

    @FXML
    public void handleApplyFilter(ActionEvent actionEvent)
    {

    }

    @FXML
    public void handleCancel(ActionEvent actionEvent)
    {

    }
}