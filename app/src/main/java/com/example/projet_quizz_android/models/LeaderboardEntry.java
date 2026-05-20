package com.example.projet_quizz_android.models;

import com.google.gson.annotations.SerializedName;

public class LeaderboardEntry {

    private String uid;
    private String username;
    
    @SerializedName("profile_image_url")
    private String profileImageUrl;
    
    @SerializedName("score_value")
    private int bestScore;

    @SerializedName("category_name")
    private String categoryName;

    public LeaderboardEntry() {}

    public LeaderboardEntry(String username, String profileImageUrl, int bestScore, String categoryName) {
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.bestScore = bestScore;
        this.categoryName = categoryName;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public int getBestScore() { return bestScore; }
    public void setBestScore(int bestScore) { this.bestScore = bestScore; }
    
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}
