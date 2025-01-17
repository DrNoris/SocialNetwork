package com.example.lab6.controllers;

import com.example.lab6.domain.Utilizator;
import com.example.lab6.service.ProfileService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ProfileController {
    @FXML
    public Button profileButton;
    @FXML
    public ImageView profileImage;
    @FXML
    public Label usernameLabel;
    @FXML
    public Label realNameLabel;
    @FXML
    public Label friendsCountLabel;


    private Utilizator user;
    private ProfileService profileService;

    public ProfileController(ProfileService profileService, Utilizator currentUser) {
        user = currentUser;
        this.profileService = profileService;
    }

    @FXML
    public void initialize(){
        loadName();
        loadFriendsCount();
        loadProfilePicture();
    }

    public void loadProfilePicture(){
        byte[] profilePicture = user.getProfilePicture();
        if (profilePicture != null) {
            Image image = new Image(new ByteArrayInputStream(profilePicture));
            profileImage.setImage(image);
        }
        else {
            Image placeholderImage = new Image(getClass().getResourceAsStream("/images/anonymous-user.jpg"));
            profileImage.setImage(placeholderImage);
        }
    }

    public void loadName(){
        usernameLabel.setText(user.getUsername());
        realNameLabel.setText(user.getFirstName() + " " + user.getLastName());
    }

    public void loadFriendsCount(){
        friendsCountLabel.setText("Friends: " + user.getFrinds().size());
    }

    public void handleProfileButtonAction() {
        FileChooser fileChooser = new FileChooser();

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        Stage stage = (Stage) profileImage.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                byte[] imageBytes = Files.readAllBytes(Path.of(selectedFile.getAbsolutePath()));
                user.setProfilePicture(imageBytes);
                profileService.updateUser(user);
                loadProfilePicture();
            } catch (IOException e) {
                System.out.println("Error reading file: " + e.getMessage());
            }
        } else {
            System.out.println("File selection was canceled.");
        }
    }
}
