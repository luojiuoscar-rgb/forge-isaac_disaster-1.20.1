package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl;

import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerCategory;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.level.BlockEvent;

import java.util.Set;

public class LuckyRock implements ITriggerModule {
    @Override
    public Set<TriggerCategory> getTriggerType() {
        return Set.of(TriggerCategory.BREAK_BLOCK);
    }

    @Override
    public void onBlockBreak(BlockEvent.BreakEvent event, int stacks, TriggerModuleQueue queue) {
        Player player = event.getPlayer();

        if (player.getRandom().nextDouble() < Math.min(0.1, 0.05 * stacks) && player.level() instanceof ServerLevel level){
            BlockPos pos = event.getPos();
            BlockState state = level.getBlockState(pos);

            if (state.is(Tags.Blocks.STONE) || state.is(Tags.Blocks.COBBLESTONE)) {
                LevelHelper.spawnMoney(level, pos.getCenter(), 1);
            }
        }
    }


}
