package com.example.projet_quizz_android;

import com.example.projet_quizz_android.utils.NavigationUtils;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.projet_quizz_android.models.User;
import com.example.projet_quizz_android.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import de.hdodenhof.circleimageview.CircleImageView;

public class WelcomeActivity extends AppCompatActivity {

    TextView tvWelcomeMessage, tvUsername, tvEmail, tvBestScore, tvCategory, tvHeaderPageTitle;
    CircleImageView ivProfile, ivHeaderProfile;
    Button btnStartQuiz;
    TextView tvEditProfile;

    FirebaseAuth myAuth;
    UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        myAuth = FirebaseAuth.getInstance();
        userRepository = UserRepository.getInstance();

        tvWelcomeMessage = findViewById(R.id.tvWelcomeMessage);
        tvUsername = findViewById(R.id.tvWelcomeUsername);
        tvEmail = findViewById(R.id.tvWelcomeEmail);
        tvBestScore = findViewById(R.id.tvWelcomeBestScore);
        tvCategory = findViewById(R.id.tvWelcomeCategory);
        ivProfile = findViewById(R.id.ivWelcomeProfile);
        btnStartQuiz = findViewById(R.id.btnStartQuiz);
        tvEditProfile = findViewById(R.id.tvEditProfileLink);
        ivHeaderProfile = findViewById(R.id.ivHeaderProfile);
        tvHeaderPageTitle = findViewById(R.id.tvHeaderThemeName);

        if (tvHeaderPageTitle != null) {
            tvHeaderPageTitle.setText("Bienvenue");
        }

        loadUserData();

        btnStartQuiz.setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, CategoryChoiceActivity.class));
        });

        tvEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, ProfileActivity.class));
        });

        if (ivHeaderProfile != null) {
            ivHeaderProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        }

        NavigationUtils.setupBottomNavigation(this, 0);
    }

    private void loadUserData() {
        FirebaseUser currentUser = myAuth.getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }

        userRepository.listenToUser(currentUser.getUid(), (documentSnapshot, error) -> {
            if (documentSnapshot != null && documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    displayUserData(user);
                }
            }
        });
    }

    private void displayUserData(User user) {
        tvWelcomeMessage.setText("Bienvenue, " + user.getUsername() + " !");
        tvUsername.setText(user.getUsername());
        tvEmail.setText(user.getEmail());
        tvBestScore.setText("🏆 Meilleur score : " + user.getBestScore() + "%");
        tvCategory.setText("⭐ Thème préféré : " + user.getFavoriteCategory());

        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Glide.with(this).load(user.getProfileImageUrl()).circleCrop().placeholder(R.drawable.quiz_logo).into(ivProfile);
            if (ivHeaderProfile != null) {
                Glide.with(this).load(user.getProfileImageUrl()).circleCrop().into(ivHeaderProfile);
            }
        }
    }
}
