package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;

public class LuckyRock implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (context.getEntity() instanceof Player player){
            if (player.getRandom().nextDouble() < Math.min(0.1, 0.05 * context.get(ContextKeys.AMPLIFIER))
                    && player.level() instanceof ServerLevel level){

                Vec3 p = context.get(ContextKeys.TARGET_POSITION);
                if (p == null) return false;

                BlockPos pos = BlockPos.containing(p);
                BlockState state = level.getBlockState(pos);

                if (state.is(Tags.Blocks.STONE) || state.is(Tags.Blocks.COBBLESTONE)) {
                    LevelHelper.spawnMoney(level, pos.getCenter(), 1);
                }
            }
        }

        return true;
    }
}
