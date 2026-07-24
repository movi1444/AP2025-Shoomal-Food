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
import javafx.stage.Stage;
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

public abstract class AbstractBaseController implements Initializable
{
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

    protected void addTextDirectionListener(TextField... control) {
        for (TextField i : control)
            addDirectionListenerForObservableString(i.textProperty(), i);
    }

    protected void addTextAreaDirectionListener(TextArea... control) {
        for (TextArea i : control)
            addDirectionListenerForObservableString(i.textProperty(), i);
    }

    @SafeVarargs
    protected final void addChoiceBoxDirectionListener(ChoiceBox<String>... choiceBox) {
        for (ChoiceBox<String> i : choiceBox)
            addDirectionListenerForObservableString(i.valueProperty(), i);
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

    protected <C extends AbstractBaseController> void navigateTo(Node currentNode, String fxmlPath, String cssPath, TransitionType transitionType, Consumer<C> controllerSetupCallback)
    {
        if (currentNode == null || currentNode.getScene() == null)
        {
            showAlert("Navigation Error", "Current node or scene is null.", Alert.AlertType.ERROR, null);
            return;
        }

        Stage stage = (Stage) currentNode.getScene().getWindow();
        Parent currentRoot = currentNode.getScene().getRoot();
        performNavigation(stage, currentRoot, fxmlPath, cssPath, transitionType, controllerSetupCallback);
    }

    protected <C extends AbstractBaseController> void navigateTo(MenuItem menuItem, String fxmlPath, String cssPath, TransitionType transitionType, Consumer<C> controllerSetupCallback)
    {
        Stage stage = resolveStageFromMenuItem(menuItem);
        if (stage == null || stage.getScene() == null)
        {
            showAlert("Navigation Error", "Could not determine current window for navigation.", Alert.AlertType.ERROR, null);
            return;
        }

        Parent currentRoot = stage.getScene().getRoot();
        performNavigation(stage, currentRoot, fxmlPath, cssPath, transitionType, controllerSetupCallback);
    }

    private <C extends AbstractBaseController> void performNavigation(Stage stage, Parent currentRoot, String fxmlPath, String cssPath, TransitionType transitionType, Consumer<C> controllerSetupCallback)
    {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(fxmlPath)));
            Parent newRoot = loader.load();

            C newController = loader.getController();
            if (newController != null && controllerSetupCallback != null)
                controllerSetupCallback.accept(newController);

            StackPane transitionContainer = new StackPane(currentRoot, newRoot);

            Scene newScene = new Scene(
                    transitionContainer,
                    Math.max(stage.getWidth() - 15, 100),
                    Math.max(stage.getHeight() - 38, 100)
            );
            if (cssPath != null && !cssPath.isBlank())
                newScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(cssPath)).toExternalForm());
            stage.setScene(newScene);

            ParallelTransition animation = createSlideTransition(stage, currentRoot, newRoot, transitionType);

            animation.setOnFinished(event -> {
                transitionContainer.getChildren().remove(currentRoot);
                newRoot.setTranslateX(0);
                newRoot.setTranslateY(0);
            });

            animation.play();

        } catch (IOException e) {
            System.err.println("Failed to load layout/style: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private ParallelTransition createSlideTransition(Stage stage, Parent currentRoot, Parent newRoot, TransitionType transitionType) {
        Duration duration = Duration.millis(500);
        TranslateTransition slideOut = new TranslateTransition(duration, currentRoot);
        TranslateTransition slideIn = new TranslateTransition(duration, newRoot);

        double width = stage.getWidth();
        double height = stage.getHeight();

        if (transitionType == TransitionType.SLIDE_UP)
        {
            slideOut.setFromY(0);
            slideOut.setToY(height);

            newRoot.setTranslateY(height);
            slideIn.setFromY(height);
            slideIn.setToY(0);

        }
        else if (transitionType == TransitionType.SLIDE_RIGHT)
        {
            slideOut.setFromX(0);
            slideOut.setToX(-width);

            newRoot.setTranslateX(width);
            slideIn.setFromX(width);
            slideIn.setToX(0);

        }
        else
        {
            slideOut.setFromX(0);
            slideOut.setToX(width);

            newRoot.setTranslateX(-width);
            slideIn.setFromX(-width);
            slideIn.setToX(0);
        }

        return new ParallelTransition(slideOut, slideIn);
    }

    private Stage resolveStageFromMenuItem(MenuItem menuItem)
    {
        if (menuItem == null)
            return null;

        Menu parentMenu = menuItem.getParentMenu();
        if (parentMenu != null)
        {
            MenuBar menuBar = findMenuBar(parentMenu);
            if (menuBar != null && menuBar.getScene() != null)
                return (Stage) menuBar.getScene().getWindow();

            if (parentMenu.getParentPopup() != null)
            {
                ContextMenu contextMenu = parentMenu.getParentPopup();
                if (contextMenu.getOwnerWindow() instanceof Stage)
                    return (Stage) contextMenu.getOwnerWindow();
            }
        }

        if (menuItem.getParentPopup() != null && menuItem.getParentPopup().getOwnerWindow() instanceof Stage)
            return (Stage) menuItem.getParentPopup().getOwnerWindow();

        return Stage.getWindows().stream()
                .filter(w -> w instanceof Stage && w.isShowing())
                .map(w -> (Stage) w)
                .findFirst()
                .orElse(null);
    }

    private MenuBar findMenuBar(Menu menu)
    {
        Menu current = menu;
        while (current.getParentMenu() != null)
            current = current.getParentMenu();

        if (current.getParentPopup() != null && current.getParentPopup().getOwnerNode() != null)
        {
            Node ownerNode = current.getParentPopup().getOwnerNode();
            if (ownerNode.getScene() != null)
                return (MenuBar) ownerNode.getScene().getRoot().lookup(".menu-bar");
        }
        return null;
    }

    protected void navigateTo(MenuItem menuItem, String fxmlPath, String cssPath, TransitionType transitionType)
    {
        navigateTo(menuItem, fxmlPath, cssPath, transitionType, null);
    }

    protected void navigateTo(Node node, String fxmlPath, String cssPath, TransitionType transitionType)
    {
        navigateTo(node, fxmlPath, cssPath, transitionType, null);
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

    protected void navigateToSignInView(Node currentNode, TransitionType transitionType)
    {
        navigateTo(currentNode, "/com/aut/shoomal/views/SignInView.fxml", "/com/aut/shoomal/styles/SignInUpStyles.css", transitionType);
    }

    protected void navigateToSignInView(Node currentNode)
    {
        navigateTo(currentNode, "/com/aut/shoomal/views/SignInView.fxml", "/com/aut/shoomal/styles/SignInUpStyles.css", TransitionType.SLIDE_UP);
    }

    protected void navigateToUserProfileView(Object sourceNode)
    {
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }
}