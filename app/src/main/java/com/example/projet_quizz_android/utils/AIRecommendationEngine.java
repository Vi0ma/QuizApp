package com.example.projet_quizz_android.utils;

import com.example.projet_quizz_android.models.Category;
import com.example.projet_quizz_android.models.Recommendation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AIRecommendationEngine {

    public static Recommendation generateRecommendation(int bestScore, String favoriteCategory, List<Category> availableCategories) {
        if (availableCategories == null || availableCategories.isEmpty()) {
            return null;
        }

        Category favCatObj = findCategoryByName(favoriteCategory, availableCategories);
        if (favCatObj == null) {
            favCatObj = availableCategories.get(0);
            favoriteCategory = favCatObj.getName();
        }

        String reason = "";
        Category targetCategory = null;

        if (bestScore < 50) {
            targetCategory = favCatObj;
            reason = "Votre score de " + bestScore + "% indique que vous pouvez encore progresser en " + favoriteCategory + ". Répétez pour vous améliorer !";
        } 
        else if (bestScore >= 80) {
            int currentIndex = availableCategories.indexOf(favCatObj);
            int nextIndex = (currentIndex + 1) % availableCategories.size();
            targetCategory = availableCategories.get(nextIndex);
            
            if (targetCategory.getId() == favCatObj.getId()) {
                reason = "Vous êtes un expert en " + favoriteCategory + " ! Continuez à exceller !";
            } else {
                reason = "Félicitations pour vos " + bestScore + "% en " + favoriteCategory + " ! Vous êtes prêt pour un nouveau défi : " + targetCategory.getName() + " !";
            }
        } 
        else {
            int currentIndex = availableCategories.indexOf(favCatObj);
            int prevIndex = (currentIndex - 1 + availableCategories.size()) % availableCategories.size();
            
            if (prevIndex == currentIndex && availableCategories.size() > 1) {
                prevIndex = (currentIndex + 1) % availableCategories.size();
            }
            targetCategory = availableCategories.get(prevIndex);
            
            reason = "Bon travail en " + favoriteCategory + ". Pour élargir vos horizons, testez vos compétences en " + targetCategory.getName() + " !";
        }

        if (targetCategory != null) {
            return new Recommendation(targetCategory.getName(), targetCategory.getId(), reason);
        }

        return null;
    }

    private static Category findCategoryByName(String name, List<Category> categories) {
        if (name == null || name.isEmpty()) return null;
        for (Category c : categories) {
            if (c.getName().trim().equalsIgnoreCase(name.trim())) {
                return c;
            }
        }
        return null;
    }
}
