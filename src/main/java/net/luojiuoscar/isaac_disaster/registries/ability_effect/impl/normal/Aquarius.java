package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.event.TriggerModuleEvents;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ground_effect.ModGroundEffects;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.impl.AquariusPuddleEffect;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.server.GroundEffectRuntime;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class Aquarius implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player) || !player.isAlive()) return false;
        float damage = (float) Math.min(Float.MAX_VALUE,
                Math.max(0, player.getAttributeValue(Attributes.ATTACK_DAMAGE) * 3.3));
        var effect = GroundEffectRuntime.INSTANCE.spawnOnGround(ModGroundEffects.AQUARIUS.get(), player,
                player.position(), AquariusPuddleEffect.RADIUS, AquariusPuddleEffect.LIFETIME, damage);
        if (effect == null) return true;
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(ability -> {
            effect.setHoming(ability.getHoming() > 0);
            effect.offerColors(ability.getBulletColorIds());
        });
        var preparation = new ExecutableEffectContext(player);
        preparation.set(ContextKeys.EFFECT_ATTACHMENT_NODE, effect);
        preparation.set(ContextKeys.TARGET_POSITION, effect.position());
        TriggerModuleEvents.dispatch(preparation, ModTriggerTypes.EFFECT_ATTACHMENT_PREPARE);
        return true;
    }
}
