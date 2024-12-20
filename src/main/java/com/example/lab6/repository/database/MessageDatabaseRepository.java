package com.example.lab6.repository.database;

import com.example.lab6.domain.AbstractMessage;
import com.example.lab6.domain.Message;
import com.example.lab6.domain.PhotoMessage;
import com.example.lab6.domain.Utilizator;
import com.example.lab6.domain.paging.Pageable;
import com.example.lab6.repository.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class MessageDatabaseRepository implements Repository<Long, Message> {
    public String user;
    public String password;
    public String url;
    private UtilizatorDatabaseRepository userRepo;

    public MessageDatabaseRepository(String user, String password, String url, UtilizatorDatabaseRepository userRepo){
        this.user = user;
        this.url = url;
        this.password = password;
        this.userRepo = userRepo;
    }

    private AbstractMessage createMessageFromMessageSet(ResultSet set){
        try{
            Long id = set.getLong("id");
            Utilizator sender = userRepo.findOne(set.getLong("sender_id")).get();
            String text = set.getString("message");
            LocalDateTime date = set.getTimestamp("date").toLocalDateTime();
            Long reply_to = set.getLong("reply_to");
            byte[] photo = set.getBytes("photo");
            List<Utilizator> receivers = new ArrayList<>();

            String query = String.format("SELECT receiver_id FROM message_receivers WHERE message_id = '%d'", id);

            try (Connection connection = DriverManager.getConnection(url, user, password);
                 ResultSet resultSet = connection.createStatement().executeQuery(query)) {

                while (resultSet.next()) {
                    receivers.add(userRepo.findOne(resultSet.getLong("receiver_id")).get());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (text != null){
                Message message = new Message(sender, receivers, text, date);
                message.setId(id);
                message.setReplyTo(reply_to);
                return message;
            } else {
                PhotoMessage message = new PhotoMessage(sender, receivers, photo, date);
                message.setId(id);
                message.setReplyTo(reply_to);
                return message;
            }
        }
        catch (SQLException e){
            return null;
        }

    }


    @Override
    public Optional<Message> findOne(Long id) {
        Message message;
        try(Connection connection = DriverManager.getConnection(url, user, password);
            ResultSet resultSet = connection.createStatement().executeQuery(String.format("select * from messages where id = '%d'", id))){
            if (resultSet.next()){
                message = (Message) createMessageFromMessageSet(resultSet);
                return Optional.ofNullable(message);
            }
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Iterable<Message> findAll() {
        List<Message> messages = new ArrayList<>();

        try(Connection connection = DriverManager.getConnection(url, user, password);
            ResultSet resultSet = connection.createStatement()
                    .executeQuery("select * from messages")){
            while (resultSet.next()){
                messages.add((Message)createMessageFromMessageSet(resultSet));
            }

            return messages;
        }
        catch (SQLException e){
            e.printStackTrace();
        }

        return messages;
    }

    @Override
    public Optional<Message> save(Message entity) {
        String sql = "insert into messages (sender_id, message, date, reply_to) values (?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, entity.getSender().getId());
            ps.setString(2, entity.getMessage());
            ps.setTimestamp(3, Timestamp.valueOf(entity.getDate()));

            if (entity.getReplyTo() != null) {
                ps.setLong(4, entity.getReplyTo());
            } else {
                ps.setNull(4, java.sql.Types.BIGINT);
            }

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        // Set the generated ID to the entity
                        entity.setId(generatedKeys.getLong(1));
                    }
                }

                // Insert data into message_receivers for each receiver
                String sqlReceivers = "INSERT INTO message_receivers (message_id, receiver_id) VALUES (?, ?)";
                try (PreparedStatement psReceivers = connection.prepareStatement(sqlReceivers)) {
                    for (Utilizator receiver : entity.getReceivers()) {
                        psReceivers.setLong(1, entity.getId());
                        psReceivers.setLong(2, receiver.getId());

                        // Execute the insert for each receiver
                        psReceivers.addBatch(); // Add to batch
                    }

                    // Execute the batch insert
                    psReceivers.executeBatch();
                }


                return Optional.empty();  // Return Optional.empty() to indicate success (no errors)
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Log or handle the exception as necessary
            return Optional.ofNullable(entity);  // Return the entity to indicate an error occurred
        }
        return Optional.empty();
    }

    public Optional<PhotoMessage> save(PhotoMessage entity) {
        String sql = "insert into messages (sender_id, photo, date, reply_to) values (?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, entity.getSender().getId());
            ps.setBytes(2, entity.getPhoto());
            ps.setTimestamp(3, Timestamp.valueOf(entity.getDate()));

            if (entity.getReplyTo() != null) {
                ps.setLong(4, entity.getReplyTo());
            } else {
                ps.setNull(4, java.sql.Types.BIGINT);
            }

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        // Set the generated ID to the entity
                        entity.setId(generatedKeys.getLong(1));
                    }
                }

                // Insert data into message_receivers for each receiver
                String sqlReceivers = "INSERT INTO message_receivers (message_id, receiver_id) VALUES (?, ?)";
                try (PreparedStatement psReceivers = connection.prepareStatement(sqlReceivers)) {
                    for (Utilizator receiver : entity.getReceivers()) {
                        psReceivers.setLong(1, entity.getId());
                        psReceivers.setLong(2, receiver.getId());

                        // Execute the insert for each receiver
                        psReceivers.addBatch(); // Add to batch
                    }

                    // Execute the batch insert
                    psReceivers.executeBatch();
                }


                return Optional.empty();  // Return Optional.empty() to indicate success (no errors)
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Log or handle the exception as necessary
            return Optional.ofNullable(entity);  // Return the entity to indicate an error occurred
        }
        return Optional.empty();
    }



    @Override
    public Optional<Message> delete(Long id) {
        String sql = "UPDATE messages SET message = 'Deleted Message' when id = ?";

        try(Connection connection = DriverManager.getConnection(url, user, password);
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return Optional.empty(); // Return empty Optional to indicate success
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<Message> update(Message entity) {
        // SQL query to update the message content based on the message ID
        String sql = "UPDATE messages SET message = ? WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            // Set the new message text and the ID of the message to be updated
            ps.setString(1, entity.getMessage());
            ps.setLong(2, entity.getId());

            // Execute the update query
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                // If the message was updated successfully, return the updated message
                return Optional.of(entity); // Return the updated entity
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // If no rows were affected or an error occurred, return Optional.empty() to indicate failure
        return Optional.empty();
    }

    public Optional<List<AbstractMessage>> findMessagesBetweenUsers(Long currentUser, Long chatUser) {
        List<AbstractMessage> messages = new ArrayList<>();
        String sql = "SELECT * " +
                "FROM messages m " +
                "JOIN message_receivers mr ON m.id = mr.message_id " +
                "WHERE (m.sender_id = ? AND mr.receiver_id = ?) " +
                "OR (m.sender_id = ? AND mr.receiver_id = ?) " +
                "ORDER BY m.date";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            // Set the user IDs as parameters
            ps.setLong(1, currentUser);
            ps.setLong(2, chatUser);
            ps.setLong(3, chatUser);
            ps.setLong(4, currentUser);

            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    // Create a message from the result set
                    messages.add(createMessageFromMessageSet(resultSet));
                }
            }
            return Optional.of(messages);
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Optional<List<AbstractMessage>> findMessagesBetweenUsers(Long currentUser, Long chatUser, Pageable pageable) {
        List<AbstractMessage> messages = new ArrayList<>();
        String sql = "SELECT * " +
                "FROM messages m " +
                "JOIN message_receivers mr ON m.id = mr.message_id " +
                "WHERE (m.sender_id = ? AND mr.receiver_id = ?) " +
                "OR (m.sender_id = ? AND mr.receiver_id = ?) " +
                "ORDER BY m.date " +
                "LIMIT ? OFFSET ?";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            // Set the user IDs as parameters
            ps.setLong(1, currentUser);
            ps.setLong(2, chatUser);
            ps.setLong(3, chatUser);
            ps.setLong(4, currentUser);
            ps.setInt(5, pageable.getPageSize());
            ps.setInt(6, pageable.getPageSize() * pageable.getPageNumber());

            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    messages.add(createMessageFromMessageSet(resultSet));
                }
            }
            return Optional.of(messages);
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
