package net.luojiuoscar.isaac_disaster.system;

import net.luojiuoscar.isaac_disaster.capability.entity.ExtraData;
import net.luojiuoscar.isaac_disaster.capability.entity.ExtraDataProvider;
import net.luojiuoscar.isaac_disaster.manager.TagManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

public final class EntityVisualState {
    private EntityVisualState() {
    }

    public static boolean mutate(LivingEntity entity, Consumer<ExtraData> mutation) {
        final boolean[] changed = {false};
        entity.getCapability(ExtraDataProvider.EXTRA_DATA_CAP).ifPresent(state -> {
            Set<ResourceLocation> sourcesBefore = Set.copyOf(state.getFreezeSources());
            Set<ResourceLocation> layersBefore = Set.copyOf(state.getActiveVisualLayers());
            mutation.accept(state);
            changed[0] = !sourcesBefore.equals(state.getFreezeSources())
                    || !layersBefore.equals(state.getActiveVisualLayers());
        });
        return changed[0];
    }

    public static boolean isFrozen(LivingEntity entity) {
        return entity.getCapability(ExtraDataProvider.EXTRA_DATA_CAP)
                .map(ExtraData::isFrozen)
                .orElse(false);
    }

    public static boolean hasLayer(LivingEntity entity, ResourceLocation layer) {
        return entity.getCapability(ExtraDataProvider.EXTRA_DATA_CAP)
                .map(state -> state.hasVisualLayer(layer))
                .orElse(false);
    }

    public static Set<ResourceLocation> getFreezeSources(LivingEntity entity) {
        return entity.getCapability(ExtraDataProvider.EXTRA_DATA_CAP)
                .map(ExtraData::getFreezeSources)
                .orElse(Collections.emptySet());
    }

    public static Set<ResourceLocation> getActiveVisualLayers(LivingEntity entity) {
        return entity.getCapability(ExtraDataProvider.EXTRA_DATA_CAP)
                .map(ExtraData::getActiveVisualLayers)
                .orElse(Collections.emptySet());
    }

    public static boolean isEligible(LivingEntity entity) {
        return entity instanceof Mob && !entity.getType().is(TagManager.BOSSES);
    }
}
