package com.example.projet_quizz_android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.projet_quizz_android.api.RetrofitClient;
import com.example.projet_quizz_android.models.LeaderboardEntry;
import com.example.projet_quizz_android.models.User;
import com.example.projet_quizz_android.repository.QuizRepository;
import com.example.projet_quizz_android.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Score extends AppCompatActivity {

    ProgressBar pb;
    TextView tvscore, tvScoreMessage, tvBestScore, tvHeaderPageTitle;
    Button bTryAgain, bLogout, bProfile, bLeaderboard, btnSubmitReview;
    RatingBar ratingBarQuiz;
    EditText etComment;
    int score;
    int categoryId;

    CircleImageView ivHeaderProfile;
    FirebaseAuth myAuth;
    UserRepository userRepository;
    QuizRepository quizRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_score);

        myAuth = FirebaseAuth.getInstance();
        userRepository = UserRepository.getInstance();
        quizRepository = QuizRepository.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        pb            = findViewById(R.id.progressBar);
        tvscore       = findViewById(R.id.textView6);
        tvScoreMessage = findViewById(R.id.tvScoreMessage);
        tvBestScore   = findViewById(R.id.tvBestScore);
        bLogout       = findViewById(R.id.button2);
        bTryAgain     = findViewById(R.id.button3);
        bProfile      = findViewById(R.id.btnProfile);
        bLeaderboard  = findViewById(R.id.btnLeaderboard);
        ivHeaderProfile = findViewById(R.id.ivHeaderProfile);
        tvHeaderPageTitle = findViewById(R.id.tvHeaderThemeName);
        
        if (tvHeaderPageTitle != null) {
            tvHeaderPageTitle.setText("Résultats");
        }

        ratingBarQuiz = findViewById(R.id.ratingBarQuiz);
        etComment = findViewById(R.id.etComment);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);

        score = getIntent().getIntExtra("score", 0);
        int percentage = score;

        tvscore.setText(percentage + " %");
        pb.setProgress(percentage);

        if (percentage >= 80) {
            tvScoreMessage.setText("🏆 Excellent ! Tu es un champion !");
        } else if (percentage >= 60) {
            tvScoreMessage.setText("👍 Bien joué ! Continue comme ça !");
        } else {
            tvScoreMessage.setText("💪 Entraîne-toi encore !");
        }

        setupHeader();

        String categoryName = getIntent().getStringExtra("CATEGORY_NAME");
        if (categoryName == null) categoryName = "Général";
        categoryId = getIntent().getIntExtra("CATEGORY_ID", 1);

        saveScoreToFirestore(percentage, categoryName);
        saveScoreToApi(percentage, categoryName);

        bTryAgain.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), CategoryChoiceActivity.class)));

        bLogout.setOnClickListener(v -> {
            myAuth.signOut();
            Toast.makeText(Score.this, "À bientôt !", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });

        bProfile.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class)));

        bLeaderboard.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), LeaderboardActivity.class)));
                
        final String finalCatName = categoryName;
        btnSubmitReview.setOnClickListener(v -> {
            float rating = ratingBarQuiz.getRating();
            if (rating == 0) {
                Toast.makeText(this, "Veuillez donner une note (1 à 5 étoiles)", Toast.LENGTH_SHORT).show();
                return;
            }
            
            FirebaseUser user = myAuth.getCurrentUser();
            if (user != null) {
                String comment = etComment.getText().toString().trim();
                String username = user.getEmail() != null ? user.getEmail().split("@")[0] : "Joueur";
                
                quizRepository.submitReview(finalCatName, categoryId, user.getUid(), username, rating, comment)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Avis enregistré ! Merci 😊", Toast.LENGTH_SHORT).show();
                            findViewById(R.id.cardRating).setVisibility(View.GONE);
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Erreur lors de l'envoi de l'avis", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void setupHeader() {
        FirebaseUser currentUser = myAuth.getCurrentUser();
        if (currentUser != null) {
            userRepository.listenToUser(currentUser.getUid(), (documentSnapshot, error) -> {
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null && user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                        Glide.with(this)
                                .load(user.getProfileImageUrl())
                                .circleCrop()
                                .placeholder(R.drawable.quiz_logo)
                                .into(ivHeaderProfile);
                    }
                }
            });
        }
        
        ivHeaderProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });
    }

    private void saveScoreToApi(int percentage, String categoryName) {
        FirebaseUser user = myAuth.getCurrentUser();
        String username = (user != null && user.getEmail() != null) ? user.getEmail().split("@")[0] : "Anonyme";
        LeaderboardEntry entry = new LeaderboardEntry(username, "", percentage, categoryName);
        RetrofitClient.getApiService().postScore(entry).enqueue(new Callback<LeaderboardEntry>() {
            @Override public void onResponse(Call<LeaderboardEntry> call, Response<LeaderboardEntry> response) {}
            @Override public void onFailure(Call<LeaderboardEntry> call, Throwable t) {}
        });
    }

    private void saveScoreToFirestore(int percentage, String categoryName) {
        FirebaseUser user = myAuth.getCurrentUser();
        if (user == null) return;
        String uid = user.getUid();
        userRepository.getUser(uid).addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                int currentBest = documentSnapshot.getLong("bestScore") != null ? documentSnapshot.getLong("bestScore").intValue() : 0;
                String username = documentSnapshot.getString("username") != null ? documentSnapshot.getString("username") : "Joueur";
                String profileImageUrl = documentSnapshot.getString("profileImageUrl") != null ? documentSnapshot.getString("profileImageUrl") : "";
                if (percentage > currentBest) {
                    userRepository.updateBestScore(uid, percentage, username, profileImageUrl, categoryName)
                            .addOnSuccessListener(aVoid -> tvBestScore.setText("🎉 Nouveau record : " + percentage + "%"));
                } else {
                    tvBestScore.setText("Meilleur score : " + currentBest + "%");
                }
            }
        });
    }
}
