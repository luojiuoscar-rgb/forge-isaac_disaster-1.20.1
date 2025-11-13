package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerStatModifierProvider;
import net.luojiuoscar.isaac_disaster.event.custom.IsaacBulletAfterHitEvent;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Polyphemus implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.POLYPHEMUS.getId();
    }

    @Override
    public void handleFirstObtain(Player player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(Player player, @Nullable ItemStack stack) {
        StatManager.DAMAGE_MULTIPLY_BASE.apply(player, 0.8);
        StatManager.DAMAGE.apply(player, 4);

        // 增加一层“双倍延迟”
        player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                playerStatModifier -> playerStatModifier.modifyDoubleShotDelay(player, 1)
        );
    }

    @Override
    public void handleRemove(Player player, @Nullable ItemStack stack) {
        StatManager.DAMAGE_MULTIPLY_BASE.apply(player, -0.8);
        StatManager.DAMAGE.apply(player, -4);

        player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                playerStatModifier -> playerStatModifier.modifyDoubleShotDelay(player, -1)
        );
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                StatManager.DAMAGE_MULTIPLY_BASE.description(0.8),
                StatManager.DAMAGE.description(4),
                Component.translatable("item.isaac_disaster.polyphemus.lore.1"),
                Component.translatable("item.isaac_disaster.polyphemus.lore.2")
        );
    }

    public static void onTriggered(IsaacBulletAfterHitEvent event){
        if (!(event.getBullet().getOwner() instanceof ServerPlayer player)) return;

        double damage = event.getDamage();

        double effectiveDamage = event.getTargetHealth();
        double newDamage = damage - effectiveDamage;
        if (newDamage > 0){  // 如果还有伤害剩余
            event.setDiscardAfterHit(false);
            event.setDamage(newDamage);
            // 计算新体型
            event.getBullet().setScale(PlayerHelper.getBulletScale(newDamage, PlayerHelper.getExtraBulletScale(player)));
        }
    }
}
