package com.example.projet_quizz_android;

import com.example.projet_quizz_android.utils.NavigationUtils;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projet_quizz_android.api.RetrofitClient;
import com.example.projet_quizz_android.models.Category;
import com.example.projet_quizz_android.models.DailyChallenge;
import com.example.projet_quizz_android.models.DailyScore;
import com.example.projet_quizz_android.models.QuizMetadata;
import com.example.projet_quizz_android.models.User;
import com.example.projet_quizz_android.repository.DailyRepository;
import com.example.projet_quizz_android.repository.QuizRepository;
import com.example.projet_quizz_android.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DailyLeaderboardActivity extends AppCompatActivity {

    RecyclerView recyclerViewLeaderboard, rvTopRated, rvTrending;
    ProgressBar progressBarLeaderboard;
    TextView tvLeaderboardEmpty, tvWinnerName, tvWinnerScore, tvHeaderPageTitle;
    CardView cardDailyWinner, cardDailyChallenge;
    TextView tvDailyDesc;
    Button btnBackToMenu, btnDailyChallenge;

    CircleImageView ivHeaderProfile;
    FirebaseAuth myAuth;
    UserRepository userRepository;
    DailyRepository dailyRepository;
    QuizRepository quizRepository;
    
    DailyLeaderboardAdapter adapter;
    QuizCardAdapter adapterTopRated, adapterTrending;
    
    List<DailyScore> leaderboardList = new ArrayList<>();
    List<QuizMetadata> topRatedList = new ArrayList<>();
    List<QuizMetadata> trendingList = new ArrayList<>();
    
    DailyChallenge todayChallenge = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_leaderboard);

        myAuth = FirebaseAuth.getInstance();
        userRepository = UserRepository.getInstance();
        dailyRepository = DailyRepository.getInstance();
        quizRepository = QuizRepository.getInstance();

        initViews();
        setupHeader();
        loadDailyLeaderboard();
        loadCategoriesAndSetupChallenge();
        loadFirestoreQuizzes();

        if (tvHeaderPageTitle != null) {
            tvHeaderPageTitle.setText("Défis");
        }

        btnBackToMenu.setOnClickListener(v -> {
            Intent intent = new Intent(this, CategoryChoiceActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        NavigationUtils.setupBottomNavigation(this, R.id.nav_daily_leaderboard);
    }

    private void initViews() {
        recyclerViewLeaderboard = findViewById(R.id.recyclerViewLeaderboard);
        rvTopRated = findViewById(R.id.rvTopRated);
        rvTrending = findViewById(R.id.rvTrending);
        progressBarLeaderboard = findViewById(R.id.progressBarLeaderboard);
        tvLeaderboardEmpty = findViewById(R.id.tvLeaderboardEmpty);
        tvWinnerName = findViewById(R.id.tvWinnerName);
        tvWinnerScore = findViewById(R.id.tvWinnerScore);
        cardDailyWinner = findViewById(R.id.cardDailyWinner);
        btnBackToMenu = findViewById(R.id.btnBackToMenu);
        ivHeaderProfile = findViewById(R.id.ivHeaderProfile);
        tvHeaderPageTitle = findViewById(R.id.tvHeaderThemeName);

        cardDailyChallenge = findViewById(R.id.cardDailyChallenge);
        tvDailyDesc = findViewById(R.id.tvDailyDesc);
        btnDailyChallenge = findViewById(R.id.btnDailyChallenge);

        recyclerViewLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DailyLeaderboardAdapter(leaderboardList);
        recyclerViewLeaderboard.setAdapter(adapter);

        rvTopRated.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvTrending.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        QuizCardAdapter.OnQuizClickListener quizClickListener = quiz -> {
            Intent intent = new Intent(this, DynamicQuizActivity.class);
            intent.putExtra("CATEGORY_ID", quiz.getId());
            intent.putExtra("CATEGORY_NAME", quiz.getName());
            startActivity(intent);
        };

        adapterTopRated = new QuizCardAdapter(topRatedList, quizClickListener);
        rvTopRated.setAdapter(adapterTopRated);

        adapterTrending = new QuizCardAdapter(trendingList, quizClickListener);
        rvTrending.setAdapter(adapterTrending);
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

    private void loadFirestoreQuizzes() {
        quizRepository.getTopRatedQuizzes().addOnSuccessListener(queryDocumentSnapshots -> {
            topRatedList.clear();
            topRatedList.addAll(queryDocumentSnapshots.toObjects(QuizMetadata.class));
            adapterTopRated.notifyDataSetChanged();
        });

        quizRepository.getMostPopularQuizzes().addOnSuccessListener(queryDocumentSnapshots -> {
            trendingList.clear();
            trendingList.addAll(queryDocumentSnapshots.toObjects(QuizMetadata.class));
            adapterTrending.notifyDataSetChanged();
        });
    }

    private void loadCategoriesAndSetupChallenge() {
        RetrofitClient.getApiService().getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    setupDailyChallenge(response.body());
                }
            }
            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {}
        });
    }

    private void setupDailyChallenge(List<Category> availableCategories) {
        if (availableCategories.isEmpty()) return;
        dailyRepository.getOrCreateTodayChallenge(availableCategories).addOnSuccessListener(challenge -> {
            todayChallenge = challenge;
            checkIfDailyPlayed();
        });
    }

    private void checkIfDailyPlayed() {
        FirebaseUser currentUser = myAuth.getCurrentUser();
        if (currentUser == null || todayChallenge == null) return;
        
        dailyRepository.checkHasPlayedToday(currentUser.getUid()).addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                tvDailyDesc.setText("Vous avez déjà relevé le défi aujourd'hui !");
                btnDailyChallenge.setText("Défi complété ✅");
                btnDailyChallenge.setEnabled(false);
                btnDailyChallenge.setAlpha(0.7f);
            } else {
                tvDailyDesc.setText("Thème du jour : " + todayChallenge.getCategoryName());
                btnDailyChallenge.setText("Jouer le défi →");
                btnDailyChallenge.setOnClickListener(v -> {
                    Intent intent = new Intent(this, DailyQuizActivity.class);
                    intent.putExtra("CATEGORY_ID", todayChallenge.getCategoryId());
                    intent.putExtra("CATEGORY_NAME", todayChallenge.getCategoryName());
                    startActivity(intent);
                });
            }
        });
    }

    private void loadDailyLeaderboard() {
        progressBarLeaderboard.setVisibility(View.VISIBLE);
        dailyRepository.getDailyLeaderboard().addOnSuccessListener(queryDocumentSnapshots -> {
            progressBarLeaderboard.setVisibility(View.GONE);
            leaderboardList.clear();
            if (queryDocumentSnapshots.isEmpty()) {
                tvLeaderboardEmpty.setVisibility(View.VISIBLE);
                cardDailyWinner.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
                return;
            }
            tvLeaderboardEmpty.setVisibility(View.GONE);
            List<DailyScore> tempList = new ArrayList<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                DailyScore score = doc.toObject(DailyScore.class);
                if (score != null) tempList.add(score);
            }
            tempList.sort((s1, s2) -> {
                if (s1.getScore() != s2.getScore()) return Integer.compare(s2.getScore(), s1.getScore());
                else return Long.compare(s2.getTimeRemaining(), s1.getTimeRemaining());
            });
            if (!tempList.isEmpty()) {
                DailyScore winner = tempList.get(0);
                cardDailyWinner.setVisibility(View.VISIBLE);
                tvWinnerName.setText(winner.getUsername());
                tvWinnerScore.setText("Score: " + winner.getScore() + "% (Temps: " + (winner.getTimeRemaining()/1000) + "s)");
                for (int i = 1; i < tempList.size(); i++) leaderboardList.add(tempList.get(i));
            }
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            progressBarLeaderboard.setVisibility(View.GONE);
        });
    }

    static class DailyLeaderboardAdapter extends RecyclerView.Adapter<DailyLeaderboardAdapter.ViewHolder> {
        private final List<DailyScore> entries;
        DailyLeaderboardAdapter(List<DailyScore> entries) { this.entries = entries; }
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard, parent, false);
            return new ViewHolder(view);
        }
        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DailyScore entry = entries.get(position);
            int rank = position + 2;
            String rankDisplay = rank == 2 ? "🥈" : rank == 3 ? "🥉" : rank + ".";
            holder.tvRank.setText(rankDisplay);
            holder.tvUsername.setText(entry.getUsername());
            holder.tvScore.setText(entry.getScore() + "% (" + (entry.getTimeRemaining() / 1000) + "s)");
            if (entry.getProfileImageUrl() != null && !entry.getProfileImageUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext()).load(entry.getProfileImageUrl()).circleCrop().placeholder(R.drawable.quiz_logo).into(holder.ivAvatar);
            } else holder.ivAvatar.setImageResource(R.drawable.quiz_logo);
        }
        @Override public int getItemCount() { return entries.size(); }
        static class ViewHolder extends RecyclerView.ViewHolder {
            CircleImageView ivAvatar;
            TextView tvRank, tvUsername, tvScore;
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivAvatar = itemView.findViewById(R.id.ivLeaderboardAvatar);
                tvRank = itemView.findViewById(R.id.tvLeaderboardRank);
                tvUsername = itemView.findViewById(R.id.tvLeaderboardUsername);
                tvScore = itemView.findViewById(R.id.tvLeaderboardScore);
            }
        }
    }
}
