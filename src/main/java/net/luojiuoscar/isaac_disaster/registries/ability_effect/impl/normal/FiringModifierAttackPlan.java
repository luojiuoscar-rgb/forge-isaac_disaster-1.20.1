package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackPlanEvent;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IExecutableEffect;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackOrigin;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.Objects;

public class FiringModifierAttackPlan implements IExecutableEffect {
    @Override
    public TriggerType getRequiredTriggerType() {
        return ModTriggerTypes.ATTACK_PLAN;
    }

    @Override
    public void apply(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return;
        if (!(context.get(ContextKeys.EVENT) instanceof AttackPlanEvent event)) return;
        if (event.getOrigin() != AttackOrigin.PLAYER_PRIMARY) return;

        MobEffectInstance wiz = player.getEffect(ModEffects.THE_WIZ.get());
        player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).ifPresent(playerIsaacItems -> {
            int baseCount = event.getBaseContexts().size();
            int innerEyeCount = playerIsaacItems.getItemCountFromAll(ModPassiveItems.THE_INNER_EYE.getId());
            int mutantSpiderCount = playerIsaacItems.getItemCountFromAll(ModPassiveItems.MUTANT_SPIDER.getId());
            int perfectVisionCount = playerIsaacItems.getItemCountFromAll(ModPassiveItems.PERFECT_VISION.getId());

            int count = computeBulletCount(baseCount, innerEyeCount, mutantSpiderCount, perfectVisionCount);
            count = Math.min(count, 17);
            if (wiz != null) {
                count = Math.min(count, 8);
            }

            event.replaceBaseContexts(event.getAttackType().getAttackContexts(player, count).stream()
                    .filter(Objects::nonNull)
                    .toList());
        });
    }

    private static int computeBulletCount(int baseCount, int innerEyeCount,
                                          int mutantSpiderCount, int perfectVisionCount) {
        int count = baseCount;

        if (perfectVisionCount >= 1) {
            if (innerEyeCount + mutantSpiderCount == 0) {
                count += 1;
            } else {
                count += perfectVisionCount - 1;
            }
        }

        if (innerEyeCount + mutantSpiderCount > 0) {
            count += innerEyeCount + (2 * mutantSpiderCount) + 1;
        }

        return count;
    }
}
