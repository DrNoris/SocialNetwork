package com.example.lab6.domain;

import java.time.LocalDateTime;

public class FriendRequest extends Entity<Long>{
    private Long senderId;
    private Long receiverId;
    private String status; // PENDING, ACCEPTED, REJECTED
    private LocalDateTime timestamp;

    public FriendRequest(Long senderId, Long receiverId, String status) {
        super(null);
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "FriendRequest{" +
                "id=" + getId() +
                ", senderId=" + senderId +
                ", receiverId=" + receiverId +
                ", status='" + status + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
