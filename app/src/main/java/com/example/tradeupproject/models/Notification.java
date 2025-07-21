package com.example.tradeupproject.models;

import com.google.firebase.Timestamp;

public class Notification {
    private String id;
    private String userId;
    private String title;
    private String message;
    private Timestamp timestamp;

    public Notification() { /* Firestore requires */ }

    // Getters / Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
