package net.luojiuoscar.isaac_disaster.loot;

import com.mojang.serialization.Codec;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.loot.modifier.*;
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

    // item
    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ITEM_POOL =
            LOOT_MODIFIER_SERIALIZERS.register("item_pool", () -> ItemPoolLootModifier.CODEC);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> SACRED_ORB =
            LOOT_MODIFIER_SERIALIZERS.register("sacred_orb", () -> SacredOrbLootModifier.CODEC);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> AFTER_ITEM_SPAWNED =
            LOOT_MODIFIER_SERIALIZERS.register("after_item_spawned", () -> AfterItemSpawnedModifier.CODEC);

    // trinket
    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> TRINKET_ENCHANTMENT =
            LOOT_MODIFIER_SERIALIZERS.register("trinket_enchantment", () -> TrinketEnchantmentLootModifier.CODEC);

    // pickup
    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> CHEST_LOOT =
            LOOT_MODIFIER_SERIALIZERS.register("chest_loot", () -> ChestLootModifier.CODEC);
    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> SACK_HEAD =
            LOOT_MODIFIER_SERIALIZERS.register("sack_head", () -> SackheadLootModifier.CODEC);
    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> MITRE =
            LOOT_MODIFIER_SERIALIZERS.register("mitre", () -> MitreLootModifier.CODEC);
    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> DAEMONS_TAIL =
            LOOT_MODIFIER_SERIALIZERS.register("daemons_tail", () -> DaemonsTailLootModifier.CODEC);
}
