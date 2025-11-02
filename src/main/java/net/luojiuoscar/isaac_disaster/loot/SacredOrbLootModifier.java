package net.luojiuoscar.isaac_disaster.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.luojiuoscar.isaac_disaster.item.item.IsaacItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

public class SacredOrbLootModifier extends LootModifier {
    public static final ThreadLocal<Boolean> IS_GENERATING = ThreadLocal.withInitial(() -> false);

    public static final Codec<SacredOrbLootModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst)
            .apply(inst, SacredOrbLootModifier::new));

    public SacredOrbLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> objectArrayList, LootContext lootContext) {
        if (IS_GENERATING.get() || objectArrayList.isEmpty()) return objectArrayList;

        ResourceLocation tableId = lootContext.getQueriedLootTableId();
        RandomSource rand = lootContext.getRandom();
        ItemStack stack = objectArrayList.get(0);

        if (!(stack.getItem() instanceof IsaacItem item) || objectArrayList.size() > 1 ||
                !tableId.getNamespace().equals("isaac_disaster") || !tableId.getPath().startsWith("pools/"))
            return objectArrayList;

        LootTable lootTable = lootContext.getLevel().getServer().getLootData().getLootTable(tableId);

        // 防止递归调用
        SacredOrbLootModifier.IS_GENERATING.set(true);
        // 从当前 stack 取出等级信息
        int level = item.getItemLevel();
        int attempts = 0;
        try {
            while (level <= 2 && attempts < 20) {
                ObjectArrayList<ItemStack> generated = new ObjectArrayList<>();
                lootTable.getRandomItems(lootContext, generated::add);

                if (!generated.isEmpty()) {
                    stack = generated.get(0);
                }

                if (stack.getItem() instanceof IsaacItem newItem) {
                    level = newItem.getItemLevel();
                    if ((level == 2 && rand.nextDouble() < 0.5) || level > 2) {
                        break;
                    }
                }
                attempts++;
            }
        } finally {
            SacredOrbLootModifier.IS_GENERATING.set(false);
        }

        // 返回最终结果
        return ObjectArrayList.of(stack);
    }


    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
