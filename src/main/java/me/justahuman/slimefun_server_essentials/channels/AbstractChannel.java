package me.justahuman.slimefun_server_essentials.channels;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.justahuman.slimefun_server_essentials.SlimefunServerEssentials;
import me.justahuman.slimefun_server_essentials.protocol.TransferEnvelope;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class AbstractChannel implements PluginMessageListener, Listener {
    protected final List<byte[]> messages = new ArrayList<>();
    protected final List<String> messageHashes = new ArrayList<>();
    protected long rawBytes;
    protected long encodedBytes;
    protected final Set<UUID> players = new HashSet<>();
    protected final Map<UUID, Runnable> onRegisterChannel = new HashMap<>();
    protected final Set<UUID> skipNextTransfer = new HashSet<>();

    protected AbstractChannel() {
        final Plugin plugin = SlimefunServerEssentials.getInstance();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, getChannel(), this);
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, getChannel());
    }

    protected String format(String string) {
        return string.toLowerCase(Locale.ROOT).replace(" ", "_");
    }

    public abstract String getChannel();
    public void load() {}
    public void clear() {
        messages.clear();
        messageHashes.clear();
        rawBytes = 0;
        encodedBytes = 0;
    }

    public int packetCount() {
        return messages.size();
    }

    public long rawBytes() {
        return rawBytes;
    }

    public long encodedBytes() {
        return encodedBytes;
    }

    public String contentHash() {
        return messageHashes.stream().collect(Collectors.joining(":"));
    }
    public void scheduleMessages(@Nonnull Player player) {
        onRegisterChannel.put(player.getUniqueId(), () -> startRateLimitedSend(player));

        if (players.contains(player.getUniqueId())) {
            onRegisterChannel.remove(player.getUniqueId()).run();
        }
    }
    public void onMessageReceived(@Nonnull Player player, @Nonnull byte[] message) {}

    public void sendMessage(@Nonnull Player player, @Nonnull String message) {
        final ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();
        dataOutput.writeUTF(message);
        sendMessage(player, dataOutput.toByteArray());
    }

    public void sendMessage(@Nonnull Player player, @Nonnull ByteArrayDataOutput message) {
        sendMessage(player, message.toByteArray());
    }

    public void sendMessage(@Nonnull Player player, @Nonnull byte[] message) {
        player.sendPluginMessage(SlimefunServerEssentials.getInstance(), getChannel(), message);
    }

    @EventHandler
    public void onPlayerRegisterChannel(PlayerRegisterChannelEvent event) {
        if (!event.getChannel().equals(getChannel())) {
            return;
        }

        players.add(event.getPlayer().getUniqueId());
        Runnable runnable = onRegisterChannel.remove(event.getPlayer().getUniqueId());
        if (runnable != null) {
            runnable.run();
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals(getChannel())) {
            return;
        }

        onMessageReceived(player, message);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        players.remove(playerId);
        onRegisterChannel.remove(playerId);
        skipNextTransfer.remove(playerId);
    }

    public List<byte[]> splitMessage(byte[] data) {
        SlimefunServerEssentials plugin = SlimefunServerEssentials.getInstance();
        int maximum = plugin.getConfig().getInt("sync.max-uncompressed-bytes", 67_108_864);
        boolean gzip = plugin.getConfig().getBoolean("sync.compression", true);
        TransferEnvelope.EncodedMessage encoded = TransferEnvelope.encode(data, gzip, maximum);
        rawBytes += encoded.rawBytes();
        encodedBytes += encoded.encodedBytes();
        messageHashes.add(encoded.sha256());
        return encoded.packets();
    }

    private void startRateLimitedSend(Player player) {
        if (skipNextTransfer.remove(player.getUniqueId())) {
            return;
        }
        if (messages.isEmpty()) {
            return;
        }
        int packetsPerTick = Math.max(1, SlimefunServerEssentials.getInstance().getConfig()
                .getInt("sync.packets-per-tick", 4));
        List<byte[]> snapshot = List.copyOf(messages);
        final int[] cursor = {0};
        SlimefunServerEssentials.getInstance().getServer().getScheduler().runTaskTimer(
                SlimefunServerEssentials.getInstance(), task -> {
                    if (skipNextTransfer.remove(player.getUniqueId())) {
                        task.cancel();
                        return;
                    }
                    if (!player.isOnline() || !players.contains(player.getUniqueId())) {
                        task.cancel();
                        return;
                    }
                    for (int sent = 0; sent < packetsPerTick && cursor[0] < snapshot.size(); sent++) {
                        sendMessage(player, snapshot.get(cursor[0]++));
                    }
                    if (cursor[0] >= snapshot.size()) {
                        task.cancel();
                    }
                }, 1L, 1L);
    }

    public void skipNext(Player player) {
        skipNextTransfer.add(player.getUniqueId());
    }
}
