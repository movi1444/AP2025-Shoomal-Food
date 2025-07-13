package com.aut.shoomal.controllers;

import javafx.animation.TranslateTransition;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aut.shoomal.utils.ImageToBase64Converter;

public abstract class AbstractBaseController implements Initializable {
    public enum TransitionType {
        SLIDE_LEFT,
        SLIDE_UP
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

    protected void showAlert(String title, String message) {
        showAlert(title, message, AlertType.INFORMATION, null);
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

    protected void navigateToSignInView(Node currentNode, TransitionType transitionType) {
        Stage stage = (Stage) currentNode.getScene().getWindow();
        Parent currentRoot = currentNode.getScene().getRoot();

        try {
            Parent signInRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/aut/shoomal/views/SignInView.fxml")));

            StackPane transitionContainer = new StackPane();
            transitionContainer.getChildren().addAll(currentRoot, signInRoot);

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(500), signInRoot);

            if (transitionType == TransitionType.SLIDE_UP) {
                signInRoot.setTranslateY(stage.getHeight());
                slideIn.setFromY(stage.getHeight());
                slideIn.setToY(0);
            } else {
                signInRoot.setTranslateX(-stage.getWidth());
                slideIn.setFromX(-stage.getWidth());
                slideIn.setToX(0);
            }

            Scene newScene = new Scene(transitionContainer, stage.getWidth() - 15, stage.getHeight() - 38);
            newScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/aut/shoomal/styles/SignInUpStyles.css")).toExternalForm());
            stage.setScene(newScene);

            slideIn.setOnFinished(event -> {
                transitionContainer.getChildren().remove(currentRoot);
            });

            slideIn.play();

        } catch (IOException e) {
            System.err.println("Failed to load SignInView.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void navigateToSignInView(Node currentNode) {
        navigateToSignInView(currentNode, TransitionType.SLIDE_LEFT);
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

    protected <T, R> void sendHttpRequest(String uri, String method, T requestBody, Class<R> responseClass,
                                          Consumer<R> onSuccess,
                                          BiConsumer<Integer, String> onFailure,
                                          String authToken) {
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBodyJson = null;
        if (requestBody != null) {
            try {
                requestBodyJson = objectMapper.writeValueAsString(requestBody);
                System.out.println("HTTP Request Body: " + requestBodyJson);
            } catch (IOException e) {
                System.err.println("Error serializing request body: " + e.getMessage());
                e.printStackTrace();
                javafx.application.Platform.runLater(() ->
                        onFailure.accept(0, "Failed to prepare request data. Please try again.")
                );
                return;
            }
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Content-Type", "application/json");

        if (authToken != null && !authToken.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + authToken);
        }

        if (method.equalsIgnoreCase("POST")) {
            requestBuilder.POST(HttpRequest.BodyPublishers.ofString(requestBodyJson != null ? requestBodyJson : ""));
        } else if (method.equalsIgnoreCase("PUT")) {
            requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(requestBodyJson != null ? requestBodyJson : ""));
        } else if (method.equalsIgnoreCase("GET")) {
            requestBuilder.GET();
        } else if (method.equalsIgnoreCase("DELETE")) {
            requestBuilder.DELETE();
        } else {
            javafx.application.Platform.runLater(() ->
                    onFailure.accept(405, "Unsupported HTTP method: " + method)
            );
            return;
        }

        HttpRequest request = requestBuilder.build();

        new Thread(() -> {
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                javafx.application.Platform.runLater(() -> {
                    System.out.println("HTTP Response Status Code: " + response.statusCode());
                    System.out.println("HTTP Response Body: " + response.body());

                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        try {
                            R successResponse = objectMapper.readValue(response.body(), responseClass);
                            onSuccess.accept(successResponse);
                        } catch (IOException e) {
                            System.err.println("Error deserializing success response: " + e.getMessage());
                            e.printStackTrace();
                            onFailure.accept(response.statusCode(), "Failed to process server response.");
                        }
                    } else {
                        try {
                            @SuppressWarnings("unchecked")
                            java.util.Map<String, String> errorResponse = objectMapper.readValue(response.body(), java.util.Map.class);
                            String errorMessage = errorResponse.getOrDefault("error", "An unknown error occurred.");
                            onFailure.accept(response.statusCode(), errorMessage);
                        } catch (IOException e) {
                            System.err.println("Error parsing error response: " + e.getMessage());
                            e.printStackTrace();
                            onFailure.accept(response.statusCode(), "Server returned an unreadable error. Status: " + response.statusCode());
                        }
                    }
                });
            } catch (IOException | InterruptedException e) {
                System.err.println("Network or API call failed: " + e.getMessage());
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    onFailure.accept(-1, "Failed to connect to server. Please check your internet connection.");
                });
            }
        }).start();
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }
}