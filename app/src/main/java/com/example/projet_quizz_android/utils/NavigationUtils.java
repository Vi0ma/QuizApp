package com.example.projet_quizz_android.utils;

import android.app.Activity;
import android.content.Intent;

import com.example.projet_quizz_android.CategoryChoiceActivity;
import com.example.projet_quizz_android.DailyLeaderboardActivity;
import com.example.projet_quizz_android.LeaderboardActivity;
import com.example.projet_quizz_android.ProfileActivity;
import com.example.projet_quizz_android.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationUtils {

    public static void setupBottomNavigation(Activity activity, int currentMenuId) {
        BottomNavigationView bottomNavigationView = activity.findViewById(R.id.bottom_navigation);
        if (bottomNavigationView == null) return;

        if (currentMenuId != 0) {
            bottomNavigationView.setSelectedItemId(currentMenuId);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == currentMenuId) {
                return true;
            }

            Intent intent = null;

            if (itemId == R.id.nav_home) {
                intent = new Intent(activity, CategoryChoiceActivity.class);
            } else if (itemId == R.id.nav_leaderboard) {
                intent = new Intent(activity, LeaderboardActivity.class);
            } else if (itemId == R.id.nav_daily_leaderboard) {
                intent = new Intent(activity, DailyLeaderboardActivity.class);
            } else if (itemId == R.id.nav_profile) {
                intent = new Intent(activity, ProfileActivity.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });
    }
}
