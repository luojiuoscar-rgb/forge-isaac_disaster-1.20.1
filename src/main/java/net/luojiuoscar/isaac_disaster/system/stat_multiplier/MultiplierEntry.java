package net.luojiuoscar.isaac_disaster.system.stat_multiplier;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** Immutable parameters for one source-owned attribute multiplier. */
public record MultiplierEntry(ResourceLocation id, UUID uuid, ResourceLocation sourceId,
                              ResourceLocation attributeId, double amount, AttributeModifier.Operation operation) {

    public static MultiplierEntry of(ResourceLocation id, UUID uuid, ResourceLocation sourceId, Attribute attribute,
                                     double amount, AttributeModifier.Operation operation) {
        ResourceLocation attributeId = attribute == null ? null : ForgeRegistries.ATTRIBUTES.getKey(attribute);
        if (attributeId == null) {
            IsaacDisaster.LOGGER.warn("Cannot create multiplier entry {} with an unregistered attribute", id);
            return null;
        }
        return new MultiplierEntry(id, uuid, sourceId, attributeId, amount, operation);
    }

    static UUID legacyModifierId(ResourceLocation id, int copyIndex) {
        String key = "isaac_disaster:multiplier/" + id + "/" + copyIndex;
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
    }
}
