package com.example.projet_quizz_android;

import com.example.projet_quizz_android.utils.NavigationUtils;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.projet_quizz_android.api.RetrofitClient;
import com.example.projet_quizz_android.models.Category;
import com.example.projet_quizz_android.models.User;
import com.example.projet_quizz_android.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    CircleImageView ivProfile, ivHeaderProfile;
    TextView tvEmail, tvUsername, tvBestScore, tvFavoriteCategory, tvHeaderPageTitle;
    EditText etEditUsername;
    Spinner spinnerFavoriteCategory;
    Button btnEditProfile, btnSaveProfile, btnDeleteAccount, btnLogout;
    ProgressBar progressBarProfile;

    FirebaseAuth myAuth;
    UserRepository userRepository;
    ListenerRegistration profileListener;

    String currentProfileImageBase64 = "";
    Uri selectedImageUri = null;
    Uri photoUri = null;

    List<String> categoryNames = new ArrayList<>();
    ArrayAdapter<String> spinnerAdapter;

    ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    Glide.with(this).load(selectedImageUri).circleCrop().into(ivProfile);
                }
            });

    ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success && photoUri != null) {
                    selectedImageUri = photoUri;
                    Glide.with(this).load(photoUri).circleCrop().into(ivProfile);
                    Toast.makeText(this, "Photo prise avec succès !", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        myAuth = FirebaseAuth.getInstance();
        userRepository = UserRepository.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupSpinner();
        setupClickListeners();
        loadProfileRealTime();
        loadCategoriesForSpinner();

        if (tvHeaderPageTitle != null) {
            tvHeaderPageTitle.setText("Profil");
        }

        NavigationUtils.setupBottomNavigation(this, R.id.nav_profile);
    }

    private void initViews() {
        ivProfile               = findViewById(R.id.ivProfilePicture);
        tvEmail                 = findViewById(R.id.tvProfileEmail);
        tvUsername              = findViewById(R.id.tvProfileUsername);
        tvBestScore             = findViewById(R.id.tvProfileBestScore);
        tvFavoriteCategory      = findViewById(R.id.tvProfileFavoriteCategory);
        etEditUsername          = findViewById(R.id.etEditUsername);
        spinnerFavoriteCategory = findViewById(R.id.spinnerFavoriteCategory);
        btnEditProfile          = findViewById(R.id.btnEditProfile);
        btnSaveProfile          = findViewById(R.id.btnSaveProfile);
        btnDeleteAccount        = findViewById(R.id.btnDeleteAccount);
        btnLogout               = findViewById(R.id.btnLogout);
        progressBarProfile      = findViewById(R.id.progressBarProfile);
        ivHeaderProfile         = findViewById(R.id.ivHeaderProfile);
        tvHeaderPageTitle       = findViewById(R.id.tvHeaderThemeName);
    }

    private void setupSpinner() {
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFavoriteCategory.setAdapter(spinnerAdapter);
    }

    private void loadCategoriesForSpinner() {
        RetrofitClient.getApiService().getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoryNames.clear();
                    for (Category c : response.body()) {
                        categoryNames.add(c.getName());
                    }
                    spinnerAdapter.notifyDataSetChanged();
                }
            }
            @Override public void onFailure(Call<List<Category>> call, Throwable t) {}
        });
    }

    private void setupClickListeners() {
        ivProfile.setOnClickListener(v -> showImageSourceDialog());

        btnEditProfile.setOnClickListener(v -> {
            etEditUsername.setVisibility(View.VISIBLE);
            spinnerFavoriteCategory.setVisibility(View.VISIBLE);
            btnSaveProfile.setVisibility(View.VISIBLE);
            btnEditProfile.setVisibility(View.GONE);
            etEditUsername.setText(tvUsername.getText().toString().replace("👤 ", ""));

            String currentFav = tvFavoriteCategory.getText().toString().trim();
            int spinnerPosition = spinnerAdapter.getPosition(currentFav);
            if (spinnerPosition >= 0) {
                spinnerFavoriteCategory.setSelection(spinnerPosition);
            }
        });

        btnSaveProfile.setOnClickListener(v -> {
            String newUsername = etEditUsername.getText().toString().trim();
            String newFavoriteCategory = spinnerFavoriteCategory.getSelectedItem() != null
                    ? spinnerFavoriteCategory.getSelectedItem().toString() : "Général";

            if (newUsername.isEmpty()) {
                Toast.makeText(this, "Le nom ne peut pas être vide", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBarProfile.setVisibility(View.VISIBLE);
            btnSaveProfile.setEnabled(false);

            if (selectedImageUri != null) {
                String base64Image = uriToBase64(selectedImageUri);
                saveProfileData(newUsername, newFavoriteCategory, base64Image);
            } else {
                saveProfileData(newUsername, newFavoriteCategory, currentProfileImageBase64);
            }
        });

        btnLogout.setOnClickListener(v -> {
            myAuth.signOut();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
    }

    private void showImageSourceDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Choisir une photo de profil")
                .setItems(
                        new String[]{"Prendre une photo", "Choisir depuis la galerie"},
                        (dialog, which) -> {
                            if (which == 0) {
                                verifierPermissionCamera();
                            } else {
                                Intent intent = new Intent(Intent.ACTION_PICK,
                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                imagePickerLauncher.launch(intent);
                            }
                        })
                .show();
    }

    private void verifierPermissionCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            lancerCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    100
            );
        }
    }

    private void lancerCamera() {
        File photoFile = new File(getCacheDir(), "profile_photo_temp.jpg");
        photoUri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".fileprovider",
                photoFile
        );
        cameraLauncher.launch(photoUri);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                lancerCamera();
            } else {
                Toast.makeText(this, "Permission caméra refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String uriToBase64(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, true);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
            byte[] byteArray = outputStream.toByteArray();
            return "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void loadProfileRealTime() {
        FirebaseUser user = myAuth.getCurrentUser();
        if (user == null) { finish(); return; }
        progressBarProfile.setVisibility(View.VISIBLE);
        profileListener = userRepository.listenToUser(user.getUid(), (documentSnapshot, error) -> {
            progressBarProfile.setVisibility(View.GONE);
            if (error != null) return;
            if (documentSnapshot != null && documentSnapshot.exists()) {
                User profile = documentSnapshot.toObject(User.class);
                if (profile != null) displayProfile(profile);
            }
        });
    }

    private void displayProfile(User user) {
        tvUsername.setText("👤 " + user.getUsername());
        tvEmail.setText("✉️ " + user.getEmail());
        tvBestScore.setText(user.getBestScore() + "%");
        tvFavoriteCategory.setText(user.getFavoriteCategory());
        currentProfileImageBase64 = user.getProfileImageUrl() != null ? user.getProfileImageUrl() : "";

        if (!currentProfileImageBase64.isEmpty()) {
            Glide.with(this).load(currentProfileImageBase64).circleCrop()
                    .placeholder(R.drawable.quiz_logo).into(ivProfile);
            if (ivHeaderProfile != null) {
                Glide.with(this).load(currentProfileImageBase64).circleCrop().into(ivHeaderProfile);
            }
        }
    }

    private void saveProfileData(String username, String favoriteCategory, String imageContent) {
        FirebaseUser user = myAuth.getCurrentUser();
        if (user == null) return;
        Map<String, Object> updates = new HashMap<>();
        updates.put("username", username);
        updates.put("favoriteCategory", favoriteCategory);
        updates.put("profileImageUrl", imageContent);
        userRepository.updateUser(user.getUid(), updates).addOnSuccessListener(aVoid -> {
            progressBarProfile.setVisibility(View.GONE);
            btnSaveProfile.setEnabled(true);
            selectedImageUri = null;
            photoUri = null;
            etEditUsername.setVisibility(View.GONE);
            spinnerFavoriteCategory.setVisibility(View.GONE);
            btnSaveProfile.setVisibility(View.GONE);
            btnEditProfile.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Profil mis à jour !", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            progressBarProfile.setVisibility(View.GONE);
            btnSaveProfile.setEnabled(true);
        });
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer le compte")
                .setMessage("Es-tu sûr(e) ?")
                .setPositiveButton("Supprimer", (dialog, which) -> deleteAccount())
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void deleteAccount() {
        FirebaseUser user = myAuth.getCurrentUser();
        if (user == null) return;
        userRepository.deleteUser(user.getUid()).addOnCompleteListener(task ->
                user.delete().addOnCompleteListener(authTask -> {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (profileListener != null) profileListener.remove();
    }
}
