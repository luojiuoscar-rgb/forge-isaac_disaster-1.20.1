package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl;

import net.luojiuoscar.isaac_disaster.event.custom.attack.GetAttackContextEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackHitBlockEvent;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerCategory;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public class Terra implements ITriggerModule {
    @Override
    public Set<TriggerCategory> getTriggerType() {
        return Set.of(
                TriggerCategory.GET_ATTACK_CONTEXT,
                TriggerCategory.BULLET_HIT_BLOCK
        );
    }

    @Override
    public void getAttackContext(GetAttackContextEvent event, int stacks, TriggerModuleQueue queue) {
            for (AttackContext context : event.getContexts()){

                context.addTriggerModule(ModTriggerModule.TERRA.getId(), stacks);
        }
    }

    @Override
    public void onBulletHitBlock(IsaacAttackHitBlockEvent event, int stacks, TriggerModuleQueue queue) {
        BlockPos pos = event.getHitResult().getBlockPos();
        Level level = event.getSource().level();
        BlockState state = level.getBlockState(pos);

        // 非气体、液体；硬度在范围内
        if (!state.getCollisionShape(level, pos).isEmpty()){

            var destroySpeed = state.getDestroySpeed(level, pos);

            if (destroySpeed<= stacks * 16 && destroySpeed > 0){
                level.destroyBlock(pos, true);

            }
        }
    }
}
