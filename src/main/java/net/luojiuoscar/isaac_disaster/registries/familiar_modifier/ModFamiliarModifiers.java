package net.luojiuoscar.isaac_disaster.registries.familiar_modifier;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.entity.familiar.SwarmFamiliar;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryManager;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import static net.luojiuoscar.isaac_disaster.registries.familiar_modifier.FamiliarModifierEntry.Parameter.*;

public final class ModFamiliarModifiers {
    public static final ResourceKey<Registry<FamiliarModifierEntry>> FAMILIAR_MODIFIER_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "familiar_modifier"));
    public static final DeferredRegister<FamiliarModifierEntry> FAMILIAR_MODIFIER_REGISTRY =
            DeferredRegister.create(FAMILIAR_MODIFIER_KEY, IsaacDisaster.MOD_ID);

    public static final RegistryObject<FamiliarModifierEntry> BFFS_ENHANCEMENT = FAMILIAR_MODIFIER_REGISTRY.register(
            "bffs/enhancement", () -> new FamiliarModifierEntry(ENHANCEMENT, 1, familiar -> !(familiar instanceof SwarmFamiliar)));
    public static final RegistryObject<FamiliarModifierEntry> BFFS_VISUAL_SCALE = FAMILIAR_MODIFIER_REGISTRY.register(
            "bffs/visual_scale", () -> new FamiliarModifierEntry(VISUAL_SCALE, 0.25, familiar -> !(familiar instanceof SwarmFamiliar)));
    public static final RegistryObject<FamiliarModifierEntry> HIVE_MIND_ENHANCEMENT = FAMILIAR_MODIFIER_REGISTRY.register(
            "hive_mind/enhancement", () -> new FamiliarModifierEntry(ENHANCEMENT, 1, SwarmFamiliar.class::isInstance));
    public static final RegistryObject<FamiliarModifierEntry> HIVE_MIND_VISUAL_SCALE = FAMILIAR_MODIFIER_REGISTRY.register(
            "hive_mind/visual_scale", () -> new FamiliarModifierEntry(VISUAL_SCALE, 0.5, SwarmFamiliar.class::isInstance));

    private ModFamiliarModifiers() { }

    @Nullable
    public static FamiliarModifierEntry get(ResourceLocation id) {
        var registry = RegistryManager.ACTIVE.getRegistry(FAMILIAR_MODIFIER_KEY);
        return registry == null ? null : registry.getValue(id);
    }
}
