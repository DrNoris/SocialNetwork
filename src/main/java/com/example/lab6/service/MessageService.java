package com.example.lab6.service;

import com.example.lab6.domain.AbstractMessage;
import com.example.lab6.domain.Message;
import com.example.lab6.domain.PhotoMessage;
import com.example.lab6.domain.Utilizator;
import com.example.lab6.domain.paging.Pageable;
import com.example.lab6.repository.database.MessageDatabaseRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MessageService {

    private final MessageDatabaseRepository messageRepo;

    public MessageService(MessageDatabaseRepository messageRepository) {
        this.messageRepo = messageRepository;
    }

    public Optional<Message> sendMessage(Utilizator sender, List<Utilizator> receivers, String messageText, Long replyTo) {
        Message message = new Message(sender, receivers, messageText, LocalDateTime.now());
        message.setReplyTo(replyTo);

        return messageRepo.save(message);
    }

    public Optional<PhotoMessage> sendPhoto(Utilizator sender, List<Utilizator> receivers, String absolutePath, Long replyTo) throws IOException {
        Path path = Paths.get(absolutePath);

        PhotoMessage message = new PhotoMessage(sender, receivers, Files.readAllBytes(path), LocalDateTime.now());
        return messageRepo.save(message);
    }

    // Get a single message by ID
    public Optional<Message> getMessageById(Long id) {
        return messageRepo.findOne(id);
    }

    // Get all messages
    public Iterable<Message> getAllMessages() {
        return messageRepo.findAll();
    }

    // Update an existing message's content
    public Optional<Message> updateMessage(Long messageId, String newText) {
        Optional<Message> messageOpt = messageRepo.findOne(messageId);

        if (messageOpt.isPresent()) {
            Message message = messageOpt.get();
            message.setMessage(newText);  // Update message content
            return messageRepo.update(message);
        }
        return Optional.empty();
    }

    // Delete a message by ID (soft delete - mark as 'Deleted Message')
    public Optional<Message> deleteMessage(Long messageId) {
        return messageRepo.delete(messageId);
    }

    // Get all messages sent by a specific sender
    public Iterable<Message> getMessagesBySender(Utilizator sender) {
        // Here you can filter messages by sender, for now, this fetches all messages
        return messageRepo.findAll(); // This would be improved with filtering based on sender if needed
    }

    // Get all messages for a specific receiver
    public Iterable<Message> getMessagesForReceiver(Utilizator receiver) {
        // Implement filtering based on receiver. This could involve querying the `message_receivers` table.
        return messageRepo.findAll(); // This would be improved with filtering based on receiver if needed
    }

    public Optional<List<AbstractMessage>> getMessagesBetweenUsers(Utilizator currentUser, Utilizator chatUser) {
        return messageRepo.findMessagesBetweenUsers(currentUser.getId(), chatUser.getId());
    }

    public Optional<List<AbstractMessage>> getMessagesBetweenUsers(Utilizator currentUser, Utilizator chatUser, Pageable pageable) {
        return messageRepo.findMessagesBetweenUsers(currentUser.getId(), chatUser.getId(), pageable);
    }
}
