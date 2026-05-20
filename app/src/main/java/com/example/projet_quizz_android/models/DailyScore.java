package com.example.projet_quizz_android.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class DailyScore {
    private String userId;
    private String username;
    private String date;
    private int score;
    private long timeRemaining;
    private String profileImageUrl;

    @ServerTimestamp
    private Date timestamp;

    public DailyScore() {}

    public DailyScore(String userId, String username, String date, int score, long timeRemaining, String profileImageUrl) {
        this.userId = userId;
        this.username = username;
        this.date = date;
        this.score = score;
        this.timeRemaining = timeRemaining;
        this.profileImageUrl = profileImageUrl;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public long getTimeRemaining() { return timeRemaining; }
    public void setTimeRemaining(long timeRemaining) { this.timeRemaining = timeRemaining; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
