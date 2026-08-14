package net.luojiuoscar.isaac_disaster.effect.custom;

import net.luojiuoscar.isaac_disaster.manager.TagManager;
import net.luojiuoscar.isaac_disaster.networking.EntityVisualStateSync;
import net.luojiuoscar.isaac_disaster.system.freeze.state.EntityVisualState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Shared lifecycle for entity effects that freeze ordinary mobs. */
public abstract class EntityFreezeEffect extends MobEffect {
    private final ResourceLocation freezeSource;
    private final ResourceLocation visualLayer;

    protected EntityFreezeEffect(MobEffectCategory category, int color, ResourceLocation freezeSource,
                                 ResourceLocation visualLayer) {
        super(category, color);
        this.freezeSource = freezeSource;
        this.visualLayer = visualLayer;
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player || entity.getType().is(TagManager.BOSSES)) {
            entity.removeEffect(this);
        }
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        if (entity instanceof Mob mob && EntityVisualState.isEligible(entity)) {
            mob.setSilent(true);
            setVisualState(entity, true);
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        if (entity instanceof Mob mob && EntityVisualState.isEligible(entity)) {
            setVisualState(entity, false);
            mob.setSilent(EntityVisualState.isFrozen(entity));
        }
    }

    protected static void reconcileVisualState(LivingEntity entity, MobEffect effect,
                                               ResourceLocation source, ResourceLocation layer) {
        if (!entity.level().isClientSide && EntityVisualState.isEligible(entity)) {
            setVisualState(entity, entity.hasEffect(effect), source, layer);
        }
    }

    private void setVisualState(LivingEntity entity, boolean active) {
        setVisualState(entity, active, freezeSource, visualLayer);
    }

    private static void setVisualState(LivingEntity entity, boolean active,
                                       ResourceLocation source, ResourceLocation layer) {
        if (entity.level().isClientSide || !EntityVisualState.isEligible(entity)) {
            return;
        }

        boolean changed = EntityVisualState.mutate(entity, state -> {
            if (active) {
                state.addFreezeSource(source);
                if (layer != null) {
                    state.addVisualLayer(layer);
                }
            } else {
                state.removeFreezeSource(source);
                if (layer != null) {
                    state.removeVisualLayer(layer);
                }
            }
        });
        if (changed) {
            EntityVisualStateSync.syncToTracking(entity);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
