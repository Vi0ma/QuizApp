package com.example.projet_quizz_android.repository;

import com.example.projet_quizz_android.models.LeaderboardEntry;
import com.example.projet_quizz_android.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {

    private static UserRepository instance;
    private final FirebaseFirestore db;
    private final CollectionReference usersRef;
    private final CollectionReference leaderboardRef;

    public static UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    private UserRepository() {
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");
        leaderboardRef = db.collection("leaderboard");
    }

    public Task<Void> createUser(User user) {
        return usersRef.document(user.getUid()).set(user);
    }

    public Task<DocumentSnapshot> getUser(String uid) {
        return usersRef.document(uid).get();
    }

    public ListenerRegistration listenToUser(String uid, EventListener<DocumentSnapshot> listener) {
        return usersRef.document(uid).addSnapshotListener(listener);
    }

    public Task<Void> updateUser(String uid, Map<String, Object> updates) {
        return usersRef.document(uid).update(updates);
    }

    public Task<Void> updateBestScore(String uid, int newScore, String username, String profileImageUrl, String categoryName) {
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("bestScore", newScore);

        LeaderboardEntry entry = new LeaderboardEntry(username, profileImageUrl, newScore, categoryName);
        entry.setUid(uid);

        leaderboardRef.document(uid).set(entry);

        return usersRef.document(uid).update(userUpdates);
    }

    public Task<Void> deleteUser(String uid) {
        leaderboardRef.document(uid).delete();
        return usersRef.document(uid).delete();
    }

    public ListenerRegistration listenToLeaderboard(EventListener<QuerySnapshot> listener) {
        return leaderboardRef
                .orderBy("bestScore", Query.Direction.DESCENDING)
                .limit(10)
                .addSnapshotListener(listener);
    }
}
