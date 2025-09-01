package com.aut.shoomal.controllers;

import com.aut.shoomal.exceptions.FrontendServiceException;
import com.aut.shoomal.utils.PreferencesManager;
import javafx.animation.TranslateTransition;
import javafx.animation.ParallelTransition;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import com.aut.shoomal.utils.ImageToBase64Converter;

public abstract class AbstractBaseController implements Initializable {
    public enum TransitionType {
        SLIDE_LEFT,
        SLIDE_UP,
        SLIDE_RIGHT
    }

    protected boolean isPersianCharacter(char c) {
        return (c >= '\u0600' && c <= '\u06FF') ||
                (c >= '\u0750' && c <= '\u077F') ||
                (c >= '\uFB50' && c <= '\uFDFF') ||
                (c >= '\uFE70' && c <= '\uFEFF');
    }

    protected <T extends Node> void addDirectionListenerForObservableString(ObservableValue<String> observable, T control) {
        if (observable == null || control == null) return;
        observable.addListener((obs, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                if (isPersianCharacter(newValue.charAt(0))) {
                    control.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                } else {
                    control.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                }
            } else {
                control.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            }
        });
    }

    protected void addTextDirectionListener(TextField control) {
        addDirectionListenerForObservableString(control.textProperty(), control);
    }

    protected void addTextAreaDirectionListener(TextArea control) {
        addDirectionListenerForObservableString(control.textProperty(), control);
    }

    protected void addChoiceBoxDirectionListener(ChoiceBox<String> choiceBox) {
        addDirectionListenerForObservableString(choiceBox.valueProperty(), choiceBox);
    }

    protected void addComboBoxDirectionListener(ComboBox<String> comboBox) {
        addDirectionListenerForObservableString(comboBox.getEditor().textProperty(), comboBox.getEditor());
    }

    protected void showAlert(String title, String message) {
        showAlert(title, message, AlertType.INFORMATION, null);
    }

    protected void showAlert(FrontendServiceException exception)
    {
        showAlert("Error", exception.getMessage(), AlertType.ERROR, null);
    }

    protected void showAlert(String title, String message, AlertType alertType, Image graphicImage) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        if (graphicImage != null) {
            ImageView imageView = new ImageView(graphicImage);
            imageView.setFitWidth(48);
            imageView.setFitHeight(48);
            alert.setGraphic(imageView);
        }
        alert.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/com/aut/shoomal/styles/AlertStyles.css")).toExternalForm()
        );
        alert.getDialogPane().getStyleClass().add("custom-alert");
        alert.getDialogPane().getStyleClass().add(alertType.toString().toLowerCase());

        addDirectionListenerForObservableString(alert.contentTextProperty(), alert.getDialogPane().lookup(".content.label"));

        alert.showAndWait();
    }

    protected void navigateTo(Node currentNode, String fxmlPath, String cssPath, TransitionType transitionType) {
        navigateTo(currentNode, fxmlPath, cssPath, transitionType, null);
    }

    protected <C extends AbstractBaseController> void navigateTo(Node currentNode, String fxmlPath, String cssPath, TransitionType transitionType, Consumer<C> controllerSetupCallback) {
        Stage stage = (Stage) currentNode.getScene().getWindow();
        Parent currentRoot = currentNode.getScene().getRoot();

        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(fxmlPath)));
            Parent newRoot = loader.load();

            C newController = loader.getController();
            if (newController != null && controllerSetupCallback != null) {
                controllerSetupCallback.accept(newController);
            }

            StackPane transitionContainer = new StackPane();
            transitionContainer.getChildren().addAll(currentRoot, newRoot);

            TranslateTransition currentRootSlideOut = new TranslateTransition(Duration.millis(500), currentRoot);
            TranslateTransition newRootSlideIn = new TranslateTransition(Duration.millis(500), newRoot);

            if (transitionType == TransitionType.SLIDE_UP) {
                currentRootSlideOut.setFromY(0);
                currentRootSlideOut.setToY(stage.getHeight());

                newRoot.setTranslateY(stage.getHeight());
                newRootSlideIn.setFromY(stage.getHeight());
                newRootSlideIn.setToY(0);

            } else if (transitionType == TransitionType.SLIDE_RIGHT) {
                currentRootSlideOut.setFromX(0);
                currentRootSlideOut.setToX(-stage.getWidth());

                newRoot.setTranslateX(stage.getWidth());
                newRootSlideIn.setFromX(stage.getWidth());
                newRootSlideIn.setToX(0);

            } else {
                currentRootSlideOut.setFromX(0);
                currentRootSlideOut.setToX(stage.getWidth());

                newRoot.setTranslateX(-stage.getWidth());
                newRootSlideIn.setFromX(-stage.getWidth());
                newRootSlideIn.setToX(0);
            }

            Scene newScene = new Scene(transitionContainer, stage.getWidth() - 15, stage.getHeight() - 38);
            newScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(cssPath)).toExternalForm());
            stage.setScene(newScene);

            ParallelTransition parallelTransition = new ParallelTransition();
            parallelTransition.getChildren().addAll(currentRootSlideOut, newRootSlideIn);

            parallelTransition.setOnFinished(event -> {
                transitionContainer.getChildren().remove(currentRoot);
                newRoot.setTranslateX(0);
                newRoot.setTranslateY(0);
            });

            parallelTransition.play();

        } catch (IOException e) {
            System.err.println("Failed to load .fxml or .css file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void navigateTo(MenuItem menuItem, String fxmlPath, String stylesheetPath, TransitionType transitionType)
    {
        navigateTo(menuItem, fxmlPath, stylesheetPath, transitionType, null);
    }

    protected void navigateToMainView(Node node)
    {
        navigateTo(
                node,
                "/com/aut/shoomal/views/MainView.fxml",
                "/com/aut/shoomal/styles/MainView.css",
                TransitionType.SLIDE_LEFT,
                (MainController controller) -> controller.setLoggedInUser(PreferencesManager.getUserData())
        );
    }

    protected <C extends AbstractBaseController> void navigateTo(MenuItem menuItem, String fxmlPath, String stylesheetPath, TransitionType transitionType, Consumer<C> controllerSetupCallback)
    {
        Stage stage = null;
        Node currentRoot = null;

        Menu parentMenu = menuItem.getParentMenu();
        if (parentMenu != null)
        {
            MenuBar menuBar = findMenuBar(parentMenu);
            if (menuBar != null)
            {
                stage = (Stage) menuBar.getScene().getWindow();
                currentRoot = stage.getScene().getRoot();
            }
            else if (parentMenu.getParentPopup() != null)
            {
                ContextMenu contextMenu = parentMenu.getParentPopup();
                if (contextMenu.getOwnerWindow() instanceof Stage)
                {
                    stage = (Stage) contextMenu.getOwnerWindow();
                    currentRoot = stage.getScene().getRoot();
                }
            }
        }

        if (stage == null || currentRoot == null)
        {
            System.err.println("Could not reliably determine Stage from MenuItem hierarchy. Attempting fallback to any active window.");
            if (!Stage.getWindows().isEmpty())
                for (Window window : Stage.getWindows())
                    if (window instanceof Stage && window.isShowing())
                    {
                        stage = (Stage) window;
                        currentRoot = stage.getScene().getRoot();
                        break;
                    }
        }

        if (stage == null || currentRoot == null)
        {
            System.err.println("Navigation Error: Could not determine current Stage or Root Node for navigation from MenuItem.");
            showAlert("Navigation Error", "Could not determine current window for navigation.", Alert.AlertType.ERROR, null);
            return;
        }

        navigateTo(currentRoot, fxmlPath, stylesheetPath, transitionType, controllerSetupCallback);
    }


    private MenuBar findMenuBar(MenuItem menuItem)
    {
        Menu parentMenu = menuItem.getParentMenu();
        while (parentMenu != null)
            parentMenu = parentMenu.getParentMenu();

        if (menuItem.getParentPopup() != null)
        {
            ContextMenu contextMenu = menuItem.getParentPopup();
            if (contextMenu.getOwnerNode() != null)
            {
                Node ownerNode = contextMenu.getOwnerNode();
                if (ownerNode.getScene() != null)
                    for (Node node : ownerNode.getScene().getRoot().lookupAll(".menu-bar"))
                        if (node instanceof MenuBar)
                            return (MenuBar) node;
            }
        }
        return null;
    }

    protected void navigateToSignInView(Node currentNode, TransitionType transitionType) {
        navigateTo(currentNode, "/com/aut/shoomal/views/SignInView.fxml", "/com/aut/shoomal/styles/SignInUpStyles.css", transitionType);
    }

    protected void navigateToSignInView(Node currentNode) {
        navigateTo(currentNode, "/com/aut/shoomal/views/SignInView.fxml", "/com/aut/shoomal/styles/SignInUpStyles.css", TransitionType.SLIDE_UP);
    }

    protected void navigateToUserProfileView(Object sourceNode) {
        navigateTo(
                (Node) sourceNode,
                "/com/aut/shoomal/views/UserProfileView.fxml",
                "/com/aut/shoomal/styles/MainView.css",
                TransitionType.SLIDE_LEFT,
                UserProfileController::setLoggedInUser
        );
    }

    protected String handleImageUploadAndConvert(Button uploadButton, ImageView imageView) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(uploadButton.getScene().getWindow());

        if (selectedFile != null) {
            try {
                String base64String = ImageToBase64Converter.convertImageFileToBase64(selectedFile.getAbsolutePath());
                System.out.println("Image uploaded and converted to Base64. Size: " + base64String.length() + " characters.");
                if (base64String != null && !base64String.isEmpty()) {
                    byte[] imageBytes = Base64.getDecoder().decode(base64String);
                    Image image = new Image(new ByteArrayInputStream(imageBytes));
                    if (imageView == null)
                        imageView = new ImageView();
                    imageView.setImage(image);
                }
                return base64String;
            } catch (IOException e) {
                System.err.println("Failed to convert image to Base64: " + e.getMessage());
                e.printStackTrace();
                showAlert("Image Error", "Failed to process image. Please try again.", AlertType.ERROR, null);
                return null;
            }
        }
        return null;
    }

    protected void setProfileImage(ImageView imageView, String base64Image) {
        if (imageView != null) {
            if (base64Image != null && !base64Image.isEmpty()) {
                try {
                    String cleanedBase64Image = base64Image.replaceAll("\\s", "");
                    byte[] imageBytes = Base64.getDecoder().decode(cleanedBase64Image);
                    Image image = new Image(new ByteArrayInputStream(imageBytes));

                    if (image.isError() || image.getWidth() == 0 || image.getHeight() == 0) {
                        throw new IllegalArgumentException("Decoded bytes do not form a valid image.");
                    }

                    imageView.setImage(image);
                    System.out.println("Profile image set from Base64.");
                } catch (Exception e) {
                    System.err.println("Failed to set profile image (invalid Base64 or image data): " + e.getMessage());
                    imageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/aut/shoomal/images/default_profile.png"))));
                }
            } else {
                imageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/aut/shoomal/images/default_profile.png"))));
            }
        }
    }

    protected void handleSeeComments(Integer foodId)
    {
        if (foodId == null) {
            showAlert("Error", "Food ID is missing for comments.", Alert.AlertType.ERROR, null);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aut/shoomal/views/FoodCommentsView.fxml"));
            Parent root = loader.load();

            FoodCommentsController foodCommentsController = loader.getController();
            if (foodCommentsController != null)
                foodCommentsController.setFoodId(foodId);

            Stage commentsStage = new Stage();
            commentsStage.initModality(Modality.APPLICATION_MODAL);
            commentsStage.setTitle("نظرات غذا");
            commentsStage.setScene(new Scene(root));
            commentsStage.setResizable(true);
            commentsStage.showAndWait();
        } catch (IOException e) {
            System.err.println("Failed to load FoodCommentsView.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert("Navigation Error", "Failed to load food comments page.", Alert.AlertType.ERROR, null);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }
}