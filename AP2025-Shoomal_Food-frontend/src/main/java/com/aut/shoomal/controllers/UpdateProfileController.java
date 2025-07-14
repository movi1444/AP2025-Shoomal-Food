package com.aut.shoomal.controllers;

import com.aut.shoomal.dto.request.UpdateProfileRequest;
import com.aut.shoomal.dto.response.BankInfoResponse;
import com.aut.shoomal.exceptions.FrontendServiceException;
import com.aut.shoomal.service.ProfileService;
import com.aut.shoomal.utils.PreferencesManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class UpdateProfileController extends AbstractBaseController
{
    @FXML private VBox bankInfoSection;
    @FXML private TextField bankNameField;
    @FXML private TextField bankAccountField;

    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField addressField;
    @FXML private TextField brandNameField;
    @FXML private TextArea descriptionArea;

    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private String userType, token;
    private ProfileService profileService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        this.profileService = new ProfileService();
        this.token = PreferencesManager.getJwtToken();
        addTextDirectionListener(nameField);
        addTextDirectionListener(phoneField);
        addTextDirectionListener(emailField);
        addTextDirectionListener(addressField);
        addTextDirectionListener(bankNameField);
        addTextDirectionListener(bankAccountField);
        addTextDirectionListener(brandNameField);
        addTextAreaDirectionListener(descriptionArea);
        loadUserProfile();
    }

    private void loadUserProfile()
    {
        if (token == null || token.isEmpty())
        {
            showAlert("Authentication Error", "User not logged in. Please log in first.", Alert.AlertType.ERROR, null);
            navigateTo(saveButton, "/com/aut/shoomal/views/SignUpView.fxml", "/com/aut/shoomal/styles/SignInUpStyles.css", TransitionType.SLIDE_LEFT);
            return;
        }

        profileService.getProfile(token)
                .thenAccept(userResponse -> {
                    Platform.runLater(() -> {
                        if (userResponse != null)
                        {
                            this.userType = userResponse.getRole();
                            nameField.setText(userResponse.getName());
                            phoneField.setText(userResponse.getPhoneNumber());
                            emailField.setText(userResponse.getEmail());
                            addressField.setText(userResponse.getAddress());
                            if (userResponse.getBank() != null)
                            {
                                if (userResponse.getBank().getBankName() != null)
                                    bankNameField.setText(userResponse.getBank().getBankName());
                                if (userResponse.getBank().getAccountNumber() != null)
                                    bankAccountField.setText(userResponse.getBank().getAccountNumber());
                            }
                        }
                    });
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        if (e.getCause() instanceof FrontendServiceException exception)
                            showAlert(exception);
                        else
                            showAlert("Unexpected Error", "An unexpected error occurred: " + e.getMessage(), Alert.AlertType.ERROR, null);
                    });
                    return null;
                });
    }

    @FXML
    public void handleSaveProfile(ActionEvent actionEvent)
    {
        if (token == null || token.isEmpty())
        {
            showAlert("Authentication Error", "User not logged in. Please log in first.", Alert.AlertType.ERROR, null);
            navigateTo(saveButton, "/com/aut/shoomal/views/SignUpView.fxml", "/com/aut/shoomal/styles/SignInUpStyles.css", TransitionType.SLIDE_LEFT);
            return;
        }

        UpdateProfileRequest request = getUpdateProfileRequest();

        profileService.updateProfile(request, token)
        .thenAccept(apiResponse -> {
            Platform.runLater(() -> {
              if (apiResponse.isSuccess())
                  showAlert("Success", "Profile saved successfully.", Alert.AlertType.INFORMATION, null);
              else
                  showAlert("Error", "Failed to save profile: " + apiResponse.getError(), Alert.AlertType.ERROR, null);
            });
        })
        .exceptionally(e -> {
            Platform.runLater(() -> {
                if (e.getCause() instanceof FrontendServiceException fsException)
                    showAlert(fsException);
                else
                    showAlert("Unexpected Error", "An unexpected error occurred: " + e.getMessage(), Alert.AlertType.ERROR, null);
            });
            return null;
        });
    }

    private UpdateProfileRequest getUpdateProfileRequest()
    {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName(nameField.getText());
        request.setPhone(phoneField.getText());
        request.setEmail(emailField.getText());
        request.setAddress(addressField.getText());
        if (bankInfoSection != null && bankInfoSection.isVisible())
        {
            BankInfoResponse response = new BankInfoResponse();
            response.setBankName(bankNameField.getText());
            response.setAccountNumber(bankAccountField.getText());
            request.setBankInfo(response);
        }
        return request;
    }

    @FXML
    public void handleCancelChange(ActionEvent actionEvent)
    {
        navigateTo(cancelButton, "/com/aut/shoomal/views/UserProfileView.fxml", "/com/aut/shoomal/styles/MainView.css", TransitionType.SLIDE_LEFT);
    }
}