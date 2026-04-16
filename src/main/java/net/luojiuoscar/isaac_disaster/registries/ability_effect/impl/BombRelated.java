package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl;

import net.luojiuoscar.isaac_disaster.entity.tnt.IsaacBomb;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class BombRelated implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!context.has(ContextKeys.ENTITY) || context.get(ContextKeys.ENTITY).isEmpty()) return false;
        if (!(context.get(ContextKeys.ENTITY).get(0) instanceof IsaacBomb bomb)) return false;
        if (!(context.getEntity() instanceof ServerPlayer player)) return true;

        Level level = player.level();
        Vec3 pos = context.getOrDefault(ContextKeys.TARGET_POSITION, player.position());


        return customEffect(context, player, level, pos, bomb);
    }

    abstract protected boolean customEffect(ExecutableEffectContext context,
                                         ServerPlayer player,
                                         Level level, Vec3 pos, IsaacBomb bomb);
}
