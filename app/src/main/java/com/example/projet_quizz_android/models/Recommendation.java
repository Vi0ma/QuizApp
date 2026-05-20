package com.example.projet_quizz_android.models;

public class Recommendation {
    private String recommendedCategoryName;
    private int recommendedCategoryId;
    private String reason;

    public Recommendation(String recommendedCategoryName, int recommendedCategoryId, String reason) {
        this.recommendedCategoryName = recommendedCategoryName;
        this.recommendedCategoryId = recommendedCategoryId;
        this.reason = reason;
    }

    public String getRecommendedCategoryName() {
        return recommendedCategoryName;
    }

    public int getRecommendedCategoryId() {
        return recommendedCategoryId;
    }

    public String getReason() {
        return reason;
    }
}
