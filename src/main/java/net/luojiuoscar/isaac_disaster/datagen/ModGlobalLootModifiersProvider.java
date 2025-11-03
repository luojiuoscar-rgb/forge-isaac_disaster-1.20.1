package net.luojiuoscar.isaac_disaster.datagen;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.loot.modifier.SacredOrbLootModifier;
import net.luojiuoscar.isaac_disaster.loot.modifier.TrinketEnchantmentLootModifier;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;

public class ModGlobalLootModifiersProvider extends GlobalLootModifierProvider {
    public ModGlobalLootModifiersProvider(PackOutput output) {
        super(output, IsaacDisaster.MOD_ID);
    }

    @Override
    protected void start() {
        add("trinket_enchantment", new TrinketEnchantmentLootModifier(new LootItemCondition[0]));
        add("item_pool", new SacredOrbLootModifier(new LootItemCondition[0]));

        add("sacred_orb", new SacredOrbLootModifier(new LootItemCondition[0]));

    }
}
