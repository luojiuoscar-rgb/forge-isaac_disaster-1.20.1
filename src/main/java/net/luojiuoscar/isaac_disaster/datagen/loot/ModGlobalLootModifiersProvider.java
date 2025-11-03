package net.luojiuoscar.isaac_disaster.datagen.loot;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.datagen.LootModifierManager;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.LootModifier;

import java.util.Map;

public class ModGlobalLootModifiersProvider extends GlobalLootModifierProvider {
    public ModGlobalLootModifiersProvider(PackOutput output) {
        super(output, IsaacDisaster.MOD_ID);
    }


    @Override
    protected void start() {
        for (Map.Entry<String, LootModifier> entry : LootModifierManager.getAll().entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
    }

}
