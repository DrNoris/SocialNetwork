package com.example.lab6.service;

import com.example.lab6.domain.FriendRequest;
import com.example.lab6.domain.Prietenie;
import com.example.lab6.domain.Tuple;
import com.example.lab6.domain.Utilizator;
import com.example.lab6.domain.observer.Observable;
import com.example.lab6.domain.paging.Pageable;
import com.example.lab6.repository.database.PrietenieDatabaseRepository;
import com.example.lab6.repository.database.RequestDatabaseRepository;
import com.example.lab6.repository.database.UtilizatorDatabaseRepository;

import java.time.LocalDateTime;
import java.util.*;

public class AppService extends Observable {
    private final UtilizatorDatabaseRepository utilizatoriRepo;
    private final PrietenieDatabaseRepository prietenieRepo;
    private final RequestDatabaseRepository requestRepo;

    public AppService (UtilizatorDatabaseRepository utilizatoriRepo,
                       PrietenieDatabaseRepository prietenieRepo,
                       RequestDatabaseRepository requestRepo) {
        this.utilizatoriRepo = utilizatoriRepo;
        this.prietenieRepo = prietenieRepo;
        this.requestRepo = requestRepo;
    }

    public Optional<List<Utilizator>> findAllName(String username){
        Optional<List<Utilizator>> users = utilizatoriRepo.findManyName(username);

        if (users.isEmpty())
            return null;

        return users;
    }

    public Optional<List<Utilizator>> getAllFriendships(Utilizator user) {
        List<Utilizator> friends = (List<Utilizator>) prietenieRepo.findAllWith(user.getId());

        return friends.isEmpty() ? Optional.empty() : Optional.of(friends);
    }

    public Optional<List<Utilizator>> getAllFriendships(Utilizator user, Pageable pageable) {
        List<Utilizator> friends = (List<Utilizator>) prietenieRepo.findAllWith(user.getId(), pageable).getElementsOnPage();

        return friends.isEmpty() ? Optional.empty() : Optional.of(friends);
    }

    public void sendRequest(Long sender_id, Long receiver_id){
        FriendRequest request = new FriendRequest(sender_id, receiver_id, "PENDING");
        if (requestRepo.save(request).isEmpty())
            notifyObservers();
        else
            throw new IllegalStateException("Failed to send friend request: Request already exists.");
    }

    public Optional<List<Utilizator>> getAllReceivedRequests(Utilizator currentUser) {
        List<Utilizator> requests = new ArrayList<>();

        requestRepo.findAllWith(currentUser.getId()).forEach(sender_id -> {
            Utilizator sender = utilizatoriRepo.findOne(sender_id).orElse(null);
            if (sender != null) {
                requests.add(sender);
            }
        });

        return requests.isEmpty() ? Optional.empty() : Optional.of(requests);
    }

    public void acceptPrietenie(Long receiver, Long sender) {
        try {
            addPrietenie(sender, receiver);
            requestRepo.modify(sender, receiver, 'Y');
            notifyObservers();
        }
        catch (IllegalStateException e){
            e.printStackTrace();
        }
    }

    public Optional<Prietenie<Long>> addPrietenie(Long id1, Long id2){
        Prietenie<Long> pr = new Prietenie<>();
        Utilizator u1 = utilizatoriRepo.findOne(id1).orElseThrow(IllegalArgumentException::new);
        Utilizator u2 = utilizatoriRepo.findOne(id2).orElseThrow(IllegalArgumentException::new);

        Prietenie<Long> existingFriendship = prietenieRepo.findOne(new Tuple<>(id1, id2)).orElse(null);
        if (existingFriendship != null) {
            throw new IllegalStateException("Friendship already exists between these users.");
        }

        existingFriendship = prietenieRepo.findOne(new Tuple<>(id2, id1)).orElse(null);
        if (existingFriendship != null) {
            throw new IllegalStateException("Friendship already exists between these users.");
        }

        pr.setFriendsFrom(LocalDateTime.now());
        pr.setId(new Tuple<>(id1, id2));

        u1.addFriend(u2);
        u2.addFriend(u1);

        return prietenieRepo.save(pr);
    }

    public Optional<Prietenie<Long>> deletePrietenie(Long id1, Long id2){
        Utilizator u1 = utilizatoriRepo.findOne(id1).orElse(null);
        Utilizator u2 = utilizatoriRepo.findOne(id2).orElse(null);

        if (u1 == null || u2 == null) {
            throw new IllegalArgumentException("One or both users do not exist.");
        }

        Tuple<Long, Long> id = new Tuple<>(id1, id2);
        Prietenie<Long> prietenie = prietenieRepo.findOne(id).orElse(null);
        if (prietenie == null) {
            id.setLeft(id2);
            id.setRight(id1);
            prietenie = prietenieRepo.findOne(id).orElse(null);
            if (prietenie == null)
                throw new IllegalStateException("Friendship does not exists between these users.");
        }

        u1.deleteFriend(u2);
        u2.deleteFriend(u1);

        notifyObservers();

        return prietenieRepo.delete(id);
    }

    public LocalDateTime getFriendshipDate(Long id, Long id1) {
        Optional<Prietenie<Long>> prietenie = prietenieRepo.findOne(new Tuple<>(id, id1));

        if (prietenie.isEmpty())
            prietenie = prietenieRepo.findOne(new Tuple<>(id1, id));

        return prietenie.get().getFriendsFrom();
    }

    public LocalDateTime getReceiveRequestsDate(Long sender, Long receiver) {
        Optional<FriendRequest> friendRequest = requestRepo.findOne(sender, receiver);

        if (friendRequest.isPresent()) {
            return friendRequest.get().getTimestamp();
        } else {
            System.out.println("No friend request found between sender " + sender + " and receiver " + receiver);
            return null;
        }
    }

    public void declinePrietenie(Long sender, Long receiver) {
        try {
            requestRepo.modify(sender, receiver, 'N');
            notifyObservers();
        }
        catch (IllegalStateException e){
            e.printStackTrace();
        }
    }

    public List<FriendRequest> getTopRequestsByUser(Long id, int top) {
        return (List<FriendRequest>) requestRepo.findAllActivity(id, top);
    }

    public Optional<Utilizator> findUserById(Long receiverId) {
        return utilizatoriRepo.findOne(receiverId);  // Return the result from findOne
    }
}
