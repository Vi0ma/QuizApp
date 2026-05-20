package com.example.projet_quizz_android;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.projet_quizz_android.api.RetrofitClient;
import com.example.projet_quizz_android.models.ApiQuestion;
import com.example.projet_quizz_android.models.User;
import com.example.projet_quizz_android.repository.UserRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DynamicQuizActivity extends AppCompatActivity {

    TextView tvQuestionCount, tvQuestionText, tvSpeechResult, tvTimer, tvHeaderThemeName;
    ImageView ivQuestion;
    RadioGroup rgOptions;
    RadioButton rbA, rbB, rbC, rbD;
    Button btnNext;
    ProgressBar progressBar;
    FloatingActionButton fabMic;

    List<ApiQuestion> questionList = new ArrayList<>();
    int currentQuestionIndex = 0;
    int score = 0;
    int categoryId;
    String categoryName;
    
    CircleImageView ivHeaderProfile;
    FirebaseAuth myAuth;
    UserRepository userRepository;

    private CountDownTimer countDownTimer;
    private static final long TIMER_DURATION = 40000;

    private final ActivityResultLauncher<Intent> voiceLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null && !matches.isEmpty()) {
                        processUltraSmartMatching(matches);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_quiz);

        myAuth = FirebaseAuth.getInstance();
        userRepository = UserRepository.getInstance();

        categoryId = getIntent().getIntExtra("CATEGORY_ID", -1);
        categoryName = getIntent().getStringExtra("CATEGORY_NAME");

        initViews();
        setupHeader();
        loadQuestions();

        if (categoryName != null) {
            tvHeaderThemeName.setText(categoryName);
        }

        btnNext.setOnClickListener(v -> checkAnswerAndNext());
        fabMic.setOnClickListener(v -> startAdvancedVoiceRecognition());
    }

    private void initViews() {
        tvQuestionCount = findViewById(R.id.tvQuestionCount);
        tvQuestionText = findViewById(R.id.tvQuestionText);
        tvSpeechResult = findViewById(R.id.tvSpeechResult);
        tvTimer = findViewById(R.id.tvTimer);
        ivQuestion = findViewById(R.id.ivQuestion);
        rgOptions = findViewById(R.id.rgOptions);
        rbA = findViewById(R.id.rbOptionA);
        rbB = findViewById(R.id.rbOptionB);
        rbC = findViewById(R.id.rbOptionC);
        rbD = findViewById(R.id.rbOptionD);
        btnNext = findViewById(R.id.btnNextQuestion);
        progressBar = findViewById(R.id.progressBarQuiz);
        ivHeaderProfile = findViewById(R.id.ivHeaderProfile);
        tvHeaderThemeName = findViewById(R.id.tvHeaderThemeName);
        fabMic = findViewById(R.id.fabMic);
    }

    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(TIMER_DURATION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText("⏱️ " + (millisUntilFinished / 1000) + "s");
            }

            @Override
            public void onFinish() {
                tvTimer.setText("⏱️ 0s");
                Toast.makeText(DynamicQuizActivity.this, "Temps écoulé !", Toast.LENGTH_SHORT).show();
                proceedToNext(false);
            }
        }.start();
    }

    private void startAdvancedVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Dites A, B, C ou D clairement");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);

        try {
            voiceLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Service Google Voice non disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void processUltraSmartMatching(ArrayList<String> matches) {
        boolean found = false;
        String bestDetected = "";

        for (String match : matches) {
            String input = match.toLowerCase().trim();
            
            if (input.matches(".*\\b(a|ah|ha|alpha|un|1|réponse a|option a)\\b.*")) { 
                rbA.setChecked(true); found = true; bestDetected = "A"; break; 
            }
            if (input.matches(".*\\b(b|bé|be|bravo|deux|2|réponse b|option b)\\b.*")) { 
                rbB.setChecked(true); found = true; bestDetected = "B"; break; 
            }
            if (input.matches(".*\\b(c|cé|ce|charlie|trois|3|réponse c|option c|c'est)\\b.*")) { 
                rbC.setChecked(true); found = true; bestDetected = "C"; break; 
            }
            if (input.matches(".*\\b(d|dé|de|delta|quatre|4|réponse d|option d|the)\\b.*")) { 
                rbD.setChecked(true); found = true; bestDetected = "D"; break; 
            }
        }

        if (found) {
            tvSpeechResult.setText("Compris : Option " + bestDetected + " ! ✅");
            new Handler(Looper.getMainLooper()).postDelayed(this::checkAnswerAndNext, 1000);
        } else {
            Toast.makeText(this, "L'IA n'a pas reconnu la lettre", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayQuestion() {
        if (questionList.isEmpty()) return;
        ApiQuestion q = questionList.get(currentQuestionIndex);
        tvQuestionCount.setText("Question " + (currentQuestionIndex + 1) + "/" + questionList.size());
        tvQuestionText.setText(q.getText());
        
        rbA.setText("A. " + q.getOptionA());
        rbB.setText("B. " + q.getOptionB());
        rbC.setText("C. " + q.getOptionC());
        rbD.setText("D. " + q.getOptionD());
        
        rgOptions.clearCheck();
        tvSpeechResult.setText("Cliquez sur le micro pour répondre");

        if (q.getImageUrl() != null && !q.getImageUrl().isEmpty()) {
            ivQuestion.setVisibility(View.VISIBLE);
            Glide.with(this).load(q.getImageUrl()).into(ivQuestion);
        } else {
            ivQuestion.setVisibility(View.GONE);
        }
        
        startTimer();
    }

    private void loadQuestions() {
        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService().getQuestions(categoryId).enqueue(new Callback<List<ApiQuestion>>() {
            @Override
            public void onResponse(Call<List<ApiQuestion>> call, Response<List<ApiQuestion>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    questionList.clear();
                    questionList.addAll(response.body());
                    displayQuestion();
                }
            }
            @Override public void onFailure(Call<List<ApiQuestion>> call, Throwable t) { progressBar.setVisibility(View.GONE); }
        });
    }

    private void checkAnswerAndNext() {
        if (questionList.isEmpty() || currentQuestionIndex >= questionList.size()) return;

        int selectedId = rgOptions.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Sélectionnez une réponse", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRb = findViewById(selectedId);
        String selectedAnswer = "";
        if (selectedRb == rbA) selectedAnswer = "A";
        else if (selectedRb == rbB) selectedAnswer = "B";
        else if (selectedRb == rbC) selectedAnswer = "C";
        else if (selectedRb == rbD) selectedAnswer = "D";

        boolean isCorrect = selectedAnswer.equals(questionList.get(currentQuestionIndex).getCorrectAnswer());
        proceedToNext(isCorrect);
    }

    private void proceedToNext(boolean isCorrect) {
        if (countDownTimer != null) countDownTimer.cancel();
        
        if (isCorrect) score++;

        currentQuestionIndex++;
        if (currentQuestionIndex < questionList.size()) {
            displayQuestion();
        } else {
            Intent intent = new Intent(this, Score.class);
            intent.putExtra("score", (score * 100) / questionList.size());
            intent.putExtra("CATEGORY_NAME", categoryName);
            intent.putExtra("CATEGORY_ID", categoryId);
            startActivity(intent);
            finish();
        }
    }

    private void setupHeader() {
        FirebaseUser currentUser = myAuth.getCurrentUser();
        if (currentUser != null) {
            userRepository.listenToUser(currentUser.getUid(), (documentSnapshot, error) -> {
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null && user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                        Glide.with(this).load(user.getProfileImageUrl()).circleCrop().placeholder(R.drawable.quiz_logo).into(ivHeaderProfile);
                    }
                }
            });
        }
        ivHeaderProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
