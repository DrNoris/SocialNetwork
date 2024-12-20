package com.example.lab6.controllers;

import com.example.lab6.domain.Utilizator;
import com.example.lab6.service.Service;
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

    private final Service service;

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
        // Load the image
        socialMediaIcon.setImage(new Image(Objects.requireNonNull(getClass().getResource("/com/example/lab6/chillguy.png")).toExternalForm()));
    }

    // Setter method for Service
    public SignInController(Service service) {
        this.service = service;
    }

    public void handleSignIn() {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            Optional<Utilizator> optionalUser = service.addUtilizator(firstName, lastName, username, password);
            if (optionalUser.isEmpty()) {
                System.out.println("User added.");
                returnToLogin(); // Return to login screen after successful sign-up
            } else {
                System.out.println("User could not be added. An existing user was found: " + optionalUser.get().getFirstName() + " " + optionalUser.get().getLastName());
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void returnToLogin() {
        try {
            // Load the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/lab6/login-view.fxml"));

            Parent root = loader.load();

            // Get the current stage (window) and set the new scene
            Scene scene = new Scene(root);
            Stage stage = (Stage) signIn.getScene().getWindow();  // Use signIn button to get the stage
            stage.setScene(scene);
            stage.show();  // Show the new scene

        } catch (IOException e) {
            e.printStackTrace();  // Handle any errors loading the scene
        }
    }
}
