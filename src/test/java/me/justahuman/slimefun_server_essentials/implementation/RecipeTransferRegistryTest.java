package me.justahuman.slimefun_server_essentials.implementation;

import me.justahuman.slimefun_server_essentials.api.RecipeBuilder;
import me.justahuman.slimefun_server_essentials.api.RecipeCategoryBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class RecipeTransferRegistryTest {
    @AfterEach
    void clearRegistry() {
        RecipeTransferRegistry.clear();
    }

    @Test
    void mergesSharedCategoriesInPacketOrder() {
        RecipeBuilder first = new RecipeBuilder().input("stone:1");
        RecipeBuilder second = new RecipeBuilder().input("dirt:1");
        RecipeCategoryBuilder firstCategory = new RecipeCategoryBuilder("enhanced_crafting_table");
        RecipeCategoryBuilder secondCategory = new RecipeCategoryBuilder("enhanced_crafting_table");
        firstCategory.recipe(first);
        secondCategory.recipe(second);

        RecipeTransferRegistry.register(List.of(firstCategory));
        RecipeTransferRegistry.register(List.of(secondCategory));

        assertSame(first, RecipeTransferRegistry.get("enhanced_crafting_table", 0));
        assertSame(second, RecipeTransferRegistry.get("enhanced_crafting_table", 1));
        assertNull(RecipeTransferRegistry.get("enhanced_crafting_table", 2));
    }
}
