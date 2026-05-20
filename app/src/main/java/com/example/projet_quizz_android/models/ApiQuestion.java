package com.example.projet_quizz_android.models;

import com.google.gson.annotations.SerializedName;

public class ApiQuestion {
    private int id;
    private String text;
    @SerializedName("image_url")
    private String imageUrl;
    @SerializedName("option_a")
    private String optionA;
    @SerializedName("option_b")
    private String optionB;
    @SerializedName("option_c")
    private String optionC;
    @SerializedName("option_d")
    private String optionD;
    @SerializedName("correct_answer")
    private String correctAnswer;
    @SerializedName("category_id")
    private int categoryId;

    public int getId() { return id; }
    public String getText() { return text; }
    public String getImageUrl() { return imageUrl; }
    public String getOptionA() { return optionA; }
    public String getOptionB() { return optionB; }
    public String getOptionC() { return optionC; }
    public String getOptionD() { return optionD; }
    public String getCorrectAnswer() { return correctAnswer; }
    public int getCategoryId() { return categoryId; }
}
