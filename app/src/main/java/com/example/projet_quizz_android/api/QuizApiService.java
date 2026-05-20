package com.example.projet_quizz_android.api;

import com.example.projet_quizz_android.models.ApiQuestion;
import com.example.projet_quizz_android.models.Category;
import com.example.projet_quizz_android.models.LeaderboardEntry;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface QuizApiService {

    @GET("categories")
    Call<List<Category>> getCategories();

    @POST("categories")
    Call<Category> createCategory(@Body Category category);

    @PUT("categories/{id}")
    Call<Category> updateCategory(@Path("id") int id, @Body Category category);

    @DELETE("categories/{id}")
    Call<Void> deleteCategory(@Path("id") int id);

    @GET("questions")
    Call<List<ApiQuestion>> getQuestions(@Query("category_id") Integer categoryId);

    @POST("question")
    Call<ApiQuestion> createQuestion(@Body ApiQuestion question);

    @PUT("question/{id}")
    Call<ApiQuestion> updateQuestion(@Path("id") int id, @Body ApiQuestion question);

    @DELETE("question/{id}")
    Call<Void> deleteQuestion(@Path("id") int id);

    @GET("scores")
    Call<List<LeaderboardEntry>> getScores();

    @POST("score")
    Call<LeaderboardEntry> postScore(@Body LeaderboardEntry score);
    
    @DELETE("score/{id}")
    Call<Void> deleteScore(@Path("id") int id);
}
