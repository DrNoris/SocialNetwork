package com.example.lab6.domain;

import java.time.LocalDateTime;

public class Prietenie<T> extends Entity<Tuple<T, T>> {
    private LocalDateTime friendsFrom;

    public Prietenie(T left, T right, LocalDateTime friendsFrom) {
        super(new Tuple<>(left, right));
        this.friendsFrom = friendsFrom;
    }

    public Prietenie() {
        super(null); // Inițializează cheia cu `null`
    }

    public LocalDateTime getFriendsFrom() {
        return friendsFrom;
    }

    public void setFriendsFrom(LocalDateTime friendsFrom) {
        this.friendsFrom = friendsFrom;
    }

    @Override
    public String toString() {
        return "Prietenie{" +
                "id1=" + getId().getLeft() +
                ", id2=" + getId().getRight() +
                ", friendsFrom=" + friendsFrom +
                '}';
    }
}
