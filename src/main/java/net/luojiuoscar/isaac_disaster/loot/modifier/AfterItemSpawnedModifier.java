package net.luojiuoscar.isaac_disaster.loot.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.luojiuoscar.isaac_disaster.item.item.IsaacItem;
import net.luojiuoscar.isaac_disaster.loot.LootGenerationContext;
import net.luojiuoscar.isaac_disaster.loot.LootGenerationMode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class AfterItemSpawnedModifier extends LootModifier {
    private static final EnumSet<LootGenerationMode> SUPPORTED_MODES =
            EnumSet.of(LootGenerationMode.NATURAL_DROP, LootGenerationMode.SPAWN_DROP);

    public static final Codec<AfterItemSpawnedModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst)
            .apply(inst, AfterItemSpawnedModifier::new));

    public AfterItemSpawnedModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> objectArrayList, LootContext lootContext) {
        if (!SUPPORTED_MODES.contains(LootGenerationContext.currentMode())) return objectArrayList;
        if (objectArrayList.isEmpty()) return objectArrayList;

        ResourceLocation tableId = lootContext.getQueriedLootTableId();
        ItemStack stack = objectArrayList.get(0);

        if (!(stack.getItem() instanceof IsaacItem) || objectArrayList.size() > 1 ||
                !tableId.getNamespace().equals("isaac_disaster") || !tableId.getPath().startsWith("pools/item/"))
            return objectArrayList;

        IsaacItem.setPool(stack, tableId.toString());

        // 返回最终结果
        return ObjectArrayList.of(stack);
    }


    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
