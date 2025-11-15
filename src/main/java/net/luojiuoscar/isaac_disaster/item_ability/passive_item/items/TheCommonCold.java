package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.event.custom.attack.PlayerPerformAttackEvent;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IDamageTriggerPassiveItem;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.ISpecialTypeBulletPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.EffectManager;
import net.luojiuoscar.isaac_disaster.manager.attack.managers.BulletColor;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TheCommonCold implements IDamageTriggerPassiveItem, ISpecialTypeBulletPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.THE_COMMON_COLD.getId();
    }

    @Override
    public double getTriggerChance(Player player) {
        return 1 / Math.max(1, 4 - (getPlayerLuck(player) / 4));
    }

    @Override
    public void handleAttackEntityEffect(Player player, LivingEntity target){
        MobEffectInstance poisonEffect = new MobEffectInstance(
                ModEffects.POISON.get(),
                70,
                0,
                false,
                true,
                true
        );

        target.addEffect(poisonEffect, player);
    };


    @Override
    public void handleFirstObtain(Player player, @Nullable ItemStack stack) {

    }

    @Override
    public void handleObtain(Player player, @Nullable ItemStack stack) {

    }

    @Override
    public void handleRemove(Player player, @Nullable ItemStack stack) {

    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.the_common_cold.lore.1")
        );
    }

    @Override
    public List<Component> getExplain(){
        List<Component> description = new ArrayList<>();

        description.add(EffectManager.POISON.getExplainDesc());

        return description;
    }

    @Override
    public void onShoot(PlayerPerformAttackEvent event) {
        Player player = event.getPlayer();

        if (player.getRandom().nextDouble() <= getTriggerChance(player)) {
            event.getContext().colorId  = BulletColor.POISON.getId();

            event.getContext().hitEffects.add(ItemId.THE_COMMON_COLD.getId());
        }
    }

    @Override
    public void onHit(Player source, LivingEntity target) {
        handleAttackEntityEffect(source, target);
    }
}
