package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.effect.IStackableEffect;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class FragileHeartActive implements IAbilityEffect {
    @Override
    public boolean applyEffect(AbilityEffectContext context) {
        if (!(context.get(ContextKeys.EVENT) instanceof LivingHurtEvent event)) return false;

        if (!(event.getEntity() instanceof Player player)) return true;
        if (event.getAmount() <= Math.max(1.0f, StatManager.MAX_HEALTH.getBonus() * 0.25f)) return true;

        double emptyHealth = player.getMaxHealth() - player.getHealth();
        // 当前骨心中有生命值时不消耗
        if (emptyHealth >= StatManager.MAX_HEALTH.getBonus()){

            ((IStackableEffect) ModEffects.FRAGILE_HEART.get()).stack(player, -1);

            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.BONE_HEART.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            event.setAmount(0.0f); // 骨心破碎时不额外扣除生命值
        }
        return true;
    }
}
