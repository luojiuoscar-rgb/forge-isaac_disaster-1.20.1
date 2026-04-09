package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.effect.IStackableEffect;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class NecronmiconShieldActive implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.get(ContextKeys.EVENT) instanceof LivingHurtEvent event)) return false;
        if (event.isCanceled()) return true;

        Entity attacker = event.getSource().getEntity();
        if (!(event.getEntity() instanceof ServerPlayer player)) return true;

        if (event.getAmount() <= Math.max(1.0f, StatManager.MAX_HEALTH.getBonus() * 0.25f)) return true;
        // 伤害来源不能是拥有死灵庇护的玩家；否则不生效
        if (attacker instanceof Player attackerplayer &&
                attackerplayer.hasEffect(ModEffects.NECRONMICON_SHIELD.get())) return true;

        // 先减少层数再触发
        ((IStackableEffect) ModEffects.NECRONMICON_SHIELD.get()).stack(player, -1);
        ModExecutableEffects.THE_NECRONMICON.get().apply(context);

        return true;
    }
}
