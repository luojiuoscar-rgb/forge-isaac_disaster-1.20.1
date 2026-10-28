package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.effect.custom.BurningEffect;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackAfterHitEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackHitBlockEvent;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.profile.PotionProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

import java.util.List;

public final class FireMindImpact implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        LivingEntity caster = context.getEntity();
        if (caster == null || !(caster.level() instanceof ServerLevel level)) return false;
        if (context.get(ContextKeys.EVENT) instanceof LivingAttackEvent event) {
            if (event.isCanceled() || event.getSource().getEntity() != caster
                    || !PlayerHelper.isHitAllowedType(event.getSource())
                    || event.getEntity().level() != level || !Float.isFinite(event.getAmount())) return false;
            return burn(context, event.getEntity());
        }
        if (context.get(ContextKeys.EVENT) instanceof IsaacAttackAfterHitEvent event
                && event.getHit().getEntity() instanceof LivingEntity target) {
            if (event.isCanceled() || event.getBulletObject() == null
                    || event.getBulletObject().getOwner() != caster || target.level() != level) return false;
            boolean completed = burn(context, target);
            placeFire(level, caster, target.blockPosition(), Direction.UP);
            return completed;
        }
        if (context.get(ContextKeys.EVENT) instanceof IsaacAttackHitBlockEvent event) {
            if (event.isCanceled() || event.getBulletObject() == null
                    || event.getBulletObject().getOwner() != caster) return false;
            var hit = event.getHitResult();
            placeFire(level, caster, hit.getBlockPos().relative(hit.getDirection()), hit.getDirection());
            return true;
        }
        return false;
    }

    private static boolean burn(ExecutableEffectContext context, LivingEntity target) {
        if (!target.isAlive() || target.fireImmune()) return true;
        var attribute = context.getEntity().getAttribute(Attributes.ATTACK_DAMAGE);
        int amplifier = BurningEffect.amplifierForDamage(attribute == null ? 0 : attribute.getValue());
        var existing = target.getEffect(ModEffects.BURNING.get());
        if (existing != null) amplifier = Math.max(amplifier, existing.getAmplifier());
        var targetContext = context.copy(target);
        targetContext.set(ContextKeys.AMPLIFIER, 1.0);
        targetContext.set(ContextKeys.POTIONS, List.of(new PotionProfile(
                ModEffects.BURNING.get(), BurningEffect.DURATION, amplifier)));
        return ModExecutableEffects.POTIONS.get() instanceof IAbilityEffect effect
                && effect.applyEffect(targetContext);
    }

    private static void placeFire(ServerLevel level, LivingEntity caster, BlockPos position, Direction face) {
        if (!level.isInWorldBounds(position)
                || !level.hasChunksAt(position.offset(-1, -1, -1), position.offset(1, 1, 1))
                || !level.isEmptyBlock(position) || !level.getFluidState(position).isEmpty()) return;
        if (caster instanceof Player player && !level.mayInteract(player, position)) return;
        var fire = BaseFireBlock.getState(level, position);
        if (!fire.canSurvive(level, position)) return;
        BlockSnapshot snapshot = BlockSnapshot.create(level.dimension(), level, position);
        if (!level.setBlockAndUpdate(position, fire)) return;
        if (ForgeEventFactory.onBlockPlace(caster, snapshot, face)) {
            boolean restoring = level.restoringBlockSnapshots;
            level.restoringBlockSnapshots = true;
            try {
                snapshot.restore(true, false);
            } finally {
                level.restoringBlockSnapshots = restoring;
            }
        }
    }
}
