package com.example.projet_quizz_android.models;

public class DailyChallenge {
    private String date;
    private int categoryId;
    private String categoryName;

    public DailyChallenge() {}

    public DailyChallenge(String date, int categoryId, String categoryName) {
        this.date = date;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}
