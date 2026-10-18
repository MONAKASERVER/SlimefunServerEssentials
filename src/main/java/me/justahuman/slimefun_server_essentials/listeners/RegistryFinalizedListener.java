package me.justahuman.slimefun_server_essentials.listeners;

import io.github.thebusybiscuit.slimefun4.api.events.SlimefunItemRegistryFinalizedEvent;
import me.justahuman.slimefun_server_essentials.SlimefunServerEssentials;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RegistryFinalizedListener implements Listener {
    @EventHandler
    public void onRegistryFinalized(SlimefunItemRegistryFinalizedEvent event) {
        SlimefunServerEssentials.getInstance().rebuildData(
                SlimefunServerEssentials.getInstance().getConfig().getBoolean("export-json-on-load", false));
    }
}
