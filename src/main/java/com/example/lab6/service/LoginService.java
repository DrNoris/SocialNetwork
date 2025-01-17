package com.example.lab6.service;

import com.example.lab6.domain.UserCredentials;
import com.example.lab6.domain.Utilizator;
import com.example.lab6.repository.Repository;
import com.example.lab6.repository.database.UserCredentialsDatabaseRepository;
import com.example.lab6.repository.database.UtilizatorDatabaseRepository;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Optional;

public class LoginService {
    private final UserCredentialsDatabaseRepository userCredentialsDatabaseRepository;
    private final UtilizatorDatabaseRepository utilizatorDatabaseRepository;

    public LoginService (UserCredentialsDatabaseRepository userCredentialsDatabaseRepository,
                         UtilizatorDatabaseRepository utilizatorDatabaseRepository) {
        this.userCredentialsDatabaseRepository = userCredentialsDatabaseRepository;
        this.utilizatorDatabaseRepository = utilizatorDatabaseRepository;
    }

    public Optional<Utilizator> login(String username, byte[] password){
        Optional<UserCredentials> userCredentials = userCredentialsDatabaseRepository.findOneUsingUsername(username);

        if (userCredentials.isPresent()){
            try {
                KeySpec spec = new PBEKeySpec(new String(password).toCharArray(),
                        userCredentials.get().getSalt(),
                        65536, 128);
                SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                byte[] hash = factory.generateSecret(spec).getEncoded();

                if (Arrays.equals(hash, userCredentials.get().getPassword()))
                    return utilizatorDatabaseRepository.findOne(userCredentials.get().getId());
                else
                    return Optional.of(null);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (InvalidKeySpecException e) {
                throw new RuntimeException(e);
            }
        }
        return Optional.of(null);
    }
}
