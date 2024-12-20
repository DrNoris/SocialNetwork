package com.example.lab6.repository.database;

import com.example.lab6.domain.Prietenie;
import com.example.lab6.domain.Tuple;
import com.example.lab6.domain.Utilizator;
import com.example.lab6.domain.paging.Page;
import com.example.lab6.domain.paging.Pageable;
import com.example.lab6.repository.PagingRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PrietenieDatabaseRepository implements PagingRepository<Tuple<Long, Long>, Prietenie<Long>> {
    private final String url;
    private final String username;
    private final String password;

    public PrietenieDatabaseRepository(String username, String password, String url) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }


    @Override
    public Optional<Prietenie<Long>> findOne(Tuple<Long, Long> id) {
        String sql = "SELECT * FROM friendships WHERE user_id1 = ? AND user_id2 = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id.getLeft());
            stmt.setLong(2, id.getRight());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Prietenie prietenie = mapResultSetToPrietenie(rs);
                return Optional.of(prietenie);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Iterable<Prietenie<Long>> findAll() {
        List<Prietenie<Long>> friendships = new ArrayList<>();
        String sql = "SELECT * FROM friendships";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Prietenie<Long> prietenie = mapResultSetToPrietenie(rs);
                friendships.add(prietenie);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friendships;
    }

    @Override
    public Optional<Prietenie<Long>> save(Prietenie<Long> entity) {
        String sql = "INSERT INTO friendships (user_id1, user_id2, friends_from) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, entity.getId().getLeft());
            stmt.setLong(2, entity.getId().getRight());
            stmt.setTimestamp(3, Timestamp.valueOf(entity.getFriendsFrom()));
            stmt.executeUpdate();
            return Optional.empty(); // No existing entity to return in this case
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.of(entity);
    }

    @Override
    public Optional<Prietenie<Long>> delete(Tuple<Long, Long> id) {
        Optional<Prietenie<Long>> existingFriendship = findOne(id);
        if (existingFriendship.isPresent()) {
            String sql = "DELETE FROM friendships WHERE user_id1 = ? AND user_id2 = ?";
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id.getLeft());
                stmt.setLong(2, id.getRight());
                stmt.executeUpdate();
                return existingFriendship;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Prietenie<Long>> update(Prietenie<Long> entity) {
        String sql = "UPDATE friendships SET friends_from = ? WHERE user_id1 = ? AND user_id2 = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(entity.getFriendsFrom()));
            stmt.setLong(2, entity.getId().getLeft());
            stmt.setLong(3, entity.getId().getRight());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return Optional.empty(); // Return empty if update was successful
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.of(entity);
    }

    private Prietenie<Long> mapResultSetToPrietenie(ResultSet rs) throws SQLException {
        Long userId1 = rs.getLong("user_id1");
        Long userId2 = rs.getLong("user_id2");
        LocalDateTime date = rs.getTimestamp("friends_from").toLocalDateTime();
        Prietenie<Long> prietenie = new Prietenie<>();
        prietenie.setId(new Tuple<>(userId1, userId2));
        prietenie.setFriendsFrom(date);
        return prietenie;
    }

    private Utilizator mapResultSetToUser(ResultSet set){
        try{
            String firstName = set.getString("first_name");
            String lastName = set.getString("last_name");
            Long id = set.getLong("id");
            String password = set.getString("password");
            String username = set.getString("user_name");

            Utilizator utilizator = new Utilizator(firstName, lastName, username, password);
            utilizator.setId(id);
            return utilizator;
        }
        catch (SQLException e){
            return null;
        }
    }

    public Iterable<Utilizator> findAllWith(Long id) {
        List<Utilizator> friends = new ArrayList<>();
        String sql = "SELECT users.id, users.first_name, users.last_name, users.user_name, users.password " +
                "FROM (SELECT user_id2 AS friend_id FROM friendships WHERE user_id1 = ? " +
                "UNION ALL " +
                "SELECT user_id1 AS friend_id FROM friendships WHERE user_id2 = ?" +
                ") AS combined_friends " +
                "JOIN users ON users.id = combined_friends.friend_id ";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set the parameters for both occurrences of the `id`
            stmt.setLong(1, id);
            stmt.setLong(2, id);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Utilizator user = mapResultSetToUser(rs);
                    if (user != null) {
                        friends.add(user);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return friends;
    }


    @Override
    public Page<Prietenie<Long>> findAllOnPage(Pageable pageable) {
        int pageNr = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        int offset = pageNr * pageSize;

        List<Prietenie<Long>> friendships = new ArrayList<>();
        String sql = "SELECT * FROM friendships TOP ? OFFSET ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setInt(1,pageSize);
            stmt.setInt(2, offset);

            try(ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Prietenie<Long> prietenie = mapResultSetToPrietenie(rs);
                    friendships.add(prietenie);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Page<>(friendships, friendships.size());
    }

    public Page<Utilizator> findAllWith(Long id, Pageable pageable) {
        List<Utilizator> friends = new ArrayList<>();
        String sql = "SELECT users.id, users.first_name, users.last_name, users.user_name, users.password " +
                "FROM (" +
                "    SELECT user_id2 AS friend_id FROM friendships WHERE user_id1 = ? " +
                "    UNION ALL " +
                "    SELECT user_id1 AS friend_id FROM friendships WHERE user_id2 = ?" +
                ") AS combined_friends " +
                "JOIN users ON users.id = combined_friends.friend_id " +
                "LIMIT ? OFFSET ?";

        int pageNr = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        int offset = pageNr * pageSize;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set the parameters for both occurrences of the `id`
            stmt.setLong(1, id);
            stmt.setLong(2, id);
            stmt.setLong(3, pageSize);
            stmt.setLong(4, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Utilizator user = mapResultSetToUser(rs);
                    if (user != null) {
                        friends.add(user);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Consider using a logging framework instead of printing the stack trace
        }
        return new Page<>(friends, friends.size());
    }
}
