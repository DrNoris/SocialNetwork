package com.example.lab6.repository.database;

import com.example.lab6.domain.FriendRequest;
import com.example.lab6.domain.Utilizator;
import com.example.lab6.repository.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class RequestDatabaseRepository implements Repository<Long, FriendRequest> {
    private final String url;
    private final String user;
    private final String password;

    public RequestDatabaseRepository(String username, String password, String url) {
        this.url = url;
        this.user = username;
        this.password = password;
    }


    @Override
    public Optional<FriendRequest> findOne(Long id) {
        String sql = "SELECT * FROM friend_requests WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(mapResultSetToFriendRequest(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<FriendRequest> findOne(Long sender, Long receiver) {
        String sql = "SELECT * FROM friend_requests WHERE sender_id = ? AND receiver_id = ? AND status = 'PENDING'";
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, sender);
            statement.setLong(2, receiver);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(mapResultSetToFriendRequest(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Iterable<Long> findAllWith(Long id) {
        List<Long> requests = new ArrayList<>();
        String sql = "SELECT sender_id FROM friend_requests WHERE receiver_id = ? and status = 'PENDING'";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Long userId2 = rs.getLong("sender_id");
                    requests.add(userId2);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return requests;
    }

    public Iterable<FriendRequest> findAllActivity(Long id, int top) {
        List<FriendRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM friend_requests WHERE sender_id = ? ORDER BY timestamp DESC LIMIT ?";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, id);
            statement.setInt(2, top);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Long senderId = rs.getLong("sender_id");
                    Long receiverId = rs.getLong("receiver_id");
                    String status = rs.getString("status");
                    LocalDateTime timestamp = rs.getTimestamp("timestamp").toLocalDateTime();

                    FriendRequest request = new FriendRequest(senderId, receiverId, status);
                    request.setTimestamp(timestamp);

                    requests.add(request);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return requests;
    }


    @Override
    public Iterable<FriendRequest> findAll() {
        List<FriendRequest> friendRequests = new ArrayList<>();
        String sql = "SELECT * FROM friend_requests";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                friendRequests.add(mapResultSetToFriendRequest(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friendRequests;
    }

    @Override
    public Optional<FriendRequest> save(FriendRequest entity) {
        if (findOne(entity.getSenderId(), entity.getReceiverId()).isPresent())
            return Optional.of(entity);

        String sql = "INSERT INTO friend_requests (sender_id, receiver_id, status, timestamp) VALUES (?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, entity.getSenderId());
            ps.setLong(2, entity.getReceiverId());
            ps.setString(3, entity.getStatus());
            ps.setTimestamp(4, Timestamp.valueOf(entity.getTimestamp()));

            int rowsAffected = ps.executeUpdate(); // Execute the insert
            if (rowsAffected > 0) {
                // Retrieve the generated ID for the inserted user
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        // Set the generated ID to the entity
                        entity.setId(generatedKeys.getLong(1));
                    }
                }
                return Optional.empty();  // Return Optional.empty() to indicate success (no errors)
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.of(entity);  // Return the entity to indicate an error occurred

        }
        return Optional.empty();
    }

    @Override
    public Optional<FriendRequest> delete(Long id) {
        String sql = "DELETE FROM friend_requests WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, id);
            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                return Optional.empty(); // Ștergere reușită
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<FriendRequest> update(FriendRequest entity) {
        String sql = "UPDATE friend_requests SET status = ?, timestemp = ? WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, entity.getStatus());
            statement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            statement.setLong(3, entity.getId());

            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                return Optional.empty();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.of(entity);
    }

    private FriendRequest mapResultSetToFriendRequest(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        Long senderId = resultSet.getLong("sender_id");
        Long receiverId = resultSet.getLong("receiver_id");
        String status = resultSet.getString("status");
        LocalDateTime timestamp = resultSet.getTimestamp("timestamp").toLocalDateTime();

        FriendRequest friendRequest = new FriendRequest(senderId, receiverId, status);
        friendRequest.setId(id);
        friendRequest.setTimestamp(timestamp);
        return friendRequest;
    }

    public void modify(Long sender, Long receiver, char accepted) {
        // Translate the 'accepted' character into a meaningful status
        String status = accepted == 'Y' ? "ACCEPTED" : "REJECTED";

        String sql = "UPDATE friend_requests SET status = ? WHERE sender_id = ? AND receiver_id = ?";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // Set the parameters for the prepared statement
            statement.setString(1, status); // Set the status column
            statement.setLong(2, sender);  // Set the sender_id condition
            statement.setLong(3, receiver); // Set the receiver_id condition

            // Execute the update query
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Status updated successfully.");
            } else {
                System.out.println("No matching record found to update.");
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Log and handle the SQLException
        }
    }

}
