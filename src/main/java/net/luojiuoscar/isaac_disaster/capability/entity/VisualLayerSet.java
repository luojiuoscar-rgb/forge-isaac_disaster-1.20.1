package net.luojiuoscar.isaac_disaster.capability.entity;

import net.luojiuoscar.isaac_disaster.registries.visual.ModVisualLayers;
import net.luojiuoscar.isaac_disaster.registries.visual.VisualLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/** Maintains active visual candidates and the group-resolved visual set. */
public final class VisualLayerSet {
    private final Set<ResourceLocation> activeLayers = new LinkedHashSet<>();
    private final Set<ResourceLocation> resolvedLayers = new LinkedHashSet<>();
    private final Function<ResourceLocation, VisualLayer> definitionResolver;

    public VisualLayerSet() {
        this(VisualLayerSet::registeredDefinition);
    }

    private VisualLayerSet(Function<ResourceLocation, VisualLayer> definitionResolver) {
        this.definitionResolver = definitionResolver;
    }

    public boolean add(ResourceLocation layerId) {
        if (definition(layerId) == null || !activeLayers.add(layerId)) {
            return false;
        }
        rebuildResolvedLayers();
        return true;
    }

    public boolean remove(ResourceLocation layerId) {
        if (!activeLayers.remove(layerId)) {
            return false;
        }
        rebuildResolvedLayers();
        return true;
    }

    public boolean contains(ResourceLocation layerId) {
        return resolvedLayers.contains(layerId);
    }

    public void replaceAll(Collection<ResourceLocation> layerIds) {
        activeLayers.clear();
        for (ResourceLocation layerId : layerIds) {
            if (definition(layerId) != null) {
                activeLayers.add(layerId);
            }
        }
        rebuildResolvedLayers();
    }

    public void clear() {
        activeLayers.clear();
        resolvedLayers.clear();
    }

    public Set<ResourceLocation> getActiveLayers() {
        return Collections.unmodifiableSet(activeLayers);
    }

    public Set<ResourceLocation> getResolvedLayers() {
        return Collections.unmodifiableSet(resolvedLayers);
    }

    private void rebuildResolvedLayers() {
        resolvedLayers.clear();
        Map<ResourceLocation, ResourceLocation> winnersByGroup = new HashMap<>();

        for (ResourceLocation layerId : activeLayers) {
            VisualLayer layer = definition(layerId);
            if (layer == null || layer.group() == null) {
                continue;
            }

            ResourceLocation currentWinnerId = winnersByGroup.get(layer.group());
            if (currentWinnerId == null || wins(layerId, layer, currentWinnerId)) {
                winnersByGroup.put(layer.group(), layerId);
            }
        }

        for (ResourceLocation layerId : activeLayers) {
            VisualLayer layer = definition(layerId);
            if (layer == null || layer.group() == null
                    || layerId.equals(winnersByGroup.get(layer.group()))) {
                resolvedLayers.add(layerId);
            }
        }
    }

    private boolean wins(ResourceLocation candidateId, VisualLayer candidate,
                         ResourceLocation currentWinnerId) {
        VisualLayer currentWinner = definition(currentWinnerId);
        if (currentWinner == null || candidate.conflictPriority() > currentWinner.conflictPriority()) {
            return true;
        }
        return candidate.conflictPriority() == currentWinner.conflictPriority()
                && candidateId.toString().compareTo(currentWinnerId.toString()) < 0;
    }

    private VisualLayer definition(ResourceLocation layerId) {
        return definitionResolver.apply(layerId);
    }

    private static VisualLayer registeredDefinition(ResourceLocation layerId) {
        IForgeRegistry<VisualLayer> registry = RegistryManager.ACTIVE.getRegistry(
                ModVisualLayers.VISUAL_LAYER_KEY);
        return registry == null ? null : registry.getValue(layerId);
    }
}
