package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general;

import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class GenerateLoot implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        LivingEntity entity = context.getEntity();
        Vec3 pos = context.getOrDefault(ContextKeys.TARGET_POSITION, entity.position());
        InteractionHand hand = context.get(ContextKeys.HAND);

        var rls = context.getOrDefault(ContextKeys.RESOURCE_LOCATIONS, List.of());
        if (rls.isEmpty()) return false;
        ResourceLocation loot = rls.get(0);

        LootHelper.spawnLootAtPos(entity, pos, loot);

        // 对生成掉落的源施加一个小冷却
        if (entity instanceof Player player && hand != null){
            player.getCooldowns().addCooldown(player.getItemInHand(hand).getItem(), 5);
        }
        return true;
    }
}
