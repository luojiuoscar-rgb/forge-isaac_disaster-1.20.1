package net.luojiuoscar.isaac_disaster.registries.ability.set;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.SetId;
import net.luojiuoscar.isaac_disaster.registries.ability.set.impl.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModSetAbility {
    public static final ResourceKey<Registry<SetAbility>> SET_ABILITY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "set_ability"));

    public static final DeferredRegister<SetAbility> SET_ABILITY_REGISTRY =
            DeferredRegister.create(SET_ABILITY_KEY, IsaacDisaster.MOD_ID);

    public static final RegistryObject<SetAbility> ADULT =
            SET_ABILITY_REGISTRY.register("adult", () -> new Adult(SetId.ADULT.getId()));

    public static final RegistryObject<SetAbility> BOOK =
            SET_ABILITY_REGISTRY.register("book", () -> new Book(SetId.BOOK.getId()));

    public static final RegistryObject<SetAbility> FUN_GUY =
            SET_ABILITY_REGISTRY.register("fun_guy", () -> new FunGuy(SetId.FUN_GUY.getId()));

    public static final RegistryObject<SetAbility> SPUN =
            SET_ABILITY_REGISTRY.register("spun", () -> new Spun(SetId.SPUN.getId()));

}
