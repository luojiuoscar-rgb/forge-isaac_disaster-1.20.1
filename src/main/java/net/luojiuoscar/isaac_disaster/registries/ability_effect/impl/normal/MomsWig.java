package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackPlanEvent;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackOrigin;
import net.luojiuoscar.isaac_disaster.registries.familiar.ModFamiliarEntities;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import net.minecraft.server.level.ServerPlayer;

public final class MomsWig implements IAbilityEffect {
    public static double chance(double luck) {
        if (!Double.isFinite(luck)) luck = 0;
        return 1.0 / Math.max(1.0, 20.0 - Math.floor(2.0 * luck));
    }

    public static int generationLimit(boolean hiveMind) { return hiveMind ? 10 : 5; }

    @Override
    public TriggerType getRequiredTriggerType() { return ModTriggerTypes.ATTACK_PLAN; }

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        if (!(context.get(ContextKeys.EVENT) instanceof AttackPlanEvent event)) return false;
        if (event.getOwner() != player || event.getOrigin() != AttackOrigin.PLAYER_PRIMARY
                || event.getBaseContexts().isEmpty()) return true;
        var data = player.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElse(null);
        if (data == null) return false;
        var source = context.get(ContextKeys.SOURCE);
        if (source == null) return false;
        var type = ModFamiliarEntities.BLUE_SPIDER.getId();
        int limit = generationLimit(PlayerHelper.hasItem(ModPassiveItems.HIVE_MIND.getId(), player));
        if (data.getCount(type, source) >= limit
                || player.getRandom().nextDouble() >= chance(getLuck(player))) return true;
        var generationContext = context.copy(null);
        generationContext.set(ContextKeys.TARGET_POSITION, player.position());
        return ((IAbilityEffect) ModExecutableEffects.SPAWN_BLUE_SPIDER.get()).applyEffect(generationContext);
    }
}
