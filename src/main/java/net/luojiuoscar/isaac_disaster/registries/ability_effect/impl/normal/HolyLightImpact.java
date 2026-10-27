package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackAfterHitEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackHitBlockEvent;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.helper.ScheduledFuncHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.HolyLight;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

public final class HolyLightImpact implements IAbilityEffect {
    private static final ResourceLocation RESTRICTED_STRIKE =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "holy_light_restricted");

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity().level() instanceof ServerLevel level)) return false;
        if (context.get(ContextKeys.EVENT) instanceof LivingAttackEvent event) {
            LivingEntity caster = context.getEntity();
            if (event.isCanceled() || event.getSource().getEntity() != caster
                    || !PlayerHelper.isHitAllowedType(event.getSource()) || !Float.isFinite(event.getAmount())
                    || event.getEntity().level() != level) return false;
            Vec3 position = event.getEntity().position();
            float damage = (float) Math.min(Float.MAX_VALUE, Math.max(0, event.getAmount()) * 3.0);
            // LivingAttackEvent precedes hurt: finish the original hit before setting holy-hit immunity.
            return ScheduledFuncHelper.schedule(RESTRICTED_STRIKE, 1, 0, 1, false, () -> {
                if (!caster.isRemoved() && caster.level() == level) {
                    HolyLight.strike(level, caster, position, damage, true);
                }
            }) > 0;
        }
        IBulletObject bullet;
        Vec3 position;
        if (context.get(ContextKeys.EVENT) instanceof IsaacAttackAfterHitEvent event
                && event.getHit().getEntity() instanceof LivingEntity target) {
            bullet = event.getBulletObject();
            position = target.position();
        } else if (context.get(ContextKeys.EVENT) instanceof IsaacAttackHitBlockEvent event) {
            bullet = event.getBulletObject();
            var hit = event.getHitResult();
            position = hit.getLocation().add(Vec3.atLowerCornerOf(hit.getDirection().getNormal()).scale(0.01));
        } else {
            return false;
        }
        if (bullet == null || bullet.getOwner() != context.getEntity() || !Float.isFinite(bullet.getDamage())) {
            return false;
        }
        float damage = (float) Math.min(Float.MAX_VALUE, bullet.getDamage() * 3.0);
        return HolyLight.strike(level, context.getEntity(), position, damage, true);
    }
}
