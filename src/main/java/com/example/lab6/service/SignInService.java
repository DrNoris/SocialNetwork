package com.example.lab6.service;

import com.example.lab6.domain.UserCredentials;
import com.example.lab6.domain.Utilizator;
import com.example.lab6.repository.database.UserCredentialsDatabaseRepository;
import com.example.lab6.repository.database.UtilizatorDatabaseRepository;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Optional;

public class SignInService {
    private final UtilizatorDatabaseRepository utilizatorDatabaseRepository;
    private final UserCredentialsDatabaseRepository userCredentialsDatabaseRepository;

    public SignInService(UtilizatorDatabaseRepository utilizatorDatabaseRepository,
                         UserCredentialsDatabaseRepository userCredentialsDatabaseRepository) {
        this.utilizatorDatabaseRepository = utilizatorDatabaseRepository;
        this.userCredentialsDatabaseRepository = userCredentialsDatabaseRepository;
    }

    public void signIn(String firstName, String lastName, String userName, String plainPassword) {
        try {
            Optional<Utilizator> savedUser = utilizatorDatabaseRepository.save(
                    new Utilizator(firstName, lastName, userName)
            );

            byte[] salt = generateSalt();
            byte[] hashedPassword = hashPassword(plainPassword, salt);

            UserCredentials credentials = new UserCredentials(savedUser.get().getId(), salt, hashedPassword);

            userCredentialsDatabaseRepository.save(credentials);
        } catch (Exception e) {
            throw new RuntimeException("Error during user sign-in process", e);
        }
    }

    private byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    private byte[] hashPassword(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return factory.generateSecret(spec).getEncoded();
    }
}
