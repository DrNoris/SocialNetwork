package com.example.lab6.service;

import com.example.lab6.domain.FriendRequest;
import com.example.lab6.domain.Prietenie;
import com.example.lab6.domain.Tuple;
import com.example.lab6.domain.Utilizator;
import com.example.lab6.repository.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Service {
    private final Repository<Tuple<Long, Long>, Prietenie<Long>> prieteniRepo;
    private final Repository<Long, Utilizator> utilizatoriRepo;
    private final Repository<Long, FriendRequest> requestRepo;

    public Service(Repository<Tuple<Long, Long>, Prietenie<Long>> prieteniRepo,
                   Repository<Long, Utilizator> utilizatoriRepo,
                   Repository<Long, FriendRequest> requestRepo) {
        this.prieteniRepo = prieteniRepo;
        this.utilizatoriRepo = utilizatoriRepo;
        this.requestRepo = requestRepo;
    }

    public Optional<Utilizator> addUtilizator(String firstName, String lastName, String username, String password){
        Utilizator u = new Utilizator(firstName, lastName, username, password);
        return utilizatoriRepo.save(u);
    }

    public Optional<Utilizator> deleteUtilizator(Long id) {
        Utilizator u = utilizatoriRepo.findOne(id).orElse(null);

        if (u == null) {
            return Optional.empty();
        }

        return utilizatoriRepo.delete(id);
    }


    public Optional<Prietenie<Long>> addPrietenie(Long id1, Long id2){
        Prietenie<Long> pr = new Prietenie<>();
        Utilizator u1 = utilizatoriRepo.findOne(id1).orElseThrow(IllegalArgumentException::new);
        Utilizator u2 = utilizatoriRepo.findOne(id2).orElseThrow(IllegalArgumentException::new);

        Prietenie<Long> existingFriendship = prieteniRepo.findOne(new Tuple<>(id1, id2)).orElse(null);
        if (existingFriendship != null) {
            throw new IllegalStateException("Friendship already exists between these users.");
        }

        existingFriendship = prieteniRepo.findOne(new Tuple<>(id2, id1)).orElse(null);
        if (existingFriendship != null) {
            throw new IllegalStateException("Friendship already exists between these users.");
        }

        pr.setFriendsFrom(LocalDateTime.now());
        pr.setId(new Tuple<>(id1, id2));

        u1.addFriend(u2);
        u2.addFriend(u1);

        return prieteniRepo.save(pr);
    }

    public Optional<Prietenie<Long>> deletePrietenie(Long id1, Long id2){
        Utilizator u1 = utilizatoriRepo.findOne(id1).orElse(null);
        Utilizator u2 = utilizatoriRepo.findOne(id2).orElse(null);

        if (u1 == null || u2 == null) {
            throw new IllegalArgumentException("One or both users do not exist.");
        }

        Tuple<Long, Long> id = new Tuple<>(id1, id2);
        Prietenie<Long> prietenie = prieteniRepo.findOne(id).orElse(null);
        if (prietenie == null) {
            id.setLeft(id2);
            id.setRight(id1);
            prietenie = prieteniRepo.findOne(id).orElse(null);
            if (prietenie == null)
                throw new IllegalStateException("Friendship does not exists between these users.");
        }

        u1.deleteFriend(u2);
        u2.deleteFriend(u1);

        return prieteniRepo.delete(id);
    }

    public int getNrNetworks(){
        Graph graph = new Graph();
        return graph.countConnectedComponents(prieteniRepo.findAll());
    }

    public List<Utilizator> getAllUsers() {
        List<Utilizator> users = new ArrayList<>();
        utilizatoriRepo.findAll().forEach(users::add);
        return users;
    }

    public Map.Entry<List<Long>, Integer> longestNetwork() {
        Iterable<Prietenie<Long>> edges = prieteniRepo.findAll();
        Graph graph = new Graph();
        return graph.longestPathInNetwork(edges);
    }
}
