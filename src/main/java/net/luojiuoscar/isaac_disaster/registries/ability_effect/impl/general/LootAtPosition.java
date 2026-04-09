package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general;

import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.manager.LootTableManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class LootAtPosition implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        LivingEntity entity = context.getEntity();
        Vec3 pos = context.getOrDefault(ContextKeys.TARGET_POSITION, entity.position());
        if (!(entity.level() instanceof ServerLevel level)) return true;

        var rls = context.getOrDefault(ContextKeys.RESOURCE_LOCATIONS, List.of());
        ResourceLocation loot = rls.isEmpty() ? LootTableManager.RANDOM_COINS : rls.get(0);

        // 空参数 无源掉落
        LootParams params = new LootParams.Builder(level).create(LootContextParamSets.EMPTY);
        LootHelper.spawnLootAtPos(level, pos, loot, params);

        return true;
    }
}
