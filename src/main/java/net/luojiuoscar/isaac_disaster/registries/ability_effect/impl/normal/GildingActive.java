package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.manager.ModLootTables;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class GildingActive implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        LivingEntity entity = context.getEntity();
        MobEffectInstance instance = entity.getEffect(ModEffects.GILDING.get());
        if (instance == null) return false;
        Vec3 pos = context.getOrDefault(ContextKeys.TARGET_POSITION, entity.position());

        int amplifier = instance.getAmplifier() + 1;
        LootHelper.spawnLootAtPos(entity, entity.position(), ModLootTables.RANDOM_COINS, amplifier);
        entity.removeEffect(ModEffects.GILDING.get()); // 移除

        entity.level().playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0f, 1.0f);

        return true;
    }
}
