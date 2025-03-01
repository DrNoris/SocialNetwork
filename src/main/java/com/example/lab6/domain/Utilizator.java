package com.example.lab6.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Utilizator extends Entity<Long>{
    private String firstName;
    private String lastName;
    private String username;
    private List<Utilizator> friends;
    private byte[] profilePicture;

    public Utilizator(String firstName, String lastName, String username) {
        super(null);
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.friends = new ArrayList<>();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<Utilizator> getFrinds(){
        return this.friends;
    }

    public void setFriends(List<Utilizator> friends){
        this.friends = friends;
    }

    public void addFriend(Utilizator newFriend){
        friends.add(newFriend);
    }

    public void deleteFriend(Utilizator Friend){
        friends.remove(Friend);
    }

    public byte[] getProfilePicture(){
        return profilePicture;
    }

    public void setProfilePicture(byte[] photo){
        this.profilePicture = photo;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public String getUsername(){
        return this.username;
    }

    @Override
    public String toString() {
        return "Utilizator{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Utilizator)) return false;
        Utilizator that = (Utilizator) o;
        return getFirstName().equals(that.getFirstName()) &&
                getLastName().equals(that.getLastName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFirstName(), getLastName());
    }
}