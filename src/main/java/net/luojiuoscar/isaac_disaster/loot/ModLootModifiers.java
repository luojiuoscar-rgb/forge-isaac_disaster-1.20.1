package net.luojiuoscar.isaac_disaster.loot;

import com.mojang.serialization.Codec;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.loot.modifier.ItemPoolLootModifier;
import net.luojiuoscar.isaac_disaster.loot.modifier.SacredOrbLootModifier;
import net.luojiuoscar.isaac_disaster.loot.modifier.TrinketEnchantmentLootModifier;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, IsaacDisaster.MOD_ID);

    public static void register(IEventBus eventBus) {
        LOOT_MODIFIER_SERIALIZERS.register(eventBus);
    }


    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> TRINKET_ENCHANTMENT =
            LOOT_MODIFIER_SERIALIZERS.register("trinket_enchantment", () -> TrinketEnchantmentLootModifier.CODEC);
    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ITEM_POOL =
            LOOT_MODIFIER_SERIALIZERS.register("item_pool", () -> ItemPoolLootModifier.CODEC);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> SACRED_ORB =
            LOOT_MODIFIER_SERIALIZERS.register("sacred_orb", () -> SacredOrbLootModifier.CODEC);


}
