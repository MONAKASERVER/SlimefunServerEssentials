package me.justahuman.slimefun_server_essentials.channels;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import me.justahuman.slimefun_server_essentials.SlimefunServerEssentials;
import me.justahuman.slimefun_server_essentials.api.RecipeBuilder;
import me.justahuman.slimefun_server_essentials.implementation.RecipeTransferRegistry;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.nio.charset.StandardCharsets;

/** Secure server-authoritative handling for JEI's recipe transfer (+) button. */
public final class RecipeTransferChannel extends AbstractChannel {
    private static final int MAX_CATEGORY_ID_LENGTH = 128;
    private static final int[] STANDARD_GRID_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8};
    private static final int[] NETWORK_CRAFTING_GRID_SLOTS = {6, 7, 8, 15, 16, 17, 24, 25, 26};
    private static final String NETWORK_CRAFTING_GRID_ID = "NETWORK_CRAFTING_GRID";

    @Override
    public String getChannel() {
        return "slimefun_server_essentials:recipe_transfer";
    }

    @Override
    public void onMessageReceived(@Nonnull Player player, @Nonnull byte[] message) {
        if (message.length == 0 || message.length > 256) {
            return;
        }
        try {
            ByteArrayDataInput input = ByteStreams.newDataInput(message);
            String categoryId = readString(input, MAX_CATEGORY_ID_LENGTH);
            int recipeIndex = readVarInt(input);
            boolean maxTransfer = input.readBoolean();
            if (categoryId.length() > MAX_CATEGORY_ID_LENGTH || recipeIndex < 0) {
                return;
            }
            Runnable transfer = () -> transfer(player, categoryId, recipeIndex, maxTransfer);
            if (Bukkit.isPrimaryThread()) {
                transfer.run();
            } else {
                Bukkit.getScheduler().runTask(SlimefunServerEssentials.getInstance(), transfer);
            }
        } catch (RuntimeException ignored) {
            // Malformed client payloads are ignored without touching inventories.
        }
    }

    private void transfer(Player player, String categoryId, int recipeIndex, boolean maxTransfer) {
        Inventory top = player.getOpenInventory().getTopInventory();
        int[] targetSlots = resolveTargetSlots(top);
        if (targetSlots == null) {
            player.sendMessage("§c自動配置にはドロッパー、ディスペンサー、またはNetworks Crafting Gridを開いてください。");
            return;
        }

        RecipeBuilder recipe = RecipeTransferRegistry.get(categoryId, recipeIndex);
        if (recipe == null || recipe.getInputs().size() > 9
                || (recipe.getWidth() != null && recipe.getWidth() > 3)
                || (recipe.getHeight() != null && recipe.getHeight() > 3)) {
            player.sendMessage("§cこのレシピは3x3へ自動配置できません。");
            return;
        }

        ItemStack[] inventory = cloneContents(player.getInventory().getStorageContents());
        ItemStack[] oldGrid = new ItemStack[targetSlots.length];
        for (int i = 0; i < targetSlots.length; i++) {
            ItemStack stack = top.getItem(targetSlots[i]);
            oldGrid[i] = isEmpty(stack) ? null : stack.clone();
        }
        for (ItemStack stack : oldGrid) {
            if (!isEmpty(stack) && !insert(inventory, stack.clone())) {
                player.sendMessage("§c自動配置には、現在の材料を戻せる空きが必要です。");
                return;
            }
        }

        List<List<ItemStack>> requirements = new ArrayList<>();
        for (String encoded : recipe.getInputs()) {
            requirements.add(encoded == null || encoded.isBlank() ? List.of() : resolveAlternatives(recipe, encoded));
        }
        if (requirements.stream().anyMatch(requirement -> requirement == null)) {
            player.sendMessage("§cこのレシピには自動配置できない材料が含まれています。");
            return;
        }

        int sets = maxTransfer ? maximumSets(inventory, requirements) : 1;
        if (sets < 1) {
            player.sendMessage("§c必要な材料が足りません。");
            return;
        }

        ItemStack[] newGrid = new ItemStack[9];
        for (int slot = 0; slot < requirements.size(); slot++) {
            List<ItemStack> alternatives = requirements.get(slot);
            if (alternatives.isEmpty()) continue;
            ItemStack selected = findAvailable(inventory, alternatives, sets);
            if (selected == null) {
                player.sendMessage("§c必要な材料が足りません。");
                return;
            }
            int required = selected.getAmount() * sets;
            if (required > selected.getMaxStackSize() || !consume(inventory, selected, required)) {
                player.sendMessage("§c一度に配置できる材料数を超えています。");
                return;
            }
            ItemStack placed = selected.clone();
            placed.setAmount(required);
            newGrid[slot] = placed;
        }

        player.getInventory().setStorageContents(inventory);
        for (int i = 0; i < targetSlots.length; i++) {
            top.setItem(targetSlots[i], newGrid[i]);
        }
        player.updateInventory();
        player.sendMessage("§aレシピの材料を自動配置しました" + (sets > 1 ? " §7(§f" + sets + "回分§7)" : "") + "。");
    }

    private int[] resolveTargetSlots(Inventory top) {
        InventoryType type = top.getType();
        if ((type == InventoryType.DISPENSER || type == InventoryType.DROPPER) && top.getSize() == 9) {
            return STANDARD_GRID_SLOTS;
        }
        if (top.getSize() != 54) {
            return null;
        }
        Location location = top.getLocation();
        if (location == null || !NETWORK_CRAFTING_GRID_ID.equals(BlockStorage.checkID(location))) {
            return null;
        }
        return NETWORK_CRAFTING_GRID_SLOTS;
    }

    private int maximumSets(ItemStack[] inventory, List<List<ItemStack>> requirements) {
        ItemStack[] simulation = cloneContents(inventory);
        int completed = 0;
        outer: for (int set = 0; set < 64; set++) {
            for (List<ItemStack> alternatives : requirements) {
                if (alternatives.isEmpty()) continue;
                ItemStack selected = findAvailable(simulation, alternatives, 1);
                if (selected == null || selected.getAmount() * (set + 1) > selected.getMaxStackSize()
                        || !consume(simulation, selected, selected.getAmount())) {
                    break outer;
                }
            }
            completed++;
        }
        return completed;
    }

    private List<ItemStack> resolveAlternatives(RecipeBuilder recipe, String encoded) {
        List<ItemStack> alternatives = new ArrayList<>();
        for (String alternative : encoded.split(",")) {
            ItemStack resolved = resolve(recipe, alternative);
            if (resolved != null && !resolved.getType().isAir()) {
                alternatives.add(resolved);
            }
        }
        return alternatives.isEmpty() ? null : alternatives;
    }

    private ItemStack resolve(RecipeBuilder recipe, String encoded) {
        if (encoded == null || encoded.isBlank() || encoded.startsWith("@")
                || encoded.startsWith("~") || encoded.startsWith("$")) {
            return null;
        }
        String value = encoded;
        int chance = value.indexOf('%');
        if (chance >= 0) value = value.substring(0, chance);
        int damage = -1;
        int damageMarker = value.indexOf('^');
        if (damageMarker >= 0) {
            try {
                damage = Integer.parseInt(value.substring(damageMarker + 1));
            } catch (NumberFormatException ignored) {}
            value = value.substring(0, damageMarker);
        }
        int separator = value.lastIndexOf(':');
        int amount = 1;
        if (separator > 0) {
            try {
                amount = Math.max(1, Integer.parseInt(value.substring(separator + 1)));
                value = value.substring(0, separator);
            } catch (NumberFormatException ignored) {}
        }

        ItemStack result;
        if (value.startsWith("?")) {
            try {
                int index = Integer.parseInt(value.substring(1));
                if (index < 0 || index >= recipe.getComplex().size()) return null;
                result = recipe.getComplex().get(index).clone();
            } catch (NumberFormatException exception) {
                return null;
            }
        } else {
            SlimefunItem slimefun = SlimefunItem.getById(value);
            if (slimefun != null) {
                result = slimefun.getItem().clone();
            } else {
                Material material = Material.matchMaterial(value.toUpperCase(Locale.ROOT));
                if (material == null || material.isAir()) return null;
                result = new ItemStack(material);
            }
        }
        result.setAmount(amount);
        if (damage >= 0 && result.getItemMeta() instanceof Damageable meta) {
            meta.setDamage(damage);
            result.setItemMeta(meta);
        }
        return result;
    }

    private ItemStack findAvailable(ItemStack[] inventory, List<ItemStack> alternatives, int sets) {
        for (ItemStack candidate : alternatives) {
            int needed = candidate.getAmount() * sets;
            int found = 0;
            for (ItemStack stack : inventory) {
                if (!isEmpty(stack) && matches(stack, candidate)) found += stack.getAmount();
            }
            if (found >= needed) return candidate;
        }
        return null;
    }

    private boolean consume(ItemStack[] inventory, ItemStack expected, int amount) {
        int remaining = amount;
        for (int slot = 0; slot < inventory.length && remaining > 0; slot++) {
            ItemStack stack = inventory[slot];
            if (isEmpty(stack) || !matches(stack, expected)) continue;
            int take = Math.min(remaining, stack.getAmount());
            stack.setAmount(stack.getAmount() - take);
            remaining -= take;
            if (stack.getAmount() <= 0) inventory[slot] = null;
        }
        return remaining == 0;
    }

    private boolean insert(ItemStack[] inventory, ItemStack incoming) {
        for (ItemStack stack : inventory) {
            if (isEmpty(stack) || !stack.isSimilar(incoming)) continue;
            int move = Math.min(incoming.getAmount(), stack.getMaxStackSize() - stack.getAmount());
            stack.setAmount(stack.getAmount() + move);
            incoming.setAmount(incoming.getAmount() - move);
            if (incoming.getAmount() == 0) return true;
        }
        for (int slot = 0; slot < inventory.length; slot++) {
            if (!isEmpty(inventory[slot])) continue;
            inventory[slot] = incoming.clone();
            return true;
        }
        return false;
    }

    private boolean matches(ItemStack actual, ItemStack expected) {
        return SlimefunUtils.isItemSimilar(actual, expected, true, false, false);
    }

    private ItemStack[] cloneContents(ItemStack[] source) {
        ItemStack[] result = new ItemStack[source.length];
        for (int i = 0; i < source.length; i++) {
            result[i] = isEmpty(source[i]) ? null : source[i].clone();
        }
        return result;
    }

    private boolean isEmpty(ItemStack stack) {
        return stack == null || stack.getType().isAir() || stack.getAmount() <= 0;
    }

    private int readVarInt(ByteArrayDataInput input) {
        int value = 0;
        int position = 0;
        byte current;
        do {
            current = input.readByte();
            value |= (current & 0x7F) << position;
            if ((position += 7) > 35) throw new IllegalArgumentException("VarInt too big");
        } while ((current & 0x80) != 0);
        return value;
    }

    private String readString(ByteArrayDataInput input, int maxCharacters) {
        int byteLength = readVarInt(input);
        if (byteLength < 0 || byteLength > maxCharacters * 4) {
            throw new IllegalArgumentException("String too long");
        }
        byte[] bytes = new byte[byteLength];
        input.readFully(bytes);
        String value = new String(bytes, StandardCharsets.UTF_8);
        if (value.length() > maxCharacters) {
            throw new IllegalArgumentException("String too long");
        }
        return value;
    }
}
