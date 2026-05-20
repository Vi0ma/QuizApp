package com.example.projet_quizz_android.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class User {

    private String uid;
    private String username;
    private String email;
    private String profileImageUrl;
    private int bestScore;
    private String favoriteCategory;

    @ServerTimestamp
    private Date createdAt;

    public User() {}

    public User(String uid, String username, String email) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.profileImageUrl = "";
        this.bestScore = 0;
        this.favoriteCategory = "Général";
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public int getBestScore() { return bestScore; }
    public void setBestScore(int bestScore) { this.bestScore = bestScore; }

    public String getFavoriteCategory() { return favoriteCategory; }
    public void setFavoriteCategory(String favoriteCategory) { this.favoriteCategory = favoriteCategory; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
