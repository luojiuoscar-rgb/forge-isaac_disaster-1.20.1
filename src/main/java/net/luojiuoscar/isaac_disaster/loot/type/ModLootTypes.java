package net.luojiuoscar.isaac_disaster.loot.type;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModLootTypes {
    public static final DeferredRegister<LootPoolEntryType> LOOT_ENTRIES =
            DeferredRegister.create(BuiltInRegistries.LOOT_POOL_ENTRY_TYPE.key(), IsaacDisaster.MOD_ID);

    public static final RegistryObject<LootPoolEntryType> COIN_ENTRY =
            LOOT_ENTRIES.register("coin_entry",
                    () -> new LootPoolEntryType(new CoinLootEntry.Serializer()));

    public static void register(net.minecraftforge.eventbus.api.IEventBus bus) {
        LOOT_ENTRIES.register(bus);
    }
}
