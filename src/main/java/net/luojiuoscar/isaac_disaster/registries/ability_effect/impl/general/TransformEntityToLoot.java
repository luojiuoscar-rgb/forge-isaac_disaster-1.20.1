package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general;

import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.manager.LootTableManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class TransformEntityToLoot implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {

        List<ResourceLocation> rls = context.getOrDefault(ContextKeys.RESOURCE_LOCATIONS, List.of());
        ResourceLocation loot = rls.isEmpty() ? LootTableManager.RANDOM_COINS : rls.get(0);

        List<LivingEntity> entities = context.get(ContextKeys.SECONDARY_LIVING_ENTITIES);
        if (entities == null || entities.isEmpty()) return false;

        for (Entity e : entities){
            Vec3 pos = e.position();

            if (e instanceof Player){
                return false;
            }else {
                e.discard();
            }

            LootHelper.spawnLootAtPos(context.getEntity(), pos, loot);
        }

        return true;
    }
}
