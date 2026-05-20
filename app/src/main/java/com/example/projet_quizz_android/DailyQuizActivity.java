package com.example.projet_quizz_android;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.bumptech.glide.Glide;
import com.example.projet_quizz_android.api.RetrofitClient;
import com.example.projet_quizz_android.models.ApiQuestion;
import com.example.projet_quizz_android.models.User;
import com.example.projet_quizz_android.repository.DailyRepository;
import com.example.projet_quizz_android.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DailyQuizActivity extends AppCompatActivity {

    private TextView tvCategoryName, tvTimer, tvQuestion, tvHeaderThemeName;
    private CardView cvOption1, cvOption2, cvOption3, cvOption4;
    private TextView tvOption1, tvOption2, tvOption3, tvOption4;
    private Button btnNext;
    private ProgressBar progressBarQuestions, progressBarSaving;
    private RelativeLayout layoutFinished;
    private TextView tvFinishedScore;
    
    private CircleImageView ivHeaderProfile;

    private List<ApiQuestion> questionList = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int categoryId;
    private String categoryName;
    private boolean answerSelected = false;

    private CountDownTimer countDownTimer;
    private long timeRemainingMs = 60000;

    private FirebaseAuth myAuth;
    private DailyRepository dailyRepository;
    private UserRepository userRepository;
    private User currentUserModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_quiz);

        myAuth = FirebaseAuth.getInstance();
        dailyRepository = DailyRepository.getInstance();
        userRepository = UserRepository.getInstance();

        categoryId = getIntent().getIntExtra("CATEGORY_ID", 1);
        categoryName = getIntent().getStringExtra("CATEGORY_NAME");

        initViews();
        setupHeader();
        fetchQuestions();

        if (categoryName != null) {
            tvHeaderThemeName.setText(categoryName);
            tvCategoryName.setText("Catégorie : " + categoryName);
        }

        btnNext.setOnClickListener(v -> {
            if (answerSelected) {
                currentQuestionIndex++;
                if (currentQuestionIndex < questionList.size()) {
                    displayQuestion(currentQuestionIndex);
                } else {
                    finishQuiz();
                }
            } else {
                Toast.makeText(this, "Veuillez choisir une réponse", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews() {
        tvCategoryName = findViewById(R.id.tvCategoryName);
        tvTimer = findViewById(R.id.tvTimer);
        tvQuestion = findViewById(R.id.tvQuestion);
        cvOption1 = findViewById(R.id.cvOption1);
        cvOption2 = findViewById(R.id.cvOption2);
        cvOption3 = findViewById(R.id.cvOption3);
        cvOption4 = findViewById(R.id.cvOption4);
        tvOption1 = findViewById(R.id.tvOption1);
        tvOption2 = findViewById(R.id.tvOption2);
        tvOption3 = findViewById(R.id.tvOption3);
        tvOption4 = findViewById(R.id.tvOption4);
        btnNext = findViewById(R.id.btnNext);
        progressBarQuestions = findViewById(R.id.progressBarQuestions);
        ivHeaderProfile = findViewById(R.id.ivHeaderProfile);
        tvHeaderThemeName = findViewById(R.id.tvHeaderThemeName);
        
        layoutFinished = findViewById(R.id.layoutFinished);
        tvFinishedScore = findViewById(R.id.tvFinishedScore);
        progressBarSaving = findViewById(R.id.progressBarSaving);

        View.OnClickListener optionClickListener = v -> {
            if (!answerSelected) {
                checkAnswer((CardView) v, ((TextView)((CardView)v).getChildAt(0)).getText().toString());
            }
        };

        cvOption1.setOnClickListener(optionClickListener);
        cvOption2.setOnClickListener(optionClickListener);
        cvOption3.setOnClickListener(optionClickListener);
        cvOption4.setOnClickListener(optionClickListener);
    }

    private void setupHeader() {
        FirebaseUser currentUser = myAuth.getCurrentUser();
        if (currentUser != null) {
            userRepository.listenToUser(currentUser.getUid(), (documentSnapshot, error) -> {
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    currentUserModel = documentSnapshot.toObject(User.class);
                    if (currentUserModel != null && currentUserModel.getProfileImageUrl() != null && !currentUserModel.getProfileImageUrl().isEmpty()) {
                        Glide.with(this).load(currentUserModel.getProfileImageUrl()).circleCrop().placeholder(R.drawable.quiz_logo).into(ivHeaderProfile);
                    }
                }
            });
        }
        ivHeaderProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void fetchQuestions() {
        RetrofitClient.getApiService().getQuestions(categoryId).enqueue(new Callback<List<ApiQuestion>>() {
            @Override
            public void onResponse(Call<List<ApiQuestion>> call, Response<List<ApiQuestion>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    questionList = response.body();
                    progressBarQuestions.setMax(questionList.size());
                    displayQuestion(0);
                    startTimer();
                } else {
                    finish();
                }
            }
            @Override public void onFailure(Call<List<ApiQuestion>> call, Throwable t) { finish(); }
        });
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeRemainingMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemainingMs = millisUntilFinished;
                tvTimer.setText(String.valueOf(millisUntilFinished / 1000));
            }
            @Override public void onFinish() { finishQuiz(); }
        }.start();
    }

    private void displayQuestion(int index) {
        answerSelected = false;
        btnNext.setVisibility(View.GONE);
        progressBarQuestions.setProgress(index);
        resetCardStyles();

        ApiQuestion q = questionList.get(index);
        tvQuestion.setText(q.getText());
        tvOption1.setText(q.getOptionA());
        tvOption2.setText(q.getOptionB());
        
        if (q.getOptionC() != null && !q.getOptionC().isEmpty()) {
            cvOption3.setVisibility(View.VISIBLE); tvOption3.setText(q.getOptionC());
        } else cvOption3.setVisibility(View.GONE);
        
        if (q.getOptionD() != null && !q.getOptionD().isEmpty()) {
            cvOption4.setVisibility(View.VISIBLE); tvOption4.setText(q.getOptionD());
        } else cvOption4.setVisibility(View.GONE);
    }

    private void resetCardStyles() {
        cvOption1.setCardBackgroundColor(Color.WHITE);
        cvOption2.setCardBackgroundColor(Color.WHITE);
        cvOption3.setCardBackgroundColor(Color.WHITE);
        cvOption4.setCardBackgroundColor(Color.WHITE);
    }

    private void checkAnswer(CardView clickedCard, String selectedText) {
        answerSelected = true;
        ApiQuestion q = questionList.get(currentQuestionIndex);
        String correctText = "";
        if ("A".equals(q.getCorrectAnswer())) correctText = q.getOptionA();
        else if ("B".equals(q.getCorrectAnswer())) correctText = q.getOptionB();
        else if ("C".equals(q.getCorrectAnswer())) correctText = q.getOptionC();
        else if ("D".equals(q.getCorrectAnswer())) correctText = q.getOptionD();

        if (selectedText.equals(correctText)) {
            clickedCard.setCardBackgroundColor(Color.parseColor("#4CAF50"));
            score++;
        } else {
            clickedCard.setCardBackgroundColor(Color.parseColor("#F44336"));
            if (tvOption1.getText().equals(correctText)) cvOption1.setCardBackgroundColor(Color.parseColor("#4CAF50"));
            else if (tvOption2.getText().equals(correctText)) cvOption2.setCardBackgroundColor(Color.parseColor("#4CAF50"));
            else if (tvOption3.getText().equals(correctText)) cvOption3.setCardBackgroundColor(Color.parseColor("#4CAF50"));
            else if (tvOption4.getText().equals(correctText)) cvOption4.setCardBackgroundColor(Color.parseColor("#4CAF50"));
        }
        btnNext.setVisibility(View.VISIBLE);
    }

    private void finishQuiz() {
        if (countDownTimer != null) countDownTimer.cancel();
        layoutFinished.setVisibility(View.VISIBLE);
        int finalPercentage = (score * 100) / questionList.size();
        tvFinishedScore.setText("Score : " + finalPercentage + "%");

        FirebaseUser user = myAuth.getCurrentUser();
        if (user != null && currentUserModel != null) {
            dailyRepository.saveDailyScore(user.getUid(), currentUserModel.getUsername(), finalPercentage, timeRemainingMs, currentUserModel.getProfileImageUrl())
                    .addOnCompleteListener(task -> {
                        startActivity(new Intent(this, DailyLeaderboardActivity.class));
                        finish();
                    });
        } else finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
