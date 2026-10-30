package net.luojiuoscar.isaac_disaster.manager;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerStatModifierProvider;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** Updates the shared multishot contribution from held items and a pending acquisition. */
public final class MultishotCountManager {
    public static final UUID MODIFIER_ID = UUID.nameUUIDFromBytes(
            (IsaacDisaster.MOD_ID + ":stable_multishot_count").getBytes(StandardCharsets.UTF_8));

    private MultishotCountManager() {}

    public static int bonus(int innerEyes, int mutantSpiders, int perfectVisions) {
        long eyes = Math.max(0, innerEyes);
        long spiders = Math.max(0, mutantSpiders);
        long visions = Math.max(0, perfectVisions);
        long count = eyes + spiders == 0 ? (visions > 0 ? 1 : 0)
                : eyes + 2 * spiders + 1 + Math.max(visions - 1, 0);
        return (int) Math.min(Integer.MAX_VALUE, count);
    }

    public static void update(ServerPlayer player) {
        update(player, null);
    }

    /** Includes the new copy when obtain runs before that item is recorded in either inventory. */
    public static void update(ServerPlayer player, @Nullable ResourceLocation obtainingItem) {
        var items = player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).resolve();
        var stats = player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).resolve();
        var attribute = player.getAttribute(ModAttributes.BULLET_COUNT.get());
        if (items.isEmpty() || stats.isEmpty() || attribute == null) return;
        var counts = items.get();
        int amount = bonus(counts.getItemCountFromAll(ModPassiveItems.THE_INNER_EYE.getId())
                        + (ModPassiveItems.THE_INNER_EYE.getId().equals(obtainingItem) ? 1 : 0),
                counts.getItemCountFromAll(ModPassiveItems.MUTANT_SPIDER.getId())
                        + (ModPassiveItems.MUTANT_SPIDER.getId().equals(obtainingItem) ? 1 : 0),
                counts.getItemCountFromAll(ModPassiveItems.PERFECT_VISION.getId())
                        + (ModPassiveItems.PERFECT_VISION.getId().equals(obtainingItem) ? 1 : 0));
        if (amount != 0 || stats.get().getStatInstance(MODIFIER_ID) != null || attribute.getModifier(MODIFIER_ID) != null) {
            StatManager.setModifier(player, MODIFIER_ID, ModAttributes.BULLET_COUNT.get(), amount, null, null, 0);
        }
    }
}
