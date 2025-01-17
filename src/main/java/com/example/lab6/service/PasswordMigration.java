package com.example.lab6.service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.*;

public class PasswordMigration {
    public static void main(String[] args) {
        String dbUrl = "jdbc:postgresql://localhost:5432/postgres";
        String dbUser = "postgres";
        String dbPassword = "noris2580";

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            String selectQuery = "SELECT id, password FROM users";
            PreparedStatement selectStmt = connection.prepareStatement(selectQuery);
            ResultSet resultSet = selectStmt.executeQuery();


            String insertQuery = "INSERT INTO user_credentials (user_id, salt, password) VALUES (?, ?, ?)";
            PreparedStatement insertStmt = connection.prepareStatement(insertQuery);

            while (resultSet.next()) {
                long userId = resultSet.getLong("id");
                String plainPassword = resultSet.getString("password");

                byte[] salt = generateSalt();
                byte[] hashedPassword = hashPassword(plainPassword, salt);

                // Insert the user credentials into the database
                insertStmt.setLong(1, userId);
                insertStmt.setBytes(2, salt);
                insertStmt.setBytes(3, hashedPassword);
                insertStmt.executeUpdate();
            }

            System.out.println("Password migration completed successfully.");
        } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    // Generate a random salt
    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    // Hash the password with the salt
    private static byte[] hashPassword(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return factory.generateSecret(spec).getEncoded();
    }
}
