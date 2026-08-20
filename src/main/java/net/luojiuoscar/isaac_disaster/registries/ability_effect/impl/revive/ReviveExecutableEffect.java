package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.revive;

import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.ReviveEntityEventS2CPacket;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public abstract class ReviveExecutableEffect implements IAbilityEffect {
    @Override
    public final boolean applyEffect(ExecutableEffectContext context) {
        apply(context);
        return true;
    }

    @Override
    public final void apply(ExecutableEffectContext context) {
        LivingEntity entity = context.getEntity();
        if (!(entity.level() instanceof ServerLevel)) {
            return;
        }

        Object event = context.get(ContextKeys.EVENT);
        if (event instanceof LivingDeathEvent deathEvent && deathEvent.getEntity() == entity) {
            deathEvent.setCanceled(true);
        }

        entity.setHealth(Math.max(entity.getHealth(), 1.0F));

        // 先完成传送等具体复活逻辑，表现应当使用最终位置。
        applyReviveEffect(context);

        ModMessages.sendToTrackingAndSelf(
                new ReviveEntityEventS2CPacket(entity, getSound(), getParticle(), getDisplayItem()), entity);
    }

    protected abstract void applyReviveEffect(ExecutableEffectContext context);

    protected SoundEvent getSound() {
        return SoundEvents.TOTEM_USE;
    }

    protected ParticleOptions getParticle() {
        return ParticleTypes.TOTEM_OF_UNDYING;
    }

    protected ItemStack getDisplayItem() {
        return new ItemStack(Items.TOTEM_OF_UNDYING);
    }
}
