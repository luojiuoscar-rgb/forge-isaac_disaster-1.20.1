package net.luojiuoscar.isaac_disaster.loot.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.item.IsaacItem;
import net.luojiuoscar.isaac_disaster.loot.LootContextHelper;
import net.luojiuoscar.isaac_disaster.loot.LootGenerationContext;
import net.luojiuoscar.isaac_disaster.loot.LootGenerationMode;
import net.luojiuoscar.isaac_disaster.loot.TempPoolManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class SacredOrbLootModifier extends LootModifier {
    private static final EnumSet<LootGenerationMode> SUPPORTED_MODES =
            EnumSet.of(LootGenerationMode.NATURAL_DROP, LootGenerationMode.SPAWN_DROP);

    public static final Codec<SacredOrbLootModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst)
            .apply(inst, SacredOrbLootModifier::new));

    public SacredOrbLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> objectArrayList, LootContext lootContext) {
        if (!SUPPORTED_MODES.contains(LootGenerationContext.currentMode())) return objectArrayList;
        if (objectArrayList.isEmpty()) return objectArrayList;

        ServerPlayer player = LootContextHelper.findResponsiblePlayer(lootContext);
        if (player == null) return objectArrayList;

        ResourceLocation tableId = lootContext.getQueriedLootTableId();
        RandomSource rand = lootContext.getRandom();
        ItemStack stack = objectArrayList.get(0);

        if (!(stack.getItem() instanceof IsaacItem item) || objectArrayList.size() > 1 ||
                !tableId.getNamespace().equals("isaac_disaster") || !tableId.getPath().startsWith("pools/item/"))
            return objectArrayList;

        // sacred orb
        if (PlayerHelper.getItemCount(ItemId.SACRED_ORB.getId(), player) == 0) return objectArrayList;

        LootPool tempPool = TempPoolManager.get(player);
        if (tempPool == null) return objectArrayList;

        ObjectArrayList<ItemStack> generated = new ObjectArrayList<>();

        // 从当前 stack 取出等级信息
        int level = item.getLevel();
        int attempts = 0;
        while (level <= 2 && attempts < 20) {
            generated.clear();
            // 生成物品
            tempPool.addRandomItems(generated::add, lootContext);

            if (!generated.isEmpty()) {
                stack = generated.get(0);
            }

            if (stack.getItem() instanceof IsaacItem newItem) {
                level = newItem.getLevel();
                if ((level == 2 && rand.nextDouble() < 0.5) || level > 2) {
                    break;
                }
            }
            attempts++;
        }

        // 返回最终结果
        return ObjectArrayList.of(stack);
    }


    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
