package net.luojiuoscar.isaac_disaster.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.luojiuoscar.isaac_disaster.datapack.init.WorldDatapackBootstrap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.WorldDataConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

/** A newly discovered seed pack must not override packs already shipped with a map. */
@Mixin(MinecraftServer.class)
public abstract class WorldDatapackOrderMixin {
    @ModifyReturnValue(method = "configurePackRepository", at = @At("RETURN"))
    private static WorldDataConfiguration isaac$orderWorldDefaults(WorldDataConfiguration original,
                                                PackRepository repository, DataPackConfig initial,
                                                boolean safeMode, FeatureFlagSet features) {
        if (safeMode) return original;
        // Work on the incoming result so other return-value modifiers can compose with this one.
        var selected = new ArrayList<>(original.dataPacks().getEnabled());
        // Preserve explicit pack ordering after first discovery; admins may reorder using /datapack.
        if (initial.getEnabled().contains(WorldDatapackBootstrap.PACK_ID)
                || initial.getEnabled().contains(WorldDatapackBootstrap.PACK_ID + ".zip")) return original;
        String id = selected.contains(WorldDatapackBootstrap.PACK_ID) ? WorldDatapackBootstrap.PACK_ID
                : WorldDatapackBootstrap.PACK_ID + ".zip";
        if (!selected.remove(id)) return original;
        selected.add(0, id);
        repository.setSelected(selected);
        return new WorldDataConfiguration(
                new DataPackConfig(List.copyOf(repository.getSelectedIds()), original.dataPacks().getDisabled()),
                original.enabledFeatures());
    }
}
