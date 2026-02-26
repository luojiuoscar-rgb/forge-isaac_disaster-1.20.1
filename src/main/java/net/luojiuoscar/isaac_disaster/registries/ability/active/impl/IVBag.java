package net.luojiuoscar.isaac_disaster.registries.ability.active.impl;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.LootTableManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ActiveAbility;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class IVBag extends ActiveAbility {
    public IVBag(int id, int level) {
        super(id, level);
    }

    @Override
    public void onFirstUse(ServerPlayer player, @Nullable ItemStack stack, @javax.annotation.Nullable InteractionHand hand) {

    }

    @Override
    public void onTrigger(ServerPlayer player, ItemStack stack, @javax.annotation.Nullable InteractionHand hand) {
        if (player.invulnerableTime > 0) return;

        float health = player.getHealth();
        float damage = (float) (StatManager.MAX_HEALTH.getBonus() * 0.5);

        // 红心足够时，优先扣除红心
        if (health > damage){
            player.setHealth(health - damage);
            // 虚假伤害效果
            player.hurt(player.damageSources().genericKill(), 0.01f);
        }
        // 不足时，直接伤害
        else{
            player.hurt(player.damageSources().genericKill(), damage);
        }

        // 缩短受伤冷却时间
        player.invulnerableTime = 10;
        LootHelper.spawnLootAtPos(player, player.position(), LootTableManager.RANDOM_COINS);
    }

    @Override
    public void onTriggerStronger(ServerPlayer player, ItemStack stack, @javax.annotation.Nullable InteractionHand hand){
        onTrigger(player, stack, hand);
    }

    @Override
    public void triggerSFX(ServerPlayer player) {
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                Component.translatable("item.isaac_disaster.iv_bag.lore.1", StatManager.MAX_HEALTH.getBonus() * 0.5),
                Component.translatable("item.isaac_disaster.iv_bag.lore.2")
        );
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack) {
        List<Component> description = new ArrayList<>();

        if (ClientDataManager.getInstance().getCountFromId(ItemId.CAR_BATTERY.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.car_battery").append(": ")
                    .append(Component.translatable("item.isaac_disaster.synergy.description.no_effect"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }
        return description;
    }
}
