package net.luojiuoscar.isaac_disaster.registries.trigger_module;

import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackAfterHitEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackBeforeHitEntityEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackHitBlockEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.PlayerPerformAttackEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;

import java.util.Set;

public interface ITriggerModule {

    Set<TriggerCategory> getTriggerType();

    default double getLuck(LivingEntity entity){
        return entity.getAttributes().getValue(Attributes.LUCK);
    }

    default double getPriority() {return 0; }

    /** 发射前（玩家攻击时） */
    default void onShoot(PlayerPerformAttackEvent event, int stacks, TriggerModuleQueue queue) {}

    /** 子弹命中实体 - 前 */
    default void beforeBulletHitEntity(IsaacAttackBeforeHitEntityEvent event, int stacks, TriggerModuleQueue queue) {}

    /** 子弹命中实体 - 后 */
    default void afterBulletHitEntity(IsaacAttackAfterHitEvent event, int stacks, TriggerModuleQueue queue) {}

    /** 实体造成伤害 */
    default void onHitEntity(LivingAttackEvent event, int stacks, TriggerModuleQueue queue) {}

    /** 子弹命中方块 */
    default void onBulletHitBlock(IsaacAttackHitBlockEvent event, int stacks, TriggerModuleQueue queue) {}

    /** 受击触发 - 通用 */
    default void onHurt(LivingHurtEvent event, int stacks, TriggerModuleQueue queue) {}

    /** 良性受击触发 - 仅在自伤时触发 */
    default void onHurtPositive(LivingHurtEvent event, int stacks, TriggerModuleQueue queue) {}

    /** 恶性受击触发 - 仅在非自伤时触发 */
    default void onHurtNegative(LivingHurtEvent event, int stacks, TriggerModuleQueue queue) {}

    /** 方块破坏 */
    default void onBlockBreak(BlockEvent.BreakEvent event, int stacks, TriggerModuleQueue queue) {}

    /** 任意事件（扩展用） */
    default void onGeneric(Event event, int stacks, TriggerModuleQueue queue) {}
}
