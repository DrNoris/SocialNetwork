package com.example.lab6.repository.database;



import com.example.lab6.domain.Utilizator;
import com.example.lab6.domain.validators.Validator;
import com.example.lab6.repository.Repository;

import java.sql.*;
import java.util.*;

public class UtilizatorDatabaseRepository implements Repository<Long, Utilizator> {
    public String user;
    public String password;
    public String url;
    public Validator<Utilizator> validator;

    public UtilizatorDatabaseRepository(String user, String password, String url, Validator<Utilizator> validator){
        this.user = user;
        this.url = url;
        this.password = password;
        this.validator = validator;
    }

    private Utilizator createUserFromUserSet(ResultSet set){
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

    @Override
    public Optional<Utilizator> findOne(Long id) {
        Utilizator utilizator;
        try(Connection connection = DriverManager.getConnection(url, user, password);
            ResultSet resultSet = connection.createStatement().executeQuery(String.format("select * from users U where U.id = '%d'", id))){
            if (resultSet.next()){
                utilizator = createUserFromUserSet(resultSet);
                return Optional.ofNullable(utilizator);
            }
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<Utilizator> findOneName(String username) {
        Utilizator utilizator = null;
        String query = "SELECT * FROM users U WHERE U.user_name = ?";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Set the parameter in the query
            preparedStatement.setString(1, username);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    utilizator = createUserFromUserSet(resultSet);
                    return Optional.ofNullable(utilizator);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<List<Utilizator>> findManyName(String username) {
        String query = "SELECT * FROM users U WHERE user_name LIKE ?";

        // Create a list to hold the users
        List<Utilizator> users = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Set the parameter in the query
            username = username + '%';  // Using 'LIKE' for partial matches
            preparedStatement.setString(1, username);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // Iterate over the result set and add users to the list
                while (resultSet.next()) {
                    Utilizator utilizator = createUserFromUserSet(resultSet);
                    users.add(utilizator);  // Add each user to the list
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Return the list of users if any are found, otherwise return Optional.empty
        return users.isEmpty() ? Optional.empty() : Optional.of(users);
    }


    @Override
    public Iterable<Utilizator> findAll() {
        Set<Utilizator> utilizatori = new HashSet<>();
        try(Connection connection = DriverManager.getConnection(url, user, password);
            ResultSet resultSet = connection.createStatement()
                    .executeQuery("select * from users order by id")){
            while (resultSet.next()){
                utilizatori.add(createUserFromUserSet(resultSet));
            }

            return utilizatori;
        }
        catch (SQLException e){
            e.printStackTrace();
        }

        return utilizatori;
    }

    @Override
    public Optional<Utilizator> save(Utilizator entity) {
        String sql = "insert into users (first_name, last_name, user_name, password) values (?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) { // Request the generated keys

            ps.setString(1, entity.getFirstName());
            ps.setString(2, entity.getLastName());
            ps.setString(3, entity.getUsername());
            ps.setString(4, entity.getPassword());

            int rowsAffected = ps.executeUpdate(); // Execute the insert

            if (rowsAffected > 0) {
                // Retrieve the generated ID for the inserted user
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        // Set the generated ID to the entity
                        entity.setId(generatedKeys.getLong(1));
                        validator.validate(entity);
                    }
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
    public Optional<Utilizator> delete(Long id) {
        String deleteFriendshipsSql = "DELETE FROM prietenie WHERE user_id1 = ? OR user_id2 = ?"; // SQL to delete friendships
        String deleteUserSql = "DELETE FROM users WHERE id = ?"; // SQL to delete user

        // Check if the user exists
        Optional<Utilizator> utilizator = findOne(id);
        if (utilizator.isEmpty()) {
            return Optional.empty();  // User not found
        }

        // Delete related friendships first
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            // Delete friendships where the user is involved
            try (PreparedStatement deleteFriendshipsStmt = connection.prepareStatement(deleteFriendshipsSql)) {
                deleteFriendshipsStmt.setLong(1, id);
                deleteFriendshipsStmt.setLong(2, id);
                deleteFriendshipsStmt.executeUpdate();  // Execute the deletion of friendships
            }

            // Now delete the user
            try (PreparedStatement deleteUserStmt = connection.prepareStatement(deleteUserSql)) {
                deleteUserStmt.setLong(1, id);
                int rowsAffected = deleteUserStmt.executeUpdate();  // Execute the deletion of the user
                if (rowsAffected > 0) {
                    return utilizator;  // Return the deleted user
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();  // Handle SQL exceptions
        }

        return Optional.empty();  // Return empty if deletion failed
    }


    @Override
    public Optional<Utilizator> update(Utilizator utilizator) {
        if(utilizator == null)
            throw new IllegalArgumentException("entity must be not null!");
        validator.validate(utilizator);
        String sql = "update users set first_name = ?, last_name = ? where id = ?";
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, utilizator.getFirstName());
            ps.setString(2, utilizator.getLastName());
            ps.setLong(3, utilizator.getId());
            if( ps.executeUpdate() > 0 )
                return Optional.empty();
            return Optional.of(utilizator);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
