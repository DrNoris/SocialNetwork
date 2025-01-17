package com.example.lab6.controllers;

import com.example.lab6.domain.Utilizator;
import com.example.lab6.service.Service;
import com.example.lab6.service.SignInService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class SignInController {

    private final SignInService signInService;

    @FXML
    private ImageView socialMediaIcon;

    @FXML
    public TextField firstNameField;

    @FXML
    public TextField lastNameField;

    @FXML
    public TextField usernameField;

    @FXML
    public PasswordField passwordField;

    @FXML
    private Button signIn;

    @FXML
    public void initialize() {
        socialMediaIcon.setImage(new Image(Objects.requireNonNull(getClass().getResource("/com/example/lab6/chillguy.png")).toExternalForm()));
    }

    public SignInController(SignInService service) {
        this.signInService = service;
    }

    public void handleSignIn() {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            signInService.signIn(firstName, lastName, username, password);
//            if (optionalUser.isEmpty()) {
//                System.out.println("User added.");
//                returnToLogin();
//            } else {
//                System.out.println("User could not be added. An existing user was found: " + optionalUser.get().getFirstName() + " " + optionalUser.get().getLastName());
//            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void returnToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/lab6/login-view.fxml"));

            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) signIn.getScene().getWindow();
            stage.setScene(scene);
            stage.show();  // Show the new scene

        } catch (IOException e) {
            e.printStackTrace();  // Handle any errors loading the scene
        }
    }
}
