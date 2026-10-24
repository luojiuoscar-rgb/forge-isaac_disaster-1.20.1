package net.luojiuoscar.isaac_disaster.groundeffect.impl;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.groundeffect.core.AbstractGroundEffect;
import net.luojiuoscar.isaac_disaster.groundeffect.core.GroundEffectContact;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.profile.PotionProfile;
import net.luojiuoscar.isaac_disaster.registries.ground_effect.GroundEffectType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public final class PotionPuddleEffect extends AbstractGroundEffect {
    private int color = 0xFFFFFF;
    private List<PotionProfile> potions = List.of();

    public PotionPuddleEffect(GroundEffectType<PotionPuddleEffect> type, int slot, int generation,
                              @Nullable UUID owner, Vec3 position, double radius, int lifetime, float damage,
                              long order, int animationDurationTicks) {
        super(type, slot, generation, owner, position, radius, lifetime, damage, order, animationDurationTicks);
    }

    public void setColor(int value) {
        int rgb = value & 0xFFFFFF;
        if (color == rgb) return;
        color = rgb;
        markDirty();
    }

    public List<PotionProfile> getPotions() { return potions; }

    public void setPotions(@Nullable List<PotionProfile> profiles) {
        if (profiles == null) {
            potions = List.of();
        } else if (profiles.stream().anyMatch(profile -> profile == null || profile.effect == null)) {
            IsaacDisaster.LOGGER.warn("Ignoring invalid potion-puddle configuration");
        } else {
            potions = List.copyOf(profiles);
        }
    }

    @Override
    public int getColor() { return color; }

    @Override
    public boolean affectsAllLivingEntities() { return true; }

    @Override
    public void onContactEffect(GroundEffectContact contact) {
        if (!contact.target().isAlive() || potions.isEmpty()) return;
        var context = new ExecutableEffectContext(contact.target());
        context.set(ContextKeys.POTIONS, potions);
        ModExecutableEffects.POTIONS.get().apply(context);
    }
}
