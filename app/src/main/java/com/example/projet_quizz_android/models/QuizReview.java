package com.example.projet_quizz_android.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class QuizReview {
    private String userId;
    private String username;
    private float rating;
    private String comment;

    @ServerTimestamp
    private Date timestamp;

    public QuizReview() {}

    public QuizReview(String userId, String username, float rating, String comment) {
        this.userId = userId;
        this.username = username;
        this.rating = rating;
        this.comment = comment;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
