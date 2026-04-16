package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.luojiuoscar.isaac_disaster.event.custom.misc.GeneralLootModifyEvent;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class TransformLootTypesToAnother implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.get(ContextKeys.EVENT) instanceof GeneralLootModifyEvent event)) return false;
        var objectArrayList = event.getObjectArrayList();
        var lootContext = event.getLootContext();

        if (objectArrayList.isEmpty()) return true;

        ServerPlayer player = null;
        if (lootContext.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof ServerPlayer thisPlayer) {
            player = thisPlayer;
        } else if (lootContext.getParamOrNull(LootContextParams.KILLER_ENTITY) instanceof ServerPlayer killerPlayer) {
            player = killerPlayer;
        }
        if (player == null) return true;

        RandomSource rand = lootContext.getRandom();
        ObjectArrayList<ItemStack> newList = new ObjectArrayList<>();

        List<Item> originalList = getOriginalItemType();
        List<Item> transformedList = getTransformedItemType();
        Class<? extends Item> instanceType = getOriginalInstanceType();

        for (ItemStack stack : objectArrayList) {
            // instanceof 判断（优先级最高）
            if (instanceType != null && instanceType.isInstance(stack.getItem())) {
                int originalCount = stack.getCount();
                int count = originalCount;

                Item transformedItem = transformedList.get(transformedList.size() - 1);

                int transformedCount = 0;

                for (int j = 0; j < originalCount; j++) {
                    if (rand.nextDouble() < getProbability(context)) {
                        transformedCount++;
                        count--;
                    }
                }

                stack.setCount(count);

                if (count != 0) {
                    newList.add(stack);
                }

                if (transformedCount > 0) {
                    ItemStack newStack = new ItemStack(transformedItem);
                    newStack.setCount(transformedCount);
                    newList.add(newStack);
                }

                continue;
            }


            // original List判断
            int index = originalList.indexOf(stack.getItem());

            if (index != -1) {
                int originalCount = stack.getCount();
                int count = originalCount;

                // 获取对应的 transformed item
                Item transformedItem;
                if (index < transformedList.size()) {
                    transformedItem = transformedList.get(index);
                } else {
                    transformedItem = transformedList.get(transformedList.size() - 1);
                }

                int transformedCount = 0;

                for (int j = 0; j < originalCount; j++) {
                    if (rand.nextDouble() < getProbability(context)) {
                        transformedCount++;
                        count--;
                    }
                }

                stack.setCount(count);

                if (count != 0) {
                    newList.add(stack);
                }

                if (transformedCount > 0) {
                    ItemStack newStack = new ItemStack(transformedItem);
                    newStack.setCount(transformedCount);
                    newList.add(newStack);
                }

            } else {
                newList.add(stack);
            }
        }

        event.setObjectArrayList(newList);

        return true;
    }

    abstract protected double getProbability(ExecutableEffectContext context);
    /** 转化后的物品和transformedItemType的index一一对应。如果超出索引，则默认使用transformed列表的最后一位 */
    abstract protected List<Item> getOriginalItemType();
    abstract protected List<Item> getTransformedItemType();

    /** 通过这种方式转化的物品默认使用transformedItemType的最后一位
     * 使用这种判断方法会跳过originalType的判断*/
    @Nullable
    protected Class<? extends Item> getOriginalInstanceType() {
        return null;
    }
}
