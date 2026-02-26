package net.luojiuoscar.isaac_disaster.registries.trigger_module;

import net.luojiuoscar.isaac_disaster.event.custom.attack.*;
import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.BulletTickEvent;
import net.luojiuoscar.isaac_disaster.event.custom.misc.RightClickTickEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.level.BlockEvent;

import java.util.Set;

public interface ITriggerModule {

    Set<TriggerCategory> getTriggerType();

    default double getLuck(LivingEntity entity){
        AttributeInstance instance = entity.getAttribute(Attributes.LUCK);
        return instance == null ? 0.0 : instance.getValue();
    }

    default double getPriority() {return 0; }

    /** 每一次获取模块的时候都会触发 */
    default void onAdded(LivingEntity entity, TriggerModuleQueue queue){}

    /** 每一次失去该模块的时候都会触发 */
    default void onRemove(LivingEntity entity, TriggerModuleQueue queue){}

    /** 获取攻击上下文 */
    default void getAttackContext(GetAttackContextEvent event, int stacks, TriggerModuleQueue queue) {}

    /** 攻击之前 */
    default void beforePerformAttack(BeforePerformAttackEvent event, int stacks, TriggerModuleQueue queue) {}

    /** 子弹命中实体 - 前 */
    default void beforeBulletHitEntity(IsaacAttackBeforeHitEntityEvent event, int stacks, TriggerModuleQueue queue) {}

    /** 子弹命中实体 - 后 */
    default void afterBulletHitEntity(IsaacAttackAfterHitEvent event, int stacks, TriggerModuleQueue queue) {}

    /** 造成伤害 */
    default void onHitEntity(LivingAttackEvent event, int stacks, TriggerModuleQueue queue) {}

    /** 杀死实体 */
    default void onKilledEntity(LivingDeathEvent event, int stacks, TriggerModuleQueue queue) {}

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

    /** 每tick触发，只有Bullet类子弹会触发 */
    default void onBulletTick(BulletTickEvent event, int stacks, TriggerModuleQueue queue){}

    /** 按住右键的时候触发 */
    default void onRightClickTick(RightClickTickEvent event, int stacks, TriggerModuleQueue queue){}

    /** 捡起物品的时候触发 */
    default void onPickupItem(EntityItemPickupEvent event, int stacks, TriggerModuleQueue queue) {}


}
