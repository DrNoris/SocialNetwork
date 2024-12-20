package com.example.lab6.service;

import com.example.lab6.domain.Utilizator;
import com.example.lab6.repository.Repository;
import com.example.lab6.repository.database.UtilizatorDatabaseRepository;

import java.util.Optional;

public class LoginService {
    private final UtilizatorDatabaseRepository utilizatoriRepo;

    public LoginService (UtilizatorDatabaseRepository utilizatoriRepo) {
        this.utilizatoriRepo = utilizatoriRepo;
    }

    public Optional<Utilizator> login(String username, String password){
        Utilizator user = utilizatoriRepo.findOneName(username)
                .orElseThrow( () -> new IllegalArgumentException("User does not exist"));

        if (!user.getPassword().equals(password))
            throw new IllegalArgumentException("Wrong password");

        return Optional.of(user);
    }
}
