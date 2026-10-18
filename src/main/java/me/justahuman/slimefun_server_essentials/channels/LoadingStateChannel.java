package me.justahuman.slimefun_server_essentials.channels;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.justahuman.slimefun_server_essentials.SlimefunServerEssentials;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerRegisterChannelEvent;

public class LoadingStateChannel extends AbstractChannel {
    @EventHandler @Override
    public void onPlayerRegisterChannel(PlayerRegisterChannelEvent event) {
        if (!event.getChannel().equals(getChannel())) {
            return;
        }

        Player player = event.getPlayer();
        sync(player);
    }

    public void sync(Player player) {
        SlimefunServerEssentials plugin = SlimefunServerEssentials.getInstance();
        if (!plugin.getConfig().getBoolean("sync.enabled", true) || !plugin.isDataReady()) {
            if (plugin.getConfig().getBoolean("debug", false)) {
                plugin.getLogger().info("Skipped Mona JEI sync for " + player.getName()
                        + " (enabled=" + plugin.getConfig().getBoolean("sync.enabled", true)
                        + ", ready=" + plugin.isDataReady() + ")");
            }
            return;
        }
        ByteArrayDataOutput loadingStatePacket = ByteStreams.newDataOutput();
        loadingStatePacket.writeInt(SlimefunServerEssentials.getComponentTypesChannel().packetCount());
        loadingStatePacket.writeInt(SlimefunServerEssentials.getItemsChannel().packetCount());
        loadingStatePacket.writeInt(SlimefunServerEssentials.getRecipeCategoriesChannel().packetCount());
        loadingStatePacket.writeInt(SlimefunServerEssentials.getRecipeDisplaysChannel().packetCount());
        loadingStatePacket.writeInt(2);
        loadingStatePacket.writeUTF(SlimefunServerEssentials.getInstance().getConfig().getString("server-id", ""));
        loadingStatePacket.writeUTF(SlimefunServerEssentials.getComponentTypesChannel().contentHash());
        loadingStatePacket.writeUTF(SlimefunServerEssentials.getItemsChannel().contentHash());
        loadingStatePacket.writeUTF(SlimefunServerEssentials.getRecipeCategoriesChannel().contentHash());
        loadingStatePacket.writeUTF(SlimefunServerEssentials.getRecipeDisplaysChannel().contentHash());
        sendMessage(player, loadingStatePacket);

        SlimefunServerEssentials.getComponentTypesChannel().scheduleMessages(player);
        SlimefunServerEssentials.getItemsChannel().scheduleMessages(player);
        SlimefunServerEssentials.getRecipeCategoriesChannel().scheduleMessages(player);
        SlimefunServerEssentials.getRecipeDisplaysChannel().scheduleMessages(player);
    }

    @Override
    public String getChannel() {
        return "slimefun_server_essentials:loading_state";
    }
}
