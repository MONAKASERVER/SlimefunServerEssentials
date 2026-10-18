package me.justahuman.slimefun_server_essentials.api;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;

import java.util.List;

/** Adapter extension point for addon recipe registries that Slimefun's standard API cannot represent. */
public interface RecipeTypeAdapter {
    boolean supports(SlimefunAddon addon);

    List<RecipeCategoryBuilder> export(SlimefunAddon addon);
}
