package com.example.lab6.controllers;

import com.example.lab6.domain.AbstractMessage;
import com.example.lab6.domain.Message;
import com.example.lab6.domain.PhotoMessage;
import com.example.lab6.domain.Utilizator;
import com.example.lab6.domain.paging.Pageable;
import com.example.lab6.service.MessageService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ChatController {
    @FXML
    public ScrollPane scrollPane;
    @FXML
    public Button openFinderButton;
    @FXML
    public Button takePhoto;
    @FXML
    private VBox chatArea;
    @FXML
    private TextField messageField;
    @FXML
    private Button sendButton;

    private Utilizator currentUser;
    private Utilizator chatUser;
    private MessageService messageService;
    private Long replyToMessageId;

    private Stage stage; // Declare Stage variable

    private int currentPage = 0;
    private final int pageSize = 20;
    private boolean isLoading = false;

    public ChatController(Utilizator currentUser, Utilizator chatUser, MessageService messageService) {
        this.currentUser = currentUser;
        this.chatUser = chatUser;
        this.messageService = messageService;
        this.replyToMessageId = null;
    }

    @FXML
    public void initialize() {
        loadChatHistory();
        messageField.setPromptText("Type your message...");

        // Set up a listener for the send button click
        sendButton.setOnAction(event -> handleSendMessage());

        // Ensure that clicking on any message to reply sets the reply context correctly
        chatArea.setOnMouseClicked(event -> {
            // Reset reply if click is outside any message
            if (event.getTarget() instanceof VBox) {
                replyToMessageId = null;
            }
        });

//        scrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
//            if (newVal.doubleValue() == 0.0) { // Top of the chat
//                loadChatHistory();
//            }
//        });
    }

    private void loadChatHistory() {
        Optional<List<AbstractMessage>> messages = messageService.getMessagesBetweenUsers(currentUser, chatUser);

        chatArea.getChildren().clear();

        if (messages.isPresent()) {
            for (AbstractMessage abstractMessage : messages.get()) {
                String formattedDate = "";
                Node messageNode = null;
                String newText= "";
                if (abstractMessage instanceof Message) {
                    Message message = (Message) abstractMessage;
                    LocalDateTime timestamp = message.getDate();

                    if (timestamp.toLocalDate().isEqual(LocalDate.now())) {
                        formattedDate = timestamp.format(DateTimeFormatter.ofPattern("HH:mm"));
                    } else {
                        formattedDate = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    }

                    messageNode = (Node) new Label("[" + formattedDate + "]" + message.getSender().getUsername() + ": " + message.getMessage());
                    ((Label) messageNode).setWrapText(true);

                    if (message.getReplyTo() != 0) {
                        String replyToMessageText = messageService.getMessageById(message.getReplyTo()).get().getMessage();
                        replyToMessageText = replyToMessageText.length() > 15 ? replyToMessageText.substring(0, 15) : replyToMessageText;

                        String sender = messageService.getMessageById(message.getReplyTo()).get().getSender().getUsername();
                        newText = "Reply to " + sender + "'s message: " + replyToMessageText + "...\n\n";
                        newText = newText + message.getMessage();
                        ((Label) messageNode).setText(newText);
                    }

                    messageNode.setOnMouseClicked(event -> handleReply(message));
                }
                else if (abstractMessage instanceof PhotoMessage){
                    PhotoMessage photoMessage = (PhotoMessage) abstractMessage;
                    LocalDateTime timestamp = photoMessage.getDate();

                    if (timestamp.toLocalDate().isEqual(LocalDate.now())) {
                        formattedDate = timestamp.format(DateTimeFormatter.ofPattern("HH:mm"));
                    } else {
                        formattedDate = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    }

                    ImageView imageView = new ImageView();
                    byte[] imageBytes = photoMessage.getPhoto(); // Assume you have the image bytes
                    if (imageBytes != null) {
                        Image image = new Image(new ByteArrayInputStream(imageBytes));
                        imageView.setImage(image);
                        imageView.setFitWidth(200);  // Set an appropriate width for the image
                        imageView.setPreserveRatio(true);  // Maintain aspect ratio
                    }

//                    if (photoMessage.getReplyTo() != 0) {
//                        String replyToMessageText = messageService.getMessageById(photoMessage.getReplyTo()).get().getMessage();
//                        replyToMessageText = replyToMessageText.length() > 15 ? replyToMessageText.substring(0, 15) : replyToMessageText;
//
//                        String sender = messageService.getMessageById(photoMessage.getReplyTo()).get().getSender().getUsername();
//                        newText = "Reply to " + sender + "'s message: " + replyToMessageText + "...\n\n";
//                    }

                    messageNode = (Node) imageView;
                    imageView.setOnMouseClicked(event -> handleReply(photoMessage));
                }

                if (messageNode != null) {
                    if (messageNode instanceof Label) {
                        Label label = (Label) messageNode;
                        // Set the background color based on whether the current user is the sender
                        if (abstractMessage.getSender().getId().equals(currentUser.getId())) {
                            label.setStyle("-fx-background-color: #c9fba1; -fx-alignment: center-right; -fx-background-radius: 15px; -fx-padding: 5px;");
                        } else {
                            label.setStyle("-fx-background-color: #afafaf; -fx-alignment: center-left; -fx-background-radius: 15px; -fx-padding: 5px;");
                        }
                    } else if (messageNode instanceof ImageView) {
                        VBox imageContainer = new VBox();
                        ImageView imageView = (ImageView) messageNode;

                        if (abstractMessage.getSender().getId().equals(currentUser.getId())) {
                            imageContainer.setStyle("-fx-background-color: #c9fba1; -fx-alignment: center-right; -fx-background-radius: 15px; -fx-padding: 5px;");
                        } else {
                            imageContainer.setStyle("-fx-background-color: #afafaf; -fx-alignment: center-left; -fx-background-radius: 15px; -fx-padding: 5px;");
                        }

                        if (!newText.isEmpty())
                            imageContainer.getChildren().add(new Label(newText));

                        imageContainer.getChildren().add(imageView);
                        messageNode = (Node) imageContainer;  // Now messageNode contains the VBox with the ImageView
                    }

                    // Add the message to the chat area (VBox)
                    chatArea.getChildren().add((javafx.scene.Node) messageNode);
                }
            }

            //currentPage++;
        } else {
            Label noMessages = new Label("No messages found.");
            chatArea.getChildren().add(noMessages);
        }

        //isLoading = false;

        Platform.runLater(() -> scrollPane.setVvalue(1.0)); // Ensure scrolling happens after layout update
    }

    private void handleReply(AbstractMessage message) {
        replyToMessageId = message.getId();
        System.out.println("Replying to message ID: " + replyToMessageId);
    }

    public void handleAttachmentButton() {
        FileChooser fileChooser = new FileChooser();

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        Stage stage = (Stage) openFinderButton.getScene().getWindow(); // Replace 'yourButton' with the button that triggers the method
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            sendPhoto(selectedFile.getAbsolutePath());
            loadChatHistory();
        } else {
            System.out.println("File selection was canceled.");
        }
    }

    @FXML
    private void handleSendMessage() {
        String messageText = messageField.getText();
        if (messageText.isEmpty()) {
            return;  // Don't send an empty message
        }

        // Send the message with the current reply context
        sendMessage(messageText);
        messageField.clear();
        replyToMessageId = null;  // Reset the reply after sending
        loadChatHistory();
    }

    public void handlePhotoButton(ActionEvent actionEvent) {
    }

    private void sendMessage(String messageText) {
        messageService.sendMessage(currentUser, List.of(chatUser), messageText, replyToMessageId);
    }

    private void sendPhoto(String path){
        try {
            messageService.sendPhoto(currentUser, List.of(chatUser), path, replyToMessageId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeChatWindow() {
        if (stage != null) {
            stage.close(); // Close the stage when the chat is closed
        }
    }
}
