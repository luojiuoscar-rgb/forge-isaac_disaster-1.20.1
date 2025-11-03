package net.luojiuoscar.isaac_disaster.datagen;

import net.luojiuoscar.isaac_disaster.loot.modifier.ItemPoolLootModifier;
import net.luojiuoscar.isaac_disaster.loot.modifier.SacredOrbLootModifier;
import net.luojiuoscar.isaac_disaster.loot.modifier.TrinketEnchantmentLootModifier;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.LootModifier;

import java.util.LinkedHashMap;
import java.util.Map;

public class LootModifierManager {
    private static final LinkedHashMap<String, LootModifier> MODIFIERS = new LinkedHashMap<>();

    public static void register(String name, LootModifier modifier) {
        MODIFIERS.put(name, modifier);
    }

    public static Map<String, LootModifier> getAll() {
        return MODIFIERS;
    }

    static {
        register("item_pool", new ItemPoolLootModifier(new LootItemCondition[0]));
        register("sacred_orb", new SacredOrbLootModifier(new LootItemCondition[0]));
        register("trinket_enchantment", new TrinketEnchantmentLootModifier(new LootItemCondition[0]));



    }
}
