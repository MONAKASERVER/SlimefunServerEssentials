package me.justahuman.slimefun_server_essentials.implementation;

import me.justahuman.slimefun_server_essentials.api.RecipeBuilder;
import me.justahuman.slimefun_server_essentials.api.RecipeCategoryBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Immutable-per-rebuild server-side recipe snapshot used to validate client transfer requests. */
public final class RecipeTransferRegistry {
    private static final Map<String, List<RecipeBuilder>> RECIPES = new HashMap<>();

    private RecipeTransferRegistry() {}

    public static void clear() {
        RECIPES.clear();
    }

    public static void register(List<RecipeCategoryBuilder> categories) {
        for (RecipeCategoryBuilder category : categories) {
            RECIPES.merge(category.getId(), category.recipes(), (current, added) -> {
                java.util.ArrayList<RecipeBuilder> merged = new java.util.ArrayList<>(current);
                merged.addAll(added);
                return List.copyOf(merged);
            });
        }
    }

    public static RecipeBuilder get(String categoryId, int recipeIndex) {
        List<RecipeBuilder> recipes = RECIPES.get(categoryId);
        if (recipes == null || recipeIndex < 0 || recipeIndex >= recipes.size()) {
            return null;
        }
        return recipes.get(recipeIndex);
    }
}
