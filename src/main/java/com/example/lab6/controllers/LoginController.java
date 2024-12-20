package com.example.lab6.controllers;

import com.example.lab6.domain.Utilizator;
import com.example.lab6.service.AppService;
import com.example.lab6.service.LoginService;
import com.example.lab6.service.MessageService;
import com.example.lab6.service.Service;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class LoginController {

    private final LoginService loginService;
    private final AppService appService;
    private final Service service;
    private final MessageService messageService;

    @FXML
    private ImageView socialMediaIcon;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button siginButton;

    public LoginController(LoginService loginService, AppService appService, Service service, MessageService messageService){
        this.loginService = loginService;
        this.service = service;
        this.appService = appService;
        this.messageService = messageService;
    }

    private void showError(String message) {
        // Show an error message to the user (you can use an Alert for a popup)
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void initialize() {
        // Load the image
        socialMediaIcon.setImage(new Image(Objects.requireNonNull(getClass().getResource("/com/example/lab6/chillguy.png")).toExternalForm()));
    }


    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        Optional<Utilizator> user;

        try {
            user = loginService.login(username, password);  // Assuming loginService is correct
        } catch (IllegalArgumentException e) {
            e.printStackTrace();  // Log the error (can be replaced with a user-friendly message)
            showError("Login failed: " + e.getMessage()); // Show a message to the user (optional)
            return; // Exit the method early if there's an exception
        }

        if (user.isPresent()) {
            try {
                // Load the new scene (app.view)
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/lab6/app-view.fxml"));
                loader.setControllerFactory(controllerClass -> {
                    if (controllerClass == AppController.class) {
                        return new AppController(appService, messageService, user.get());  // Inject Service here
                    }
                    try {
                        return controllerClass.getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

                Parent root = loader.load();

                // Get the current stage (window) and set the new scene
                Scene scene = new Scene(root);
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(scene);
                stage.show();  // Show the new scene

            } catch (IOException e) {
                e.printStackTrace();  // Handle any errors loading the scene
                showError("Failed to load the new scene.");
            }
        } else {
            showError("Invalid username or password.");
        }
    }



    @FXML
    private void handleSignIn() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/lab6/signin-view.fxml"));
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == SignInController.class) {
                    return new SignInController(service);  // Inject Service here
                }
                try {
                    return controllerClass.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Parent root = loader.load();

            // Get the current stage (window) and set the new scene
            Scene scene = new Scene(root);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();  // Show the new scene
        } catch (IOException e) {
            e.printStackTrace();  // Handle any errors loading the scene
            showError("Failed to load the new scene.");
        }
    }

    public static class ChatMultipleView {
    }
}
