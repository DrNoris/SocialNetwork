package com.example.lab6.controllers;

import com.example.lab6.domain.FriendRequest;
import com.example.lab6.domain.Utilizator;
import com.example.lab6.domain.observer.Observer;
import com.example.lab6.domain.paging.Pageable;
import com.example.lab6.service.AppService;
import com.example.lab6.service.MessageService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class AppController implements Observer {
    private final AppService service;
    private final Utilizator currentUser;
    private final MessageService messageService;
    private int currentPage = 0;
    private int pageSize = 5;
    private int maxPage = 0;

    public AppController(AppService service, MessageService messageService, Utilizator currentUser){
        this.service = service;
        this.currentUser = currentUser;
        this.messageService = messageService;
        service.addObserver(this);
    }

    @FXML
    public VBox friendsVBox;

    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;

    //Friends table
    @FXML
    private TableView<Utilizator> friendsTable;
    @FXML
    private TableColumn<Utilizator, String> nameColumn;
    @FXML
    private TableColumn<Utilizator, String> friendsSinceColumn;
    @FXML
    private TableColumn<Utilizator, Void> actionColumn;

    @FXML
    private TableColumn<?, ?> statusColumn;
    @FXML
    private VBox notificationContainer;
    @FXML
    public VBox activityContainer;
    @FXML
    private VBox userListVBox;


    @Override
    public void update() {
        Platform.runLater(() -> {
            loadFriends();
            loadNotifications();
            loadActivity();
        });
    }

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        friendsTable.setPlaceholder(new Label("No friends to display."));

        configureActionColumn();
        loadFriends();
        loadNotifications();
        loadActivity();
    }


    private void configureActionColumn() {
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");
            private final Button chatButton = new Button("Chat");
            private final HBox buttonContainer = new HBox(deleteButton, chatButton);

            {
                buttonContainer.setSpacing(10); // Space between buttons

                deleteButton.setOnAction(event -> {
                    Utilizator user = (Utilizator) getTableView().getItems().get(getIndex());
                    handleDeleteFriend(user);
                });

                chatButton.setOnAction(event -> {
                    Utilizator user = (Utilizator) getTableView().getItems().get(getIndex());
                    handleChat(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonContainer);
                }
            }
        });

        Optional<List<Utilizator>> optionalFriends = service.getAllFriendships(currentUser);
        int friendsCount = optionalFriends.map(List::size).orElse(0);


        if (friendsCount > pageSize){
            HBox paginationButtons = new HBox();

            Button previousPageButton = new Button("<-");
            Button nextPageButton = new Button("->");

            previousPageButton.setOnAction(event -> previousPage(previousPageButton, nextPageButton));
            nextPageButton.setOnAction(event -> nextPage(previousPageButton, nextPageButton));

            previousPageButton.setDisable(true);

            paginationButtons.getChildren().add(previousPageButton);
            paginationButtons.getChildren().add(nextPageButton);

            friendsVBox.getChildren().add(paginationButtons);

            maxPage = friendsCount / pageSize;
        }

        if (friendsCount == 0) {
            Label noFriendsLabel = new Label("You have no friends yet.");
            friendsVBox.getChildren().add(noFriendsLabel);
            System.out.println("Nu există prieteni de încărcat!");
        } else {
            if (friendsCount > 1) {
                    Button sendGroup = new Button("Send to Friends");
                    sendGroup.setOnAction(event -> handleFriendsChat());

                    friendsVBox.getChildren().add(sendGroup);
            }
        }
    }


    private void loadFriends() {
        Optional<List<Utilizator>> friends = service.getAllFriendships(currentUser, new Pageable(currentPage, pageSize));

        friendsTable.getItems().clear();

        if (friends.isPresent() && !friends.get().isEmpty()) {
            friendsSinceColumn.setCellValueFactory(param -> {
                Utilizator user = param.getValue();
                LocalDateTime friendshipDate = service.getFriendshipDate(user.getId(), currentUser.getId());
                if (friendshipDate != null) {
                    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    return new SimpleStringProperty(friendshipDate.format(dateFormat));
                }
                return new SimpleStringProperty("Unknown Date");
            });

            friendsTable.setItems(FXCollections.observableArrayList(friends.get()));
        } else {
            friendsTable.setPlaceholder(new Label("No friends to display."));
        }
    }


    private void loadNotifications() {
        Optional<List<Utilizator>> requests = service.getAllReceivedRequests(currentUser);

        notificationContainer.getChildren().clear();

        if (requests.isEmpty()) {
            System.out.println("Nu există cereri noi!");
            Label nothing = new Label("Nothing new!");
            notificationContainer.getChildren().add(nothing);
        } else {
            Label title = new Label("Received friend requests");
            notificationContainer.getChildren().add(title);
            requests.get().forEach(user -> {
                VBox row = new VBox();
                HBox data = new HBox();
                data.setSpacing(10);
                row.setStyle("-fx-background-color: #caff00; -fx-padding: 5;");

                Label userLabel = new Label(user.getUsername());

                LocalDateTime requestDate = service.getReceiveRequestsDate(user.getId(), currentUser.getId());

                String formattedDate = "Unknown Date";
                if (requestDate != null) {
                    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    formattedDate = requestDate.format(dateFormat);
                }

                Label dateLabel = new Label("From: " + formattedDate);
                dateLabel.setFont(new Font(8));

                Button addUser = new Button("Accept");
                addUser.setOnAction(event -> handleAccept(user));

                Button deleteRequest = new Button("Decline");
                deleteRequest.setOnAction(event -> handleDecline(user));

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                data.getChildren().addAll(userLabel, spacer, addUser, deleteRequest);
                row.getChildren().addAll(data, dateLabel);

                notificationContainer.getChildren().add(row);
            });
        }
    }


    private void loadActivity() {
        List<FriendRequest> userRequests = service.getTopRequestsByUser(currentUser.getId(), 5);

        activityContainer.getChildren().clear();

        if (userRequests.isEmpty()) {
            activityContainer.getChildren().add(new Label("No recent activity found."));
            return;
        }

        for (FriendRequest request : userRequests) {
            HBox activityRow = new HBox();
            activityRow.setSpacing(10);
            activityRow.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 5; -fx-border-color: #cccccc;");

            Label timeLabel = new Label(formatTime(request.getTimestamp()));
            Label sentTo = new Label(service.findUserById(request.getReceiverId()).get().getUsername());
            Label statusLabel = new Label(request.getStatus());
            statusLabel.setStyle(request.getStatus());

            activityRow.getChildren().addAll(timeLabel, sentTo, statusLabel);

            activityContainer.getChildren().add(activityRow);
        }
    }

    private String formatTime(LocalDateTime requestTime) {
        LocalDate today = LocalDate.now();
        if (requestTime.toLocalDate().isEqual(today)) {
            return requestTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        } else {
            return requestTime.toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
    }


    private void handleDecline(Utilizator user) {
        service.declinePrietenie(user.getId(), currentUser.getId());
    }


    private void handleAccept(Utilizator user) {
        service.acceptPrietenie(currentUser.getId(), user.getId());
    }

    private void handleFriendsChat() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/lab6/chat-multiple-view.fxml"));

            loader.setControllerFactory(param -> new ChatMultipleController(currentUser, service.getAllFriendships(currentUser), messageService));

            Parent root = loader.load();

            Stage chatStage = new Stage();
            chatStage.setTitle("Multiple messages");

            Scene scene = new Scene(root, 600, 400);
            chatStage.setScene(scene);
            chatStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleSearch() {
        String username = searchField.getText();

        if (username.isEmpty())
            userListVBox.getChildren().clear();
        else {
            userListVBox.getChildren().clear();

            Optional<List<Utilizator>> users = service.findAllName(username);

            if (users.isEmpty()) {
                userListVBox.getChildren().add(new Label("No users found."));
            } else {
                users.get().forEach(user -> {
                    HBox row = new HBox();
                    row.setSpacing(10); // Add some spacing between elements
                    row.setStyle("-fx-background-color: #6a6a6a; -fx-padding: 5;");


                    Label userLabel = new Label(user.getUsername());
                    Button addUser = new Button("Add");

                    addUser.setOnAction(event -> handleAddFriend(user));

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    row.getChildren().addAll(userLabel, spacer, addUser);

                    userListVBox.getChildren().add(row);
                });
            }
        }
    }

    private void handleDeleteFriend(Utilizator user) {
        try {
            service.deletePrietenie(user.getId(), currentUser.getId());
        } catch (IllegalArgumentException e){
            System.out.println(e.getMessage());
        }
    }

    private void handleChat(Utilizator user) {
        System.out.println("Opening chat with " + user.getUsername());

        // Load the chat window FXML
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/lab6/chat-view.fxml"));

            // Inject dependencies into the controller
            loader.setControllerFactory(param -> new ChatController(currentUser, user, messageService));

            // Load the FXML
            Parent root = loader.load();

            // Create a new Stage (chat window)
            Stage chatStage = new Stage();
            chatStage.setTitle("Chat with " + user.getUsername());

            // Set the scene and show the stage
            Scene scene = new Scene(root, 400, 400);
            chatStage.setScene(scene);
            chatStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void handleAddFriend(Utilizator user){
        try {
            service.sendRequest(currentUser.getId(), user.getId());
        } catch (IllegalStateException e) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                        null,
                        e.getMessage(),
                        "Error sending the request",
                        JOptionPane.ERROR_MESSAGE
                );
            });
        }
    }

    private void previousPage(Button previousPageButton, Button nextPageButton) {
        if(currentPage > 0) {
            currentPage = currentPage - 1;
            nextPageButton.setDisable(false);
        }

        if (currentPage == 0)
            previousPageButton.setDisable(true);

        loadFriends();
    }

    private void nextPage(Button previousPageButton, Button nextPageButton) {
        if(currentPage < maxPage) {
            currentPage = currentPage + 1;
            previousPageButton.setDisable(false);
        }

        if (currentPage == maxPage)
            nextPageButton.setDisable(true);

        loadFriends();
    }

    @FXML
    public void handleViewNotifications() {

    }

}
