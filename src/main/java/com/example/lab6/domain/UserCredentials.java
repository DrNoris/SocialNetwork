package com.example.lab6.domain;

public class UserCredentials extends Entity<Long> {
    private long userId;
    private byte[] salt;
    private byte[] password;

    public UserCredentials(long userId, byte[] salt, byte[] password) {
        super(userId);
        this.userId = userId;
        this.salt = salt;
        this.password = password;
    }

    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    public byte[] getPassword() {
        return password;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }
}
