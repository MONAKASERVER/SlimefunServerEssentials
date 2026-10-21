package me.justahuman.slimefun_server_essentials.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecipeCategoryBuilderTest {
    @Test
    void preservesGridDimensionsWhenRecipesAreMerged() {
        RecipeCategoryBuilder category = new RecipeCategoryBuilder("six_by_six");
        category.recipe(new RecipeBuilder()
                .input("stone:1")
                .output("diamond:1")
                .grid(6, 6));
        category.recipe(new RecipeBuilder()
                .input("dirt:1")
                .output("diamond:1")
                .grid(6, 6));

        RecipeBuilder merged = category.recipes().getFirst();
        assertEquals(1, category.recipes().size());
        assertEquals(6, merged.getWidth());
        assertEquals(6, merged.getHeight());
    }
}
