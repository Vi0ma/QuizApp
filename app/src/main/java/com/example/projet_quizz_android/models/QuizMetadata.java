package com.example.projet_quizz_android.models;

import java.util.ArrayList;
import java.util.List;

public class QuizMetadata {
    private int id;
    private String name;
    private double averageRating;
    private int totalVotes;
    private List<String> badges;

    public QuizMetadata() {
        this.badges = new ArrayList<>();
    }

    public QuizMetadata(int id, String name) {
        this.id = id;
        this.name = name;
        this.averageRating = 0.0;
        this.totalVotes = 0;
        this.badges = new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public int getTotalVotes() { return totalVotes; }
    public void setTotalVotes(int totalVotes) { this.totalVotes = totalVotes; }

    public List<String> getBadges() { return badges; }
    public void setBadges(List<String> badges) { this.badges = badges; }
}
