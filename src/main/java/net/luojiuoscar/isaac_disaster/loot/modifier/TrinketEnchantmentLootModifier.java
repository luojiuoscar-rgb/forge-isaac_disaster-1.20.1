package net.luojiuoscar.isaac_disaster.loot.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

public class TrinketEnchantmentLootModifier extends LootModifier {
    public static final Codec<TrinketEnchantmentLootModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst)
            .apply(inst, TrinketEnchantmentLootModifier::new));

    public TrinketEnchantmentLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> objectArrayList, LootContext lootContext) {
        if (objectArrayList.isEmpty()) return objectArrayList;

        ResourceLocation tableId = lootContext.getQueriedLootTableId();
        RandomSource rand = lootContext.getRandom();
        ItemStack stack = objectArrayList.get(0);

        if (!(stack.getItem() instanceof Trinket) || objectArrayList.size() > 1 ||
                !tableId.getNamespace().equals("isaac_disaster") || !tableId.getPath().startsWith("pools/trinket/"))
            return objectArrayList;

        if (rand.nextDouble() < 0.02){ // 2% chance
            Trinket.setEnchanted(stack, true);
        }

        return objectArrayList;
    }


    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
