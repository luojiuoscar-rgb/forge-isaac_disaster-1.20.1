package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.block.block_entity.PedestalBlockEntity;
import net.luojiuoscar.isaac_disaster.manager.data.BlockData;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public final class EternalD6 implements IAbilityEffect {
    @Override
    @SuppressWarnings("resource")
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        ServerLevel level = player.serverLevel();
        int amplifier = Math.max(1, context.getOrDefault(ContextKeys.AMPLIFIER, 1.).intValue());
        for (int activation = 0; activation < amplifier; activation++) {
            activate(player.getRandom(),
                    () -> ModExecutableEffects.D6.get().apply(context),
                    () -> D6.forEachNearbyDisplay(player, (pos, display) -> {
                        display.clearItemDisplayList();
                        display.setItemDisplay(ItemStack.EMPTY);
                        if (display instanceof PedestalBlockEntity pedestal) pedestal.setGenerated(true);
                        BlockData.get(level).removeItemBlock(pos);
                        var blockEntity = level.getBlockEntity(pos);
                        if (blockEntity != null) {
                            blockEntity.setChanged();
                            level.sendBlockUpdated(pos, blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
                        }
                    }));
        }
        return true;
    }

    static void activate(RandomSource random, Runnable reroll, Runnable clear) {
        if (random.nextFloat() < 0.25f) clear.run();
        else reroll.run();
    }
}
