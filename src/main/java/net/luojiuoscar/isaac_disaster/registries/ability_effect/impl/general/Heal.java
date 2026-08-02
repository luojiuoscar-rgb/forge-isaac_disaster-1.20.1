package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

public class Heal implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (context.getEntity() instanceof ServerPlayer player){
            Item item = context.get(ContextKeys.ITEM);
            boolean hasMaggysBow = PlayerHelper.hasItem(ItemId.MAGGYS_BOW.getId(), player);
            StatManager.healHealth(player, (float) getHealingAmplifier(
                    context.getOrDefault(ContextKeys.AMPLIFIER, 1.), hasMaggysBow, isRedHeartPickup(item)
            ));
        }
        return true;
    }

    static double getHealingAmplifier(double amplifier, boolean hasMaggysBow, boolean isRedHeartPickup) {
        return hasMaggysBow && isRedHeartPickup ? amplifier * 2 : amplifier;
    }

    private static boolean isRedHeartPickup(Item item) {
        return item == ModItems.HALF_RED_HEART.get()
                || item == ModItems.RED_HEART.get()
                || item == ModItems.DOUBLE_RED_HEART.get()
                || item == ModItems.BLENDED_HEART.get();
    }
}
