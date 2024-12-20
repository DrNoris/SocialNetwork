package com.example.lab6.domain;

import java.time.LocalDateTime;
import java.util.List;

public class Message extends AbstractMessage {
    private String message;

    public Message(Utilizator sender, List<Utilizator> receivers, String message, LocalDateTime date) {
        super(sender, receivers, date);
        this.message = message;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
