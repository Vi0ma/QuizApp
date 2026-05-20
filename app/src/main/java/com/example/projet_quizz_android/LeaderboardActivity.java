package com.example.projet_quizz_android;

import com.example.projet_quizz_android.utils.NavigationUtils;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projet_quizz_android.api.RetrofitClient;
import com.example.projet_quizz_android.models.LeaderboardEntry;
import com.example.projet_quizz_android.models.User;
import com.example.projet_quizz_android.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeaderboardActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ProgressBar progressBarLeaderboard;
    TextView tvLeaderboardEmpty, tvHeaderPageTitle;

    LeaderboardAdapter adapter;
    List<LeaderboardEntry> entries = new ArrayList<>();
    
    CircleImageView ivHeaderProfile;
    FirebaseAuth myAuth;
    UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        myAuth = FirebaseAuth.getInstance();
        userRepository = UserRepository.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView             = findViewById(R.id.recyclerViewLeaderboard);
        progressBarLeaderboard   = findViewById(R.id.progressBarLeaderboard);
        tvLeaderboardEmpty       = findViewById(R.id.tvLeaderboardEmpty);
        ivHeaderProfile          = findViewById(R.id.ivHeaderProfile);
        tvHeaderPageTitle        = findViewById(R.id.tvHeaderThemeName);

        if (tvHeaderPageTitle != null) {
            tvHeaderPageTitle.setText("Classement");
        }

        adapter = new LeaderboardAdapter(entries);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        setupHeader();
        loadLeaderboardFromApi();

        NavigationUtils.setupBottomNavigation(this, R.id.nav_leaderboard);
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

    private void loadLeaderboardFromApi() {
        progressBarLeaderboard.setVisibility(View.VISIBLE);

        RetrofitClient.getApiService().getScores().enqueue(new Callback<List<LeaderboardEntry>>() {
            @Override
            public void onResponse(Call<List<LeaderboardEntry>> call, Response<List<LeaderboardEntry>> response) {
                progressBarLeaderboard.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    entries.clear();
                    entries.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    tvLeaderboardEmpty.setVisibility(entries.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    Toast.makeText(LeaderboardActivity.this, "Erreur API : " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<LeaderboardEntry>> call, Throwable t) {
                progressBarLeaderboard.setVisibility(View.GONE);
                Toast.makeText(LeaderboardActivity.this, "Échec connexion au serveur local", Toast.LENGTH_SHORT).show();
            }
        });
    }

    static class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

        private final List<LeaderboardEntry> entries;

        LeaderboardAdapter(List<LeaderboardEntry> entries) {
            this.entries = entries;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_leaderboard, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LeaderboardEntry entry = entries.get(position);
            int rank = position + 1;
            String rankDisplay = rank == 1 ? "🥇" : rank == 2 ? "🥈" : rank == 3 ? "🥉" : rank + ".";

            holder.tvRank.setText(rankDisplay);
            holder.tvUsername.setText(entry.getUsername());
            holder.tvScore.setText(entry.getBestScore() + "%");

            if (entry.getProfileImageUrl() != null && !entry.getProfileImageUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(entry.getProfileImageUrl())
                        .circleCrop()
                        .placeholder(R.drawable.quiz_logo)
                        .into(holder.ivAvatar);
            } else {
                holder.ivAvatar.setImageResource(R.drawable.quiz_logo);
            }
        }

        @Override
        public int getItemCount() { return entries.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            CircleImageView ivAvatar;
            TextView tvRank, tvUsername, tvScore;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivAvatar   = itemView.findViewById(R.id.ivLeaderboardAvatar);
                tvRank     = itemView.findViewById(R.id.tvLeaderboardRank);
                tvUsername = itemView.findViewById(R.id.tvLeaderboardUsername);
                tvScore    = itemView.findViewById(R.id.tvLeaderboardScore);
            }
        }
    }
}
