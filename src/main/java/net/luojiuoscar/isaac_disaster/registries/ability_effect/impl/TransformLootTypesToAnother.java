package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.luojiuoscar.isaac_disaster.event.custom.misc.GeneralLootModifyEvent;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class TransformLootTypesToAnother implements IAbilityEffect {
    @Override
    public TriggerType getRequiredTriggerType() {
        return ModTriggerTypes.LOOT;
    }

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.get(ContextKeys.EVENT) instanceof GeneralLootModifyEvent event)) return false;
        if (!(event.getEntity() instanceof ServerPlayer)) return true;
        var source = event.getObjectArrayList();
        if (source.isEmpty()) return true;
        var random = event.getLootContext().getRandom();
        var output = new ObjectArrayList<ItemStack>();
        var originals = getOriginalItemType();
        var replacements = getTransformedItemType();
        var instanceType = getOriginalInstanceType();
        for (ItemStack stack : source) {
            Item replacement;
            if (instanceType != null && instanceType.isInstance(stack.getItem())) {
                replacement = replacements.get(replacements.size() - 1);
            } else {
                int index = originals.indexOf(stack.getItem());
                if (index < 0) {
                    output.add(stack);
                    continue;
                }
                replacement = replacements.get(Math.min(index, replacements.size() - 1));
            }
            int transformed = 0;
            for (int i = 0; i < stack.getCount(); i++) {
                if (random.nextDouble() < getProbability(context)) transformed++;
            }
            ItemStack converted = transformed == 0 ? ItemStack.EMPTY : createTransformedStack(stack, replacement, transformed);
            stack.setCount(stack.getCount() - transformed);
            addStack(output, stack);
            addStack(output, converted);
        }
        event.setObjectArrayList(output);
        return true;
    }

    protected abstract double getProbability(ExecutableEffectContext context);
    protected abstract List<Item> getOriginalItemType();
    protected abstract List<Item> getTransformedItemType();

    /** Override when conversion needs a different quantity or must retain source stack data. */
    protected ItemStack createTransformedStack(ItemStack original, Item replacement, int count) {
        return new ItemStack(replacement, count);
    }

    private static void addStack(ObjectArrayList<ItemStack> output, ItemStack stack) {
        int remaining = stack.getCount();
        while (remaining > 0) {
            int amount = Math.min(remaining, stack.getMaxStackSize());
            output.add(stack.copyWithCount(amount));
            remaining -= amount;
        }
    }

    /** Instance matches take precedence and use the last replacement in the list. */
    @Nullable
    protected Class<? extends Item> getOriginalInstanceType() {
        return null;
    }
}
