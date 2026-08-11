package net.luojiuoscar.isaac_disaster.effect.custom;

import net.luojiuoscar.isaac_disaster.manager.TagManager;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.networking.EntityVisualStateSync;
import net.luojiuoscar.isaac_disaster.registries.visual.ModVisualLayers;
import net.luojiuoscar.isaac_disaster.system.EntityVisualState;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GoldenEffect extends MobEffect {
    public GoldenEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity pLivingEntity, int pAmplifier){
        if (pLivingEntity.getType().is(TagManager.BOSSES) || pLivingEntity instanceof Player) {
            pLivingEntity.removeEffect(this);
            return;
        }
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        if (entity instanceof Mob mob){
            mob.setSilent(true);
            setVisualState(entity, true);
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        if (entity instanceof Mob mob){
            mob.setSilent(false);
            setVisualState(entity, false);
        }
    }

    public static void reconcileVisualState(LivingEntity entity) {
        if (!entity.level().isClientSide) {
            setVisualState(entity, entity.hasEffect(ModEffects.GOLDEN.get()));
        }
    }

    private static void setVisualState(LivingEntity entity, boolean active) {
        if (entity.level().isClientSide || !EntityVisualState.isEligible(entity)) {
            return;
        }

        boolean changed = EntityVisualState.mutate(entity, state -> {
            if (active) {
                state.addFreezeSource(ModVisualLayers.GOLDEN.getId());
                state.addVisualLayer(ModVisualLayers.GOLDEN.getId());
            } else {
                state.removeFreezeSource(ModVisualLayers.GOLDEN.getId());
                state.removeVisualLayer(ModVisualLayers.GOLDEN.getId());
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
