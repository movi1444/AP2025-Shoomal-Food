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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.io.IOException;


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
            navigateTo(saveButton, "/com/aut/shoomal/views/SignInView.fxml", "/com/aut/shoomal/styles/SignInUpStyles.css", TransitionType.SLIDE_LEFT);
            return;
        }

        profileService.getProfile(token)
                .thenAccept(userResponse -> {
                    Platform.runLater(() -> {
                        if (userResponse != null)
                        {
                            this.userType = userResponse.getRole();
                            if (nameField != null) nameField.setText(userResponse.getName());
                            if (phoneField != null) phoneField.setText(userResponse.getPhoneNumber());
                            if (emailField != null) emailField.setText(userResponse.getEmail());
                            if (addressField != null) addressField.setText(userResponse.getAddress());

                            if (userResponse.getBank() != null)
                            {
                                if (bankInfoSection != null) {
                                    bankInfoSection.setVisible(true);
                                    bankInfoSection.setManaged(true);
                                }
                                if (userResponse.getBank().getBankName() != null && bankNameField != null)
                                    bankNameField.setText(userResponse.getBank().getBankName());
                                if (userResponse.getBank().getAccountNumber() != null && bankAccountField != null)
                                    bankAccountField.setText(userResponse.getBank().getAccountNumber());
                            } else {
                                if (bankInfoSection != null) {
                                    bankInfoSection.setVisible(false);
                                    bankInfoSection.setManaged(false);
                                }
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
            navigateTo(saveButton, "/com/aut/shoomal/views/SignInView.fxml", "/com/aut/shoomal/styles/SignInUpStyles.css", TransitionType.SLIDE_LEFT);
            return;
        }

        UpdateProfileRequest request = getUpdateProfileRequest();

        profileService.updateProfile(request, token)
                .thenAccept(apiResponse -> {
                    Platform.runLater(() -> {
                        if (apiResponse.isSuccess()) {
                            showAlert("Success", "Profile saved successfully.", Alert.AlertType.INFORMATION, null);
                            navigateToUserProfileView(actionEvent.getSource());
                        } else {
                            showAlert("Error", "Failed to save profile: " + apiResponse.getError(), Alert.AlertType.ERROR, null);
                        }
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
        if (nameField != null) request.setFullName(nameField.getText());
        if (phoneField != null) request.setPhone(phoneField.getText());
        if (emailField != null) request.setEmail(emailField.getText());
        if (addressField != null) request.setAddress(addressField.getText());

        if (bankInfoSection != null && bankInfoSection.isVisible())
        {
            BankInfoResponse response = new BankInfoResponse();
            if (bankNameField != null) response.setBankName(bankNameField.getText());
            if (bankAccountField != null) response.setAccountNumber(bankAccountField.getText());
            request.setBankInfo(response);
        }
        return request;
    }

    @FXML
    public void handleCancelChange(ActionEvent actionEvent)
    {
        navigateToUserProfileView(actionEvent.getSource());
    }

    private void navigateToUserProfileView(Object sourceNode) {
        Stage stage = (Stage) ((Node) sourceNode).getScene().getWindow();
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/com/aut/shoomal/views/UserProfileView.fxml")));
            Parent profileRoot = loader.load();

            UserProfileController userProfileController = loader.getController();

            PreferencesManager.attemptAutoLogin();
            userProfileController.setLoggedInUser(PreferencesManager.getUserData());

            Scene newScene = new Scene(profileRoot, stage.getWidth() - 15, stage.getHeight() - 38);
            newScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/aut/shoomal/styles/MainView.css")).toExternalForm());
            stage.setScene(newScene);
            stage.setTitle("User Profile");
            stage.show();

        } catch (IOException e) {
            System.err.println("Failed to load UserProfileView.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert("Navigation Error", "Failed to load user profile page.", Alert.AlertType.ERROR, null);
        }
    }
}