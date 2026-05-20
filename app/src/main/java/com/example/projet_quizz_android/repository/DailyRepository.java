package com.example.projet_quizz_android.repository;

import com.example.projet_quizz_android.models.Category;
import com.example.projet_quizz_android.models.DailyChallenge;
import com.example.projet_quizz_android.models.DailyScore;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class DailyRepository {

    private static DailyRepository instance;
    private final FirebaseFirestore db;
    private final CollectionReference challengesRef;
    private final CollectionReference scoresRef;

    private DailyRepository() {
        db = FirebaseFirestore.getInstance();
        challengesRef = db.collection("daily_challenges");
        scoresRef = db.collection("daily_scores");
    }

    public static synchronized DailyRepository getInstance() {
        if (instance == null) {
            instance = new DailyRepository();
        }
        return instance;
    }

    public static String getTodayDateString() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    public Task<DailyChallenge> getOrCreateTodayChallenge(List<Category> availableCategories) {
        String today = getTodayDateString();
        DocumentReference docRef = challengesRef.document(today);

        return docRef.get().continueWithTask(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                DailyChallenge challenge = task.getResult().toObject(DailyChallenge.class);
                return com.google.android.gms.tasks.Tasks.forResult(challenge);
            } else {
                Category randomCategory = availableCategories.get(new Random().nextInt(availableCategories.size()));
                DailyChallenge newChallenge = new DailyChallenge(today, randomCategory.getId(), randomCategory.getName());
                
                return docRef.set(newChallenge).continueWithTask(setTask -> {
                    if (setTask.isSuccessful()) {
                        return com.google.android.gms.tasks.Tasks.forResult(newChallenge);
                    } else {
                        throw setTask.getException();
                    }
                });
            }
        });
    }

    public Task<QuerySnapshot> checkHasPlayedToday(String userId) {
        String today = getTodayDateString();
        return scoresRef
                .whereEqualTo("date", today)
                .whereEqualTo("userId", userId)
                .limit(1)
                .get();
    }

    public Task<Void> saveDailyScore(String userId, String username, int score, long timeRemaining, String profileImageUrl) {
        String today = getTodayDateString();
        DailyScore dailyScore = new DailyScore(userId, username, today, score, timeRemaining, profileImageUrl);
        String docId = today + "_" + userId;
        return scoresRef.document(docId).set(dailyScore);
    }

    public Task<QuerySnapshot> getDailyLeaderboard() {
        String today = getTodayDateString();
        return scoresRef
                .whereEqualTo("date", today)
                .get();
    }
}
