package me.justahuman.slimefun_server_essentials.implementation.adapter;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import me.justahuman.slimefun_server_essentials.api.RecipeBuilder;
import me.justahuman.slimefun_server_essentials.api.RecipeCategoryBuilder;
import me.justahuman.slimefun_server_essentials.api.RecipeTypeAdapter;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/** Optional reflection bridge for MonaIndustry's 6x6 ExpertRecipeRegistry. */
public final class MonaIndustryExpertRecipeAdapter implements RecipeTypeAdapter {
    @Override
    public boolean supports(SlimefunAddon addon) {
        Class<?> pluginClass = addon.getJavaPlugin().getClass();
        return hasField(pluginClass, "expertRecipes");
    }

    @Override
    public List<RecipeCategoryBuilder> export(SlimefunAddon addon) {
        try {
            Object plugin = addon.getJavaPlugin();
            Field registryField = findField(plugin.getClass(), "expertRecipes");
            registryField.setAccessible(true);
            Object registry = registryField.get(plugin);
            Method recipesMethod = registry.getClass().getMethod("recipes");
            Collection<?> recipes = (Collection<?>) recipesMethod.invoke(registry);
            if (recipes.isEmpty()) {
                return List.of();
            }

            RecipeCategoryBuilder category = new RecipeCategoryBuilder("mona_industry_expert_crafting");
            category.displayGrid(6, 6);
            SlimefunItem crafter = SlimefunItem.getById("MONA_EXPERT_CRAFTER");
            if (crafter != null) {
                category.item(crafter.getItem());
            }

            for (Object recipe : recipes) {
                Method ingredientsMethod = recipe.getClass().getMethod("ingredients");
                Method outputMethod = recipe.getClass().getMethod("output");
                ItemStack[] ingredients = (ItemStack[]) ingredientsMethod.invoke(recipe);
                ItemStack output = (ItemStack) outputMethod.invoke(recipe);
                if (ingredients.length != 36 || output == null || output.getType().isAir()) {
                    continue;
                }
                category.recipe(new RecipeBuilder()
                        .inputs(Arrays.asList(ingredients))
                        .output(output)
                        .grid(6, 6));
            }
            return category.isEmpty() ? List.of() : List.of(category);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not read MonaIndustry expert recipes", exception);
        }
    }

    private static boolean hasField(Class<?> type, String name) {
        try {
            findField(type, name);
            return true;
        } catch (NoSuchFieldException ignored) {
            return false;
        }
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                // Continue through plugin base classes.
            }
        }
        throw new NoSuchFieldException(name);
    }
}
