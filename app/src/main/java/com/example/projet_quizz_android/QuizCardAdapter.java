package com.example.projet_quizz_android;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet_quizz_android.models.QuizMetadata;

import java.util.List;

public class QuizCardAdapter extends RecyclerView.Adapter<QuizCardAdapter.ViewHolder> {
    private final List<QuizMetadata> quizzes;
    private final OnQuizClickListener listener;

    public interface OnQuizClickListener {
        void onQuizClick(QuizMetadata quiz);
    }

    public QuizCardAdapter(List<QuizMetadata> quizzes, OnQuizClickListener listener) {
        this.quizzes = quizzes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuizMetadata quiz = quizzes.get(position);
        holder.tvQuizName.setText(quiz.getName());
        holder.ratingBar.setRating((float) quiz.getAverageRating());
        holder.tvRatingText.setText(String.format("(%d)", quiz.getTotalVotes()));

        if (quiz.getBadges() != null && !quiz.getBadges().isEmpty()) {
            holder.tvBadge.setVisibility(View.VISIBLE);
            holder.tvBadge.setText(quiz.getBadges().get(0));
        } else {
            holder.tvBadge.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onQuizClick(quiz));
    }

    @Override
    public int getItemCount() { return quizzes.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuizName, tvRatingText, tvBadge;
        RatingBar ratingBar;

        ViewHolder(View view) {
            super(view);
            tvQuizName = view.findViewById(R.id.tvQuizName);
            ratingBar = view.findViewById(R.id.ratingBarSmall);
            tvRatingText = view.findViewById(R.id.tvRatingText);
            tvBadge = view.findViewById(R.id.tvBadge);
        }
    }
}
