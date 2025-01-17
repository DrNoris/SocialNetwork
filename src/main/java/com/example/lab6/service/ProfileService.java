package com.example.lab6.service;

import com.example.lab6.domain.Utilizator;
import com.example.lab6.repository.database.UtilizatorDatabaseRepository;

public class ProfileService {
    private UtilizatorDatabaseRepository userRepo;

    public ProfileService(UtilizatorDatabaseRepository userRepo){
        this.userRepo = userRepo;
    }

    public void updateUser(Utilizator user){
        userRepo.update(user);
    }
}
