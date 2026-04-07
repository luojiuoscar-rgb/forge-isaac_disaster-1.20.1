package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BreakBlockAndDrop implements IAbilityEffect {
    @Override
    public boolean applyEffect(AbilityEffectContext context) {
        Vec3 position = context.get(ContextKeys.TARGET_POSITION);
        int amplifier = context.getOrDefault(ContextKeys.AMPLIFIER, 1.).intValue();
        if (position == null) return false;

        Level level = context.getEntity().level();
        BlockPos pos = BlockPos.containing(position);
        BlockState state = level.getBlockState(pos);

        // 非气体、液体；硬度在范围内
        if (!state.getCollisionShape(level, pos).isEmpty()){

            var destroySpeed = state.getDestroySpeed(level, pos);

            if (destroySpeed <= amplifier * 16 && destroySpeed > 0){
                level.destroyBlock(pos, true);

            }
        }
        return true;
    }
}
