package me.justahuman.slimefun_server_essentials.channels;

import me.justahuman.slimefun_server_essentials.SlimefunServerEssentials;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public final class CacheStatusChannel extends AbstractChannel {
    public static final int COMPONENT_TYPES = 1;
    public static final int ITEMS = 1 << 1;
    public static final int RECIPE_CATEGORIES = 1 << 2;
    public static final int RECIPE_DISPLAYS = 1 << 3;

    @Override
    public String getChannel() {
        return "slimefun_server_essentials:cache_status";
    }

    @Override
    public void onMessageReceived(@Nonnull Player player, @Nonnull byte[] message) {
        if (message.length != 1 || !SlimefunServerEssentials.getInstance().getConfig()
                .getBoolean("cache.enabled", true)) {
            return;
        }
        int mask = Byte.toUnsignedInt(message[0]);
        if ((mask & COMPONENT_TYPES) != 0) {
            SlimefunServerEssentials.getComponentTypesChannel().skipNext(player);
        }
        if ((mask & ITEMS) != 0) {
            SlimefunServerEssentials.getItemsChannel().skipNext(player);
        }
        if ((mask & RECIPE_CATEGORIES) != 0) {
            SlimefunServerEssentials.getRecipeCategoriesChannel().skipNext(player);
        }
        if ((mask & RECIPE_DISPLAYS) != 0) {
            SlimefunServerEssentials.getRecipeDisplaysChannel().skipNext(player);
        }
    }
}
