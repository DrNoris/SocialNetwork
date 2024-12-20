package com.example.lab6.controllers;

import com.example.lab6.domain.Message;
import com.example.lab6.domain.Utilizator;
import com.example.lab6.service.MessageService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import jdk.jshell.execution.Util;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChatMultipleController {
    @FXML
    public TextField searchField;
    @FXML
    public Button searchButton;
    @FXML
    public VBox searchResults;
    @FXML
    public FlowPane selectedUsers;
    @FXML
    public TextField messageField;
    @FXML
    public Button sendButton;

    private Utilizator currentUser;
    private List<Utilizator> allFriends;
    private List<Utilizator> allReceivers;
    private final MessageService messageService;

    public ChatMultipleController(Utilizator currentUser, Optional<List<Utilizator>> allFriendships, MessageService messageService) {
        this.currentUser = currentUser;
        this.allFriends = allFriendships.orElseGet(ArrayList::new);
        this.messageService = messageService;
        this.allReceivers = new ArrayList<>(); // Initialize as an empty list
    }


    public void sendMessage() {
        String messageText = messageField.getText();;
        messageField.clear();

        messageService.sendMessage(currentUser, allReceivers, messageText, null);
    }

    private void loadSelectedUsers() {
        // Clear the FlowPane before adding updated content
        selectedUsers.getChildren().clear();

        for (Utilizator user : allReceivers) {
            // Create a horizontal box for each user (HBox)
            HBox userBox = new HBox();
            userBox.setSpacing(5); // Add some spacing between elements
            userBox.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 5; -fx-border-radius: 5; -fx-border-color: #cccccc;");

            // Create a label with the username
            Label usernameLabel = new Label(user.getUsername());
            usernameLabel.setStyle("-fx-font-size: 14px;");

            // Create a button to remove the user
            Button removeButton = new Button("X");
            removeButton.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-background-color: transparent;");
            removeButton.setOnAction(event -> {
                // Remove the user from allReceivers and reload the selected users
                allReceivers.remove(user);
                loadSelectedUsers(); // Refresh the UI
            });

            // Add the label and button to the HBox
            userBox.getChildren().addAll(usernameLabel, removeButton);

            // Add the HBox to the FlowPane
            selectedUsers.getChildren().add(userBox);
        }
    }

    public void handleSearch() {
        String username = searchField.getText();

        if (username.isEmpty())
            searchResults.getChildren().clear();
        else {
            searchResults.getChildren().clear();

            List<Utilizator> users = allFriends.stream().filter(user ->
                user.getUsername().toLowerCase().startsWith(username.toLowerCase())).toList();

            if (users.isEmpty()) {
                searchResults.getChildren().add(new Label("No friends found."));
            } else {
                users.forEach(user -> {
                    HBox row = new HBox();
                    row.setSpacing(10); // Add some spacing between elements
                    row.setStyle("-fx-background-color: #6a6a6a; -fx-padding: 5;");


                    Label userLabel = new Label(user.getUsername());
                    Button addUser = new Button("Add");

                    addUser.setOnAction(event -> handleAddFriendToReceivers(user));

                    // Add a spacer to push the Button to the far right
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    // Add elements to the row
                    row.getChildren().addAll(userLabel, spacer, addUser);

                    // Add the row to the VBox
                    searchResults.getChildren().add(row);
                });
            }
        }
    }

    private void handleAddFriendToReceivers(Utilizator user) {
        if (allReceivers.stream().noneMatch(receiver -> receiver.getId().equals(user.getId()))) {
            allReceivers.add(user);
        }
        loadSelectedUsers();
    }
}
