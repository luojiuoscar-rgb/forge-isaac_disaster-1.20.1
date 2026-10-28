package net.luojiuoscar.isaac_disaster.effect.custom;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID)
public final class BurningEffect extends MobEffect {
    public static final int DURATION = 80;
    public static final int DAMAGE_INTERVAL = 40;
    private static final String DAMAGE_CLOCK = IsaacDisaster.MOD_ID + ":burning_ticks";

    public BurningEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF9933);
    }

    public static float damageForAmplifier(int amplifier) {
        return DeadlyPoisonEffect.damageForAmplifier(amplifier);
    }

    public static int amplifierForDamage(double damage) {
        return DeadlyPoisonEffect.amplifierForDamage(damage);
    }

    /** Advances the independent pulse clock; zero means a damage pulse is due. */
    public static int advanceDamageClock(int elapsedTicks) {
        return (Mth.clamp(elapsedTicks, 0, DAMAGE_INTERVAL - 1) + 1) % DAMAGE_INTERVAL;
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        super.addAttributeModifiers(entity, attributes, amplifier);
        if (entity.level().isClientSide) return;
        if (!entity.getPersistentData().contains(DAMAGE_CLOCK)) {
            entity.getPersistentData().putInt(DAMAGE_CLOCK, 0);
        }
        var effect = entity.getEffect(ModEffects.BURNING.get());
        int duration = effect == null || effect.isInfiniteDuration() ? DURATION : effect.getDuration();
        int seconds = Mth.clamp((int) Math.ceil(Math.max(0, duration) / 20.0), 1, Integer.MAX_VALUE / 20);
        entity.setSecondsOnFire(seconds);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide || !entity.isAlive()) return;
        if (!entity.isOnFire()) {
            entity.removeEffect(ModEffects.BURNING.get());
            return;
        }
        int elapsed = advanceDamageClock(entity.getPersistentData().getInt(DAMAGE_CLOCK));
        entity.getPersistentData().putInt(DAMAGE_CLOCK, elapsed);
        if (elapsed == 0) entity.hurt(entity.damageSources().onFire(), damageForAmplifier(amplifier));
    }

    /** Clears the independent pulse clock without changing ordinary fire from other sources. */
    public static void clear(LivingEntity entity) {
        if (entity.level().isClientSide || !entity.getPersistentData().contains(DAMAGE_CLOCK)) return;
        entity.getPersistentData().remove(DAMAGE_CLOCK);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect() == ModEffects.BURNING.get()) clear(event.getEntity());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onExpired(MobEffectEvent.Expired event) {
        if (event.getEffectInstance() != null && event.getEffectInstance().getEffect() == ModEffects.BURNING.get()) {
            clear(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onApplicable(MobEffectEvent.Applicable event) {
        if (event.getEffectInstance().getEffect() == ModEffects.BURNING.get()) {
            LivingEntity target = event.getEntity();
            if (target.fireImmune() || target.isInWaterRainOrBubble() || target.isInPowderSnow) {
                event.setResult(Event.Result.DENY);
            }
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!event.getEntity().level().isClientSide) {
            event.getEntity().removeEffect(ModEffects.BURNING.get());
            clear(event.getEntity());
        }
    }
}
