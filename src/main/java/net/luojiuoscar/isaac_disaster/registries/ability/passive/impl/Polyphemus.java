package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackAfterHitEvent;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Polyphemus extends PassiveAbility {
    public Polyphemus(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.DAMAGE_MULTIPLY_BASE.apply(player, 0.8);
        StatManager.DAMAGE.apply(player, 4);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.DAMAGE_MULTIPLY_BASE.apply(player, -0.8);
        StatManager.DAMAGE.apply(player, -4);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                StatManager.DAMAGE_MULTIPLY_BASE.description(0.8),
                StatManager.DAMAGE.description(4),
                Component.translatable("item.isaac_disaster.polyphemus.lore.1"),
                Component.translatable("item.isaac_disaster.polyphemus.lore.2")
        );
    }

    public static void onTriggered(IsaacAttackAfterHitEvent event){
        if (!(event.getBulletObject() instanceof TearBullet bullet &&
                event.getSource() instanceof Player player)) return;

        double damage = event.getDamage();

        double effectiveDamage = event.getTargetHealth();
        double newDamage = damage - effectiveDamage;
        if (newDamage > 0){  // 如果还有伤害剩余
            event.setDamage(newDamage);
            // 计算新体型
            bullet.setScale(PlayerHelper.getBulletScale(newDamage, PlayerHelper.getExtraBulletScale(player)));
            event.setCanceled(true);
        }
    }
}
