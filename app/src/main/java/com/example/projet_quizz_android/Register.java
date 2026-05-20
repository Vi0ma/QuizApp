package com.example.projet_quizz_android;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.projet_quizz_android.api.RetrofitClient;
import com.example.projet_quizz_android.models.Category;
import com.example.projet_quizz_android.models.User;
import com.example.projet_quizz_android.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Register extends AppCompatActivity {

    Button btnRegister;
    EditText FirstName, Email, Password, ConfirmPassword;
    Spinner spinnerCategory;
    TextView tvBack;
    ProgressBar progressBarRegister;
    FirebaseAuth myAuth;
    UserRepository userRepository;

    List<String> categoryNames = new ArrayList<>();
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        myAuth = FirebaseAuth.getInstance();
        userRepository = UserRepository.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        loadCategories();

        btnRegister.setOnClickListener(v -> {
            String firstname        = FirstName.getText().toString().trim();
            String mail             = Email.getText().toString().trim();
            String password         = Password.getText().toString().trim();
            String confirmPassword  = ConfirmPassword.getText().toString().trim();
            String selectedCategory = spinnerCategory.getSelectedItem() != null ? spinnerCategory.getSelectedItem().toString() : "Général";

            if (TextUtils.isEmpty(firstname)) {
                Toast.makeText(this, "Remplir le champ prénom", Toast.LENGTH_SHORT).show(); return;
            }
            if (TextUtils.isEmpty(mail)) {
                Toast.makeText(this, "Remplir le champ email", Toast.LENGTH_SHORT).show(); return;
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Remplir le champ mot de passe", Toast.LENGTH_SHORT).show(); return;
            }
            if (password.length() < 6) {
                Toast.makeText(this, "Mot de passe : 6 caractères minimum", Toast.LENGTH_SHORT).show(); return;
            }
            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show(); return;
            }

            progressBarRegister.setVisibility(View.VISIBLE);
            btnRegister.setEnabled(false);
            signup(firstname, mail, password, selectedCategory);
        });

        tvBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        FirstName        = findViewById(R.id.firstname_register);
        Email            = findViewById(R.id.email_register);
        Password         = findViewById(R.id.password_register);
        ConfirmPassword  = findViewById(R.id.confirm_password);
        spinnerCategory  = findViewById(R.id.spinner_category_register);
        btnRegister      = findViewById(R.id.btnregister);
        tvBack           = findViewById(R.id.back_to_login);
        progressBarRegister = findViewById(R.id.progressBarRegister);

        categoryNames.add("Général");
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void loadCategories() {
        RetrofitClient.getApiService().getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoryNames.clear();
                    for (Category cat : response.body()) {
                        categoryNames.add(cat.getName());
                    }
                    if (categoryNames.isEmpty()) categoryNames.add("Général");
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(Register.this, "Erreur de chargement des catégories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signup(String username, String email, String password, String favoriteCategory) {
        myAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = myAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            User user = new User(firebaseUser.getUid(), username, email);
                            user.setFavoriteCategory(favoriteCategory);

                            userRepository.createUser(user)
                                    .addOnSuccessListener(aVoid -> {
                                        progressBarRegister.setVisibility(View.GONE);
                                        Toast.makeText(Register.this, "Inscription réussie !", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(Register.this, MainActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        progressBarRegister.setVisibility(View.GONE);
                                        Toast.makeText(Register.this, "Compte créé mais profil non sauvegardé", Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(Register.this, MainActivity.class));
                                        finish();
                                    });
                        }
                    } else {
                        progressBarRegister.setVisibility(View.GONE);
                        btnRegister.setEnabled(true);
                        Toast.makeText(Register.this, "Erreur : " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
