package net.luojiuoscar.isaac_disaster.groundeffect.core;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** One grouped contact between a target and all overlapping effects of one type. */
public record GroundEffectContact(
        ServerLevel level,
        LivingEntity target,
        @Nullable LivingEntity owner,
        List<AbstractGroundEffect> effects) {

    public GroundEffectContact {
        effects = List.copyOf(effects);
    }

    public float ownerAttackDamage() {
        return owner == null ? 0.0F : (float) owner.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }
}
