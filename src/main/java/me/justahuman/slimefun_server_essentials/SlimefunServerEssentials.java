package me.justahuman.slimefun_server_essentials;

import de.tr7zw.nbtapi.NBT;
import lombok.Getter;
import me.justahuman.slimefun_server_essentials.channels.registry.ItemsChannel;
import me.justahuman.slimefun_server_essentials.channels.LoadingStateChannel;
import me.justahuman.slimefun_server_essentials.channels.registry.RecipeCategoriesChannel;
import me.justahuman.slimefun_server_essentials.channels.registry.RecipeDisplaysChannel;
import me.justahuman.slimefun_server_essentials.channels.registry.ComponentTypesChannel;
import me.justahuman.slimefun_server_essentials.channels.CacheStatusChannel;
import me.justahuman.slimefun_server_essentials.command.MonaJeiCommand;
import me.justahuman.slimefun_server_essentials.api.event.SlimefunEssentialsRegisterEvent;
import me.justahuman.slimefun_server_essentials.implementation.DisplayComponentTypes;
import me.justahuman.slimefun_server_essentials.implementation.RecipeCategoryExporters;
import me.justahuman.slimefun_server_essentials.implementation.RecipeDisplays;
import me.justahuman.slimefun_server_essentials.implementation.RecipeTypeAdapters;
import me.justahuman.slimefun_server_essentials.listeners.RegisterDefaultsListener;
import me.justahuman.slimefun_server_essentials.listeners.RegistryFinalizedListener;
import me.justahuman.slimefun_server_essentials.util.JsonUtils;
import me.justahuman.slimefun_server_essentials.util.Utils;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;

public final class SlimefunServerEssentials extends JavaPlugin {
    private static @Getter SlimefunServerEssentials instance;

    private static @Getter LoadingStateChannel loadingStateChannel;

    private static @Getter ItemsChannel itemsChannel;
    private static @Getter RecipeCategoriesChannel recipeCategoriesChannel;
    private static @Getter RecipeDisplaysChannel recipeDisplaysChannel;
    private static @Getter ComponentTypesChannel componentTypesChannel;
    private static @Getter CacheStatusChannel cacheStatusChannel;
    private volatile boolean dataReady;
    private volatile long lastBuildMillis;
    private volatile long lastBuildDurationMillis;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (getConfig().getString("server-id", "").isBlank()) {
            getConfig().set("server-id", UUID.randomUUID().toString());
            saveConfig();
        }
        if (!NBT.preloadApi()) {
            getLogger().warning("NBT-API wasn't initialized properly, disabling the plugin");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        instance = this;

        loadingStateChannel = new LoadingStateChannel();
        cacheStatusChannel = new CacheStatusChannel();

        itemsChannel = new ItemsChannel();
        recipeDisplaysChannel = new RecipeDisplaysChannel();
        recipeCategoriesChannel = new RecipeCategoriesChannel();
        componentTypesChannel = new ComponentTypesChannel();

        new Metrics(instance, 18206);

        getServer().getPluginManager().registerEvents(new RegistryFinalizedListener(), this);
        getServer().getPluginManager().registerEvents(new RegisterDefaultsListener(), this);
        MonaJeiCommand command = new MonaJeiCommand(this);
        if (getCommand("monajei") != null) {
            getCommand("monajei").setExecutor(command);
            getCommand("monajei").setTabCompleter(command);
        }
    }

    @Override
    public void onDisable() {
        dataReady = false;
    }

    public void rebuildData(boolean exportJson) {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Slimefun registry snapshots must be collected on the server thread");
        }
        long started = System.currentTimeMillis();
        dataReady = false;
        List.of(componentTypesChannel, itemsChannel, recipeCategoriesChannel, recipeDisplaysChannel)
                .forEach(channel -> channel.clear());

        JsonUtils.setExportEnabled(exportJson);
        try {
            Utils.load();
            if (exportJson) {
                JsonUtils.deleteGenerated();
            }
            DisplayComponentTypes.clear();
            RecipeCategoryExporters.clear();
            RecipeDisplays.clear();
            RecipeTypeAdapters.clear();
            Bukkit.getPluginManager().callEvent(new SlimefunEssentialsRegisterEvent());
            componentTypesChannel.load();
            itemsChannel.load();
            recipeCategoriesChannel.load();
            recipeDisplaysChannel.load();
            dataReady = true;
            lastBuildMillis = System.currentTimeMillis();
            lastBuildDurationMillis = lastBuildMillis - started;
            getLogger().info("Built Mona JEI sync cache in " + lastBuildDurationMillis + " ms ("
                    + totalPackets() + " packets, " + totalRawBytes() + " raw bytes, "
                    + totalEncodedBytes() + " transferred bytes)");
        } finally {
            JsonUtils.setExportEnabled(false);
        }

        if (dataReady && getConfig().getBoolean("sync.automatic", true)) {
            Bukkit.getOnlinePlayers().forEach(loadingStateChannel::sync);
        }
    }

    public void sendStatus(CommandSender sender) {
        sender.sendMessage("§6Mona Slimefun Essentials §7- §f" + getPluginMeta().getVersion());
        sender.sendMessage("§7Ready: §f" + dataReady + " §7| build: §f" + lastBuildDurationMillis + " ms");
        sender.sendMessage("§7Packets: §f" + totalPackets() + " §7| raw: §f" + totalRawBytes()
                + " B §7| transfer: §f" + totalEncodedBytes() + " B");
        sender.sendMessage("§7Last build epoch: §f" + lastBuildMillis);
    }

    public boolean isDataReady() {
        return dataReady;
    }

    private int totalPackets() {
        return componentTypesChannel.packetCount() + itemsChannel.packetCount()
                + recipeCategoriesChannel.packetCount() + recipeDisplaysChannel.packetCount();
    }

    private long totalRawBytes() {
        return componentTypesChannel.rawBytes() + itemsChannel.rawBytes()
                + recipeCategoriesChannel.rawBytes() + recipeDisplaysChannel.rawBytes();
    }

    private long totalEncodedBytes() {
        return componentTypesChannel.encodedBytes() + itemsChannel.encodedBytes()
                + recipeCategoriesChannel.encodedBytes() + recipeDisplaysChannel.encodedBytes();
    }
}
