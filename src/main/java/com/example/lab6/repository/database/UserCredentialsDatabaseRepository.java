package com.example.lab6.repository.database;

import com.example.lab6.domain.UserCredentials;
import com.example.lab6.domain.Utilizator;
import com.example.lab6.domain.validators.Validator;
import com.example.lab6.repository.Repository;

import java.sql.*;
import java.util.Optional;
import java.util.OptionalInt;

public class UserCredentialsDatabaseRepository implements Repository<Long, UserCredentials> {
    public String user;
    public String password;
    public String url;

    public UserCredentialsDatabaseRepository(String user, String password, String url){
        this.user = user;
        this.url = url;
        this.password = password;
    }

    private UserCredentials createUserCredentialsFromSet(ResultSet set){
        try{
            long userId = set.getLong("user_id");
            byte[] salt = set.getBytes("salt");
            byte[] password = set.getBytes("password");

            UserCredentials userCredentials = new UserCredentials(userId, salt, password);

            return userCredentials;
        }
        catch (SQLException e){
            return null;
        }
    }


    @Override
    public Optional<UserCredentials> findOne(Long idUser) {
        UserCredentials userCredentials;

        try(Connection connection = DriverManager.getConnection(url, user, password);
            ResultSet resultSet = connection.createStatement().executeQuery(
                    String.format("select * from user_credentials UC where UC.id = '%d'", idUser))){
            if (resultSet.next()){
                userCredentials = createUserCredentialsFromSet(resultSet);
                return Optional.ofNullable(userCredentials);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public Optional<UserCredentials> findOneUsingUsername(String username){
        UserCredentials userCredentials;

        try(Connection connection = DriverManager.getConnection(url, user, password);
            ResultSet resultSet = connection.createStatement().executeQuery(
                    String.format("select * from user_credentials " +
                            "join users on users.id = user_credentials.user_id" +
                            " where users.user_name = '%s'", username))){
            if (resultSet.next()){
                userCredentials = createUserCredentialsFromSet(resultSet);
                return Optional.ofNullable(userCredentials);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }


    @Override
    public Iterable<UserCredentials> findAll() {
        return null;
    }

    @Override
    public Optional<UserCredentials> save(UserCredentials entity) {
        String sql = "insert into user_credentials (user_id, salt, password) values (?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, entity.getId());
            ps.setBytes(2, entity.getSalt());
            ps.setBytes(3, entity.getPassword());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                return Optional.empty();
            }
            return Optional.ofNullable(entity);
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.ofNullable(entity);
        }
    }

    @Override
    public Optional<UserCredentials> delete(Long aLong) {
        return Optional.empty();
    }

    @Override
    public Optional<UserCredentials> update(UserCredentials entity) {
        return Optional.empty();
    }
}
