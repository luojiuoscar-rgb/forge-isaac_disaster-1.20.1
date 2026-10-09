package net.luojiuoscar.isaac_disaster.registries.charge_bar;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModChargeBars {
    private ModChargeBars() {}
    public static final ResourceKey<Registry<ChargeBarType>> CHARGE_BAR_KEY = ResourceKey.createRegistryKey(
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "charge_bar"));
    public static final DeferredRegister<ChargeBarType> CHARGE_BAR_REGISTRY =
            DeferredRegister.create(CHARGE_BAR_KEY, IsaacDisaster.MOD_ID);

    /** Integer.MAX_VALUE is reserved for the existing attack indicator; extensions use lower priorities. */
    public static final RegistryObject<ChargeBarType> ATTACK_CHARGE = CHARGE_BAR_REGISTRY.register(
            "attack_charge", () -> new ChargeBarType(Integer.MAX_VALUE,
                    ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID,
                            "textures/hud/charge/charge_ring_base.png"), 0xFF7DE000));

    public static final RegistryObject<ChargeBarType> REVELATION = CHARGE_BAR_REGISTRY.register(
            "revelation", () -> new ChargeBarType(Integer.MAX_VALUE - 1,
                    ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID,
                            "textures/hud/charge/revelation_charge_base.png"), 0xFFFFD75A));
}
