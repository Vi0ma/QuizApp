package com.example.projet_quizz_android.repository;

import com.example.projet_quizz_android.models.QuizMetadata;
import com.example.projet_quizz_android.models.QuizReview;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class QuizRepository {
    private static QuizRepository instance;
    private final FirebaseFirestore db;
    private final CollectionReference quizzesRef;

    private QuizRepository() {
        db = FirebaseFirestore.getInstance();
        quizzesRef = db.collection("quizzes");
    }

    public static synchronized QuizRepository getInstance() {
        if (instance == null) {
            instance = new QuizRepository();
        }
        return instance;
    }

    public Task<Void> submitReview(String categoryName, int categoryId, String userId, String username, float newRating, String comment) {
        DocumentReference quizDocRef = quizzesRef.document(categoryName);
        DocumentReference reviewDocRef = quizDocRef.collection("reviews").document(userId);

        return db.runTransaction(transaction -> {
            QuizMetadata metadata = transaction.get(quizDocRef).toObject(QuizMetadata.class);
            
            if (metadata == null) {
                metadata = new QuizMetadata(categoryId, categoryName);
            }

            QuizReview oldReview = transaction.get(reviewDocRef).toObject(QuizReview.class);

            double currentTotalScore = metadata.getAverageRating() * metadata.getTotalVotes();
            int newTotalVotes = metadata.getTotalVotes();
            double newTotalScore = currentTotalScore;

            if (oldReview != null) {
                newTotalScore = newTotalScore - oldReview.getRating() + newRating;
            } else {
                newTotalScore += newRating;
                newTotalVotes++;
            }

            double newAverage = newTotalVotes == 0 ? 0 : (newTotalScore / newTotalVotes);

            metadata.setAverageRating(newAverage);
            metadata.setTotalVotes(newTotalVotes);
            
            updateBadges(metadata);

            QuizReview newReviewObj = new QuizReview(userId, username, newRating, comment);

            transaction.set(quizDocRef, metadata);
            transaction.set(reviewDocRef, newReviewObj);

            return null;
        });
    }

    private void updateBadges(QuizMetadata metadata) {
        if (metadata.getBadges() == null) metadata.setBadges(new ArrayList<>());
        metadata.getBadges().clear();

        if (metadata.getAverageRating() >= 4.5 && metadata.getTotalVotes() > 2) {
            metadata.getBadges().add("Top Rated 🏅");
        }
        if (metadata.getTotalVotes() >= 5) {
            metadata.getBadges().add("Trending 🔥");
        }
    }

    public Task<QuerySnapshot> getTopRatedQuizzes() {
        return quizzesRef.orderBy("averageRating", Query.Direction.DESCENDING).get();
    }

    public Task<QuerySnapshot> getMostPopularQuizzes() {
        return quizzesRef.orderBy("totalVotes", Query.Direction.DESCENDING).get();
    }
}
