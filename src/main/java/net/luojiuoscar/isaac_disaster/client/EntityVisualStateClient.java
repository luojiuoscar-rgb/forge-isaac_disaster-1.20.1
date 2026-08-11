package net.luojiuoscar.isaac_disaster.client;

import net.luojiuoscar.isaac_disaster.capability.entity.ExtraDataProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.UUID;

public final class EntityVisualStateClient {
    private EntityVisualStateClient() {
    }

    public static void apply(int entityId, UUID entityUuid,
                             List<ResourceLocation> freezeSources,
                             List<ResourceLocation> activeVisualLayers) {
        if (Minecraft.getInstance().level == null) {
            return;
        }

        Entity entity = Minecraft.getInstance().level.getEntity(entityId);
        if (!(entity instanceof LivingEntity livingEntity)
                || !entityUuid.equals(entity.getUUID())) {
            return;
        }

        livingEntity.getCapability(ExtraDataProvider.EXTRA_DATA_CAP).ifPresent(state ->
                state.replaceRuntimeState(freezeSources, activeVisualLayers));
    }
}
