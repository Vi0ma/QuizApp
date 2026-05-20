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
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projet_quizz_android.api.RetrofitClient;
import com.example.projet_quizz_android.models.Category;
import com.example.projet_quizz_android.models.Recommendation;
import com.example.projet_quizz_android.models.User;
import com.example.projet_quizz_android.repository.UserRepository;
import com.example.projet_quizz_android.utils.AIRecommendationEngine;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryChoiceActivity extends AppCompatActivity {

    RecyclerView rvCategories;
    ProgressBar progressBar;
    CategoryAdapter adapterAll;
    List<Category> categories = new ArrayList<>();
    List<Category> fullCategoriesList = new ArrayList<>();
    
    CircleImageView ivHeaderProfile;
    TextView tvHeaderPageTitle;
    FirebaseAuth myAuth;
    UserRepository userRepository;
    
    User currentUserModel;
    
    CardView cardAIRecommendation;
    TextView tvAIReason;
    Button btnLaunchRecommendation;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_choice);

        myAuth = FirebaseAuth.getInstance();
        userRepository = UserRepository.getInstance();

        rvCategories = findViewById(R.id.rvCategories);
        progressBar = findViewById(R.id.progressBarCategories);
        ivHeaderProfile = findViewById(R.id.ivHeaderProfile);
        tvHeaderPageTitle = findViewById(R.id.tvHeaderThemeName);
        searchView = findViewById(R.id.searchViewCategories);
        
        cardAIRecommendation = findViewById(R.id.cardAIRecommendation);
        tvAIReason = findViewById(R.id.tvAIReason);
        btnLaunchRecommendation = findViewById(R.id.btnLaunchRecommendation);

        if (tvHeaderPageTitle != null) {
            tvHeaderPageTitle.setText("Accueil");
        }

        rvCategories.setLayoutManager(new GridLayoutManager(this, 2));

        adapterAll = new CategoryAdapter(categories, category -> {
            Intent intent = new Intent(CategoryChoiceActivity.this, DynamicQuizActivity.class);
            intent.putExtra("CATEGORY_ID", category.getId());
            intent.putExtra("CATEGORY_NAME", category.getName());
            startActivity(intent);
        });
        rvCategories.setAdapter(adapterAll);

        setupHeader();
        loadCategories();
        setupSearch();

        NavigationUtils.setupBottomNavigation(this, R.id.nav_home);
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterCategories(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterCategories(newText);
                return true;
            }
        });
    }

    private void filterCategories(String query) {
        List<Category> filteredList = new ArrayList<>();
        for (Category category : fullCategoriesList) {
            if (category.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(category);
            }
        }
        categories.clear();
        categories.addAll(filteredList);
        adapterAll.notifyDataSetChanged();
    }

    private void setupHeader() {
        FirebaseUser currentUser = myAuth.getCurrentUser();
        if (currentUser != null) {
            userRepository.listenToUser(currentUser.getUid(), (documentSnapshot, error) -> {
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        currentUserModel = user;
                        checkAndDisplayRecommendation();
                        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                            Glide.with(this).load(user.getProfileImageUrl()).circleCrop().placeholder(R.drawable.quiz_logo).into(ivHeaderProfile);
                        }
                    }
                }
            });
        }
        ivHeaderProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void loadCategories() {
        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService().getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    fullCategoriesList.clear();
                    fullCategoriesList.addAll(response.body());
                    categories.clear();
                    categories.addAll(fullCategoriesList);
                    adapterAll.notifyDataSetChanged();
                    checkAndDisplayRecommendation();
                }
            }
            @Override public void onFailure(Call<List<Category>> call, Throwable t) { progressBar.setVisibility(View.GONE); }
        });
    }

    private void checkAndDisplayRecommendation() {
        if (currentUserModel != null && !fullCategoriesList.isEmpty()) {
            Recommendation recommendation = AIRecommendationEngine.generateRecommendation(currentUserModel.getBestScore(), currentUserModel.getFavoriteCategory(), fullCategoriesList);
            if (recommendation != null) {
                tvAIReason.setText(recommendation.getReason());
                cardAIRecommendation.setVisibility(View.VISIBLE);
                btnLaunchRecommendation.setOnClickListener(v -> {
                    Intent intent = new Intent(this, DynamicQuizActivity.class);
                    intent.putExtra("CATEGORY_ID", recommendation.getRecommendedCategoryId());
                    intent.putExtra("CATEGORY_NAME", recommendation.getRecommendedCategoryName());
                    startActivity(intent);
                });
            } else cardAIRecommendation.setVisibility(View.GONE);
        }
    }

    static class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
        private final List<Category> categories;
        private final OnCategoryClickListener listener;
        interface OnCategoryClickListener { void onCategoryClick(Category category); }
        CategoryAdapter(List<Category> categories, OnCategoryClickListener listener) { this.categories = categories; this.listener = listener; }
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_card, parent, false);
            return new ViewHolder(view);
        }
        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Category category = categories.get(position);
            holder.tvName.setText(category.getName());
            holder.tvEmoji.setText("❓");
            holder.itemView.setOnClickListener(v -> listener.onCategoryClick(category));
        }
        @Override public int getItemCount() { return categories.size(); }
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvEmoji;
            ViewHolder(View view) {
                super(view);
                tvName = view.findViewById(R.id.tvCategoryName);
                tvEmoji = view.findViewById(R.id.tvCategoryEmoji);
            }
        }
    }
}
