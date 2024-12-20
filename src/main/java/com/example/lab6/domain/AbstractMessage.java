package com.example.lab6.domain;

import java.time.LocalDateTime;
import java.util.List;

public class AbstractMessage extends Entity<Long>{
    private Utilizator sender;
    private List<Utilizator> receivers;
    private LocalDateTime date;
    private Long replyTo = null;

    public AbstractMessage(Utilizator sender, List<Utilizator> receivers, LocalDateTime date) {
        super(null);
        this.sender = sender;
        this.receivers = receivers;
        this.date = date;
    }

    public Utilizator getSender() {
        return sender;
    }

    public void setSender(Utilizator sender) {
        this.sender = sender;
    }

    public List<Utilizator> getReceivers() {
        return receivers;
    }

    public void setReceivers(List<Utilizator> receivers) {
        this.receivers = receivers;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Long getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(Long replyTo) {
        this.replyTo = replyTo;
    }
}
