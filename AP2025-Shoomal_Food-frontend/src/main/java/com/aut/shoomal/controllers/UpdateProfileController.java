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
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import java.lang.Math;

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
    @FXML private ImageView profileImageView;
    @FXML private Button uploadImageButton;

    private String userType, token, firstPhoneNumber;
    private ProfileService profileService;
    private String profileImageBase64String;

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

        if (uploadImageButton != null) {
            uploadImageButton.setOnAction(event -> handleUploadImageButton());
        }

        if (profileImageView != null) {
            final double imageSize = 250.0;
            profileImageView.setFitWidth(imageSize);
            profileImageView.setFitHeight(imageSize);
            profileImageView.setPreserveRatio(true);

            Circle clip = new Circle(imageSize / 2, imageSize / 2, imageSize / 2);
            profileImageView.setClip(clip);

            profileImageView.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
                clip.setCenterX(newBounds.getWidth() / 2.0);
                clip.setCenterY(newBounds.getHeight() / 2.0);
                clip.setRadius(Math.min(newBounds.getWidth(), newBounds.getHeight()) / 2.0);
            });
        }
    }

    private void loadUserProfile()
    {
        if (token == null || token.isEmpty())
        {
            showAlert("Authentication Error", "User not logged in. Please log in first.", Alert.AlertType.ERROR, null);
            navigateTo(saveButton, "/com/aut/shoomal/views/SignInView.fxml", "/com/aut/shoomal/styles/SignInUpStyles.css", TransitionType.SLIDE_LEFT);
            return;
        }

        if (brandNameField != null) {
            brandNameField.setVisible(false);
            brandNameField.setManaged(false);
        }
        if (descriptionArea != null) {
            descriptionArea.setVisible(false);
            descriptionArea.setManaged(false);
        }


        profileService.getProfile(token)
                .thenAccept(userResponse -> Platform.runLater(() -> {
                    if (userResponse != null)
                    {
                        this.userType = userResponse.getRole();
                        if (nameField != null) nameField.setText(userResponse.getName());
                        if (phoneField != null)
                        {
                            firstPhoneNumber = userResponse.getPhoneNumber();
                            phoneField.setText(userResponse.getPhoneNumber());
                        }
                        if (emailField != null) emailField.setText(userResponse.getEmail());
                        if (addressField != null) addressField.setText(userResponse.getAddress());

                        super.setProfileImage(profileImageView, userResponse.getProfileImageBase64());
                        this.profileImageBase64String = userResponse.getProfileImageBase64();

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
                        }
                        else
                        if (bankInfoSection != null)
                        {
                            bankNameField.setText("N/A");
                            bankAccountField.setText("N/A");
                        }
                        if ("seller".equalsIgnoreCase(this.userType)) {
                            if (brandNameField != null) {
                                brandNameField.setVisible(true);
                                brandNameField.setManaged(true);
                            }
                            if (descriptionArea != null) {
                                descriptionArea.setVisible(true);
                                descriptionArea.setManaged(true);
                            }
                        }
                    }
                }))
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
        request.setProfileImageBase64(this.profileImageBase64String);

        profileService.updateProfile(request, token)
                .thenAccept(apiResponse -> Platform.runLater(() -> {
                    if (apiResponse.isSuccess()) {
                        if (phoneField != null)
                        {
                            if (!phoneField.getText().equals(firstPhoneNumber))
                            {
                                showAlert("Success", "Because of changing the phone number, your session has expired. Please login again.", Alert.AlertType.INFORMATION, null);
                                navigateToSignInView(saveButton);
                            }
                            else
                            {
                                showAlert("Success", "Profile saved successfully.", Alert.AlertType.INFORMATION, null);
                                navigateToUserProfileView(actionEvent.getSource());
                            }
                        }

                    } else
                        showAlert("Error", "Failed to save profile: " + apiResponse.getError(), Alert.AlertType.ERROR, null);
                }))
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

    @FXML
    private void handleUploadImageButton() {
        this.profileImageBase64String = super.handleImageUploadAndConvert(uploadImageButton, profileImageView);
    }
}