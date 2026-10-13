package net.luojiuoscar.isaac_disaster.system.stat_multiplier;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

/** Immutable parameters for one source-owned attribute multiplier. */
public record MultiplierEntry(ResourceLocation id, UUID uuid, ResourceLocation sourceId,
                              ResourceLocation attributeId, double amount, AttributeModifier.Operation operation) {
    public static final ResourceLocation FIRING_PENALTY_ID =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "multiplier/firing_penalty");
    public static final ResourceLocation DAMAGE_BOOST_ID =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "multiplier/damage_boost");

    public record Key(ResourceLocation id, ResourceLocation sourceId) {
    }

    public Key key() {
        return new Key(id, sourceId);
    }

    public static MultiplierEntry of(ResourceLocation id, UUID uuid, ResourceLocation sourceId, Attribute attribute,
                                     double amount, AttributeModifier.Operation operation) {
        ResourceLocation attributeId = attribute == null ? null : ForgeRegistries.ATTRIBUTES.getKey(attribute);
        if (attributeId == null) {
            IsaacDisaster.LOGGER.warn("Cannot create multiplier entry {} with an unregistered attribute", id);
            return null;
        }
        return new MultiplierEntry(id, uuid, sourceId, attributeId, amount, operation);
    }

}
