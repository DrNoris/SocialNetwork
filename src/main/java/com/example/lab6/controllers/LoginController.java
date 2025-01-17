package com.example.lab6.controllers;

import com.example.lab6.domain.Utilizator;
import com.example.lab6.service.*;
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
    private final MessageService messageService;
    private final ProfileService profileService;
    private final SignInService signInService;

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

    public LoginController(LoginService loginService, AppService appService, MessageService messageService, ProfileService profileService, SignInService signInService){
        this.loginService = loginService;
        this.appService = appService;
        this.messageService = messageService;
        this.profileService = profileService;
        this.signInService = signInService;
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
        socialMediaIcon.setImage(new Image(Objects.requireNonNull(getClass().getResource("/com/example/lab6/chillguy.png")).toExternalForm()));
    }


    @FXML
    private void handleLogin() {
        try {
            String username = usernameField.getText();
            String password = passwordField.getText();

            byte[] passwordBytes = password.getBytes("UTF-8");

            Optional<Utilizator> user = loginService.login(username, passwordBytes);

            if (user.isPresent()) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/lab6/app-view.fxml"));
                    loader.setControllerFactory(controllerClass -> {
                        if (controllerClass == AppController.class) {
                            return new AppController(appService, messageService, profileService, user.get());
                        }
                        try {
                            return controllerClass.getDeclaredConstructor().newInstance();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });

                    Parent root = loader.load();

                    Scene scene = new Scene(root);
                    Stage stage = (Stage) usernameField.getScene().getWindow();
                    stage.setScene(scene);
                    stage.show(); // Show the new scene
                } catch (IOException e) {
                    e.printStackTrace();
                    showError("Failed to load the new scene.");
                }
            } else {
                showError("Invalid username or password.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("An error occurred while processing the login.");
        }
    }




    @FXML
    private void handleSignIn() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/lab6/signin-view.fxml"));
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == SignInController.class) {
                    return new SignInController(signInService);
                }
                try {
                    return controllerClass.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();  // Show the new scene
        } catch (IOException e) {
            e.printStackTrace();  // Handle any errors loading the scene
            showError("Failed to load the new scene.");
        }
    }
}
