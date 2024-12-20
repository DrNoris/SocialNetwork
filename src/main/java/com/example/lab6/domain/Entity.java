package com.example.lab6.domain;

public class Entity<ID>  {

    public Entity(ID id) {
        this.id = id;
    }

    private ID id;
    public ID getId() {
        return id;
    }
    public void setId(ID id) {
        this.id = id;
    }
}