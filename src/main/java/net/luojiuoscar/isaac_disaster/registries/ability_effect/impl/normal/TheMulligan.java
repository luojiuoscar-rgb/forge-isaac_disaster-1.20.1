package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackAfterHitEvent;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.familiar.ModFamiliarEntities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

public final class TheMulligan implements IAbilityEffect {
    public static final int GENERATION_LIMIT = 30;

    public static double chance() { return 1.0 / 6.0; }

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        var event = context.get(ContextKeys.EVENT);
        if (event instanceof IsaacAttackAfterHitEvent hit) {
            var bullet = hit.getBulletObject();
            if (hit.isCanceled() || hit.getDamage() <= 0 || bullet.getOwner() != player
                    || bullet.getShooter() != player) return true;
            if (!ModAttackTypes.BULLET.getId().equals(bullet.getRootTypeId())
                    && !ModAttackTypes.LASER.getId().equals(bullet.getRootTypeId())) return true;
        } else if (event instanceof LivingAttackEvent hit) {
            if (hit.isCanceled() || hit.getAmount() <= 0 || hit.getSource().getEntity() != player
                    || !PlayerHelper.isHitAllowedType(hit.getSource())) return true;
        } else return false;
        var data = player.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElse(null);
        if (data == null) return false;
        var source = context.get(ContextKeys.SOURCE);
        if (source == null) return false;
        var type = ModFamiliarEntities.BLUE_FLY.getId();
        if (data.getCount(type, source) >= GENERATION_LIMIT
                || player.getRandom().nextDouble() >= chance()) return true;
        FamiliarHelper.spawnFamiliars(player, type, source, player.position());
        return true;
    }
}
