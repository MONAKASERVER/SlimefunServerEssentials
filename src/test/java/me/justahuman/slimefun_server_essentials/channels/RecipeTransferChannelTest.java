package me.justahuman.slimefun_server_essentials.channels;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecipeTransferChannelTest {

    @Test
    void usesTheRegisteredNetworksCraftingGridId() {
        assertEquals("NTW_CRAFTING_GRID", RecipeTransferChannel.NETWORK_CRAFTING_GRID_ID);
    }
}
