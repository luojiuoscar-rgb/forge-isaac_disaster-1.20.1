package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.luojiuoscar.isaac_disaster.event.custom.misc.GeneralLootModifyEvent;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.item.ItemStack;

public class ContractFromBelow implements IAbilityEffect {
    public static int calculateStackCount(int originalCount, int amplifier, int maxStackSize) {
        if (maxStackSize <= 1) return originalCount;
        int multipliedCount = originalCount * (amplifier + 1);
        return Math.min(multipliedCount, maxStackSize);
    }

    public static double getCancellationChance(int stacks) {
        return 0.5D * Math.pow(0.666D, stacks);
    }

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.get(ContextKeys.EVENT) instanceof GeneralLootModifyEvent event)) return false;

        int amplifier = context.getOrDefault(ContextKeys.AMPLIFIER, 1D).intValue();
        if (amplifier <= 0) return false;

        ObjectArrayList<ItemStack> modified = new ObjectArrayList<>();
        double cancellationChance = getCancellationChance(amplifier);
        for (ItemStack stack : event.getObjectArrayList()) {
            if (stack.getMaxStackSize() <= 1) {
                modified.add(stack);
            } else if (event.getLootContext().getRandom().nextDouble() >= cancellationChance) {
                ItemStack copiedStack = stack.copy();
                copiedStack.setCount(calculateStackCount(stack.getCount(), amplifier, stack.getMaxStackSize()));
                modified.add(copiedStack);
            }
        }

        event.setObjectArrayList(modified);
        return true;
    }
}
