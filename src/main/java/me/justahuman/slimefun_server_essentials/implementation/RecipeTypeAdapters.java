package me.justahuman.slimefun_server_essentials.implementation;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import me.justahuman.slimefun_server_essentials.SlimefunServerEssentials;
import me.justahuman.slimefun_server_essentials.api.RecipeCategoryBuilder;
import me.justahuman.slimefun_server_essentials.api.RecipeTypeAdapter;

import java.util.ArrayList;
import java.util.List;

public final class RecipeTypeAdapters {
    private static final List<RecipeTypeAdapter> ADAPTERS = new ArrayList<>();

    private RecipeTypeAdapters() {}

    public static void clear() {
        ADAPTERS.clear();
    }

    public static void register(RecipeTypeAdapter adapter) {
        ADAPTERS.add(adapter);
    }

    public static List<RecipeCategoryBuilder> export(SlimefunAddon addon) {
        List<RecipeCategoryBuilder> categories = new ArrayList<>();
        for (RecipeTypeAdapter adapter : ADAPTERS) {
            try {
                if (adapter.supports(addon)) {
                    categories.addAll(adapter.export(addon));
                }
            } catch (RuntimeException exception) {
                SlimefunServerEssentials.getInstance().getLogger().warning(
                        "Recipe adapter " + adapter.getClass().getSimpleName() + " failed for "
                                + addon.getName() + ": " + exception.getMessage());
            }
        }
        return categories;
    }
}
