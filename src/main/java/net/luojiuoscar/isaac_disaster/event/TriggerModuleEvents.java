package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.entity.EffectModulesProvider;
import net.luojiuoscar.isaac_disaster.entity.tnt.IsaacBomb;
import net.luojiuoscar.isaac_disaster.event.custom.attack.*;
import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.BulletTickEvent;
import net.luojiuoscar.isaac_disaster.event.custom.misc.BeforeTriggerModuleActiveEvent;
import net.luojiuoscar.isaac_disaster.event.custom.misc.GeneralLootModifyEvent;
import net.luojiuoscar.isaac_disaster.event.custom.misc.RightClickTickEvent;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID)
public class TriggerModuleEvents {

    public static void dispatch(ExecutableEffectContext context, TriggerType type) {
        LivingEntity entity = context.getEntity();

        // 获取queue并且触发所有模块
        entity.getCapability(EffectModulesProvider.EFFECT_MODULES).ifPresent(triggerModule -> {
            var queue = triggerModule.getTriggerModules().copy();
            context.set(ContextKeys.TRIGGER_MODULE_QUEUE, queue);

            if (queue == null || queue.isEmpty()) return;

            IForgeRegistry<TriggerModule> reg =
                    RegistryManager.ACTIVE.getRegistry(ModTriggerModule.TRIGGER_MODULE_KEY);
            if (reg == null) return;

            // 触发前事件
            BeforeTriggerModuleActiveEvent preEvent = new BeforeTriggerModuleActiveEvent(context);
            MinecraftForge.EVENT_BUS.post(preEvent);

            queue.lock();

            for (var inst : queue.getQueue()) {
                context.set(ContextKeys.AMPLIFIER, (double) inst.stacks);
                TriggerModule module = reg.getValue(inst.id);
                if (module == null) continue;

                // 调用模块的 fire 方法，触发对应 TriggerType 的 SimpleTrigger
                module.fire(context, type);
            }
        });
    }

    public static void dispatchBullet(ExecutableEffectContext context, TriggerType type){
        context.set(ContextKeys.AMPLIFIER, 1.);
        ModTriggerModule.BULLET_TRIGGER_MODULE.get().fire(context, type);
    }

    @SubscribeEvent
    public static void getAttackContext(GetAttackContextEvent event) {
        LivingEntity entity = event.getPlayer();

        ExecutableEffectContext context = new ExecutableEffectContext(entity);
        context.set(ContextKeys.EVENT, event);
        context.set(ContextKeys.TARGET_POSITION, entity.position());

        dispatch(context, ModTriggerTypes.GET_ATTACK_CONTEXT);
    }

    @SubscribeEvent
    public static void beforePerformAttack(BeforePerformAttackEvent event) {
        LivingEntity entity = event.getEntity();

        ExecutableEffectContext context = new ExecutableEffectContext(entity);
        context.set(ContextKeys.EVENT, event);
        context.set(ContextKeys.TARGET_POSITION, entity.position());

        dispatch(context, ModTriggerTypes.BEFORE_PERFORM_ATTACK);
    }

    @SubscribeEvent
    public static void onHitEntity(LivingAttackEvent event) {
        // restricted damage types
        if (!PlayerHelper.isHitAllowedType(event.getSource())) return;
        if (!(event.getSource().getEntity() instanceof LivingEntity entity)) return;
        ExecutableEffectContext context = new ExecutableEffectContext(entity);
        context.set(ContextKeys.EVENT, event);
        context.set(ContextKeys.DOUBLE, List.of((double) event.getAmount()));

        List<LivingEntity> secondary_entities = new ArrayList<>();
        LivingEntity victim = event.getEntity();
        secondary_entities.add(victim);
        context.set(ContextKeys.SECONDARY_LIVING_ENTITIES, secondary_entities); // 受伤的生物
        context.set(ContextKeys.TARGET_POSITION, victim.position());
        if (event.getSource().getDirectEntity() != null){
            context.set(ContextKeys.ENTITY, List.of(event.getSource().getDirectEntity()));
        }

        dispatch(context, ModTriggerTypes.HIT_ENTITY);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        ExecutableEffectContext context = new ExecutableEffectContext(entity);
        context.set(ContextKeys.EVENT, event);
        context.set(ContextKeys.TARGET_POSITION, entity.position());

        List<LivingEntity> secondary_entities = new ArrayList<>();
        context.set(ContextKeys.SECONDARY_LIVING_ENTITIES, secondary_entities);
        context.set(ContextKeys.DOUBLE, List.of((double) event.getAmount()));
        if (event.getSource().getDirectEntity() != null){
            context.set(ContextKeys.ENTITY, List.of(event.getSource().getDirectEntity()));
        }

        dispatch(context, ModTriggerTypes.ON_HURT);
        if (!PlayerHelper.isSelfDamage(event.getSource())){
            dispatch(context, ModTriggerTypes.ON_HURT_NEGATIVE);
        }else {
            dispatch(context, ModTriggerTypes.ON_HURT_POSITIVE);
        }
    }

    @SubscribeEvent
    public static void beforeBulletHitEntity(IsaacAttackBeforeHitEntityEvent event) {
        IBulletObject bulletObject = event.getBulletObject();

        if (bulletObject.getOwner() == null) return;
        ExecutableEffectContext context = new ExecutableEffectContext(bulletObject.getOwner());
        context.set(ContextKeys.EVENT, event);
        context.set(ContextKeys.BULLET, bulletObject);
        context.set(ContextKeys.TARGET_POSITION, bulletObject.getPosition());
        if (event.getHit().getEntity() instanceof LivingEntity entity){
            context.set(ContextKeys.SECONDARY_LIVING_ENTITIES, List.of(entity));
        }
        context.set(ContextKeys.DOUBLE, List.of(event.getDamage()));

        dispatchBullet(context, ModTriggerTypes.BULLET_HIT_ENTITY_BEFORE);
    }

    @SubscribeEvent
    public static void afterBulletHitEntity(IsaacAttackAfterHitEvent event) {
        IBulletObject bulletObject = event.getBulletObject();

        if (bulletObject.getOwner() == null) return;
        ExecutableEffectContext context = new ExecutableEffectContext(bulletObject.getOwner());
        context.set(ContextKeys.EVENT, event);
        context.set(ContextKeys.BULLET, bulletObject);
        context.set(ContextKeys.TARGET_POSITION, bulletObject.getPosition());
        if (event.getHit().getEntity() instanceof LivingEntity entity){
            context.set(ContextKeys.SECONDARY_LIVING_ENTITIES, List.of(entity));
        }
        context.set(ContextKeys.DOUBLE, List.of(event.getDamage()));

        dispatchBullet(context, ModTriggerTypes.BULLET_HIT_ENTITY_AFTER);
    }

    @SubscribeEvent
    public static void onBulletHitBlock(IsaacAttackHitBlockEvent event) {
        IBulletObject bulletObject = event.getBulletObject();
        if (bulletObject.getOwner() == null) return;

        ExecutableEffectContext context = new ExecutableEffectContext(bulletObject.getOwner());
        context.set(ContextKeys.EVENT, event);
        context.set(ContextKeys.BULLET, bulletObject);
        context.set(ContextKeys.TARGET_POSITION, event.getHitResult().getBlockPos().getCenter());
        context.set(ContextKeys.DOUBLE, List.of((double) bulletObject.getDamage()));

        dispatchBullet(context, ModTriggerTypes.BULLET_HIT_BLOCK);
    }

    /**
     * 死亡事件和击杀事件本质是同一事件，但是触发主体不同
     * */
    @SubscribeEvent
    public static void onKilledEntity(LivingDeathEvent event) {
        Entity attacker = event.getSource().getEntity();
        LivingEntity victim = event.getEntity();


        ExecutableEffectContext death_ctx = new ExecutableEffectContext(victim);
        death_ctx.set(ContextKeys.EVENT, event);
        death_ctx.set(ContextKeys.TARGET_POSITION, victim.position());

        if (!(attacker instanceof LivingEntity entity)) return;

        List<LivingEntity> secondary_entities = new ArrayList<>();
        secondary_entities.add(entity);
        death_ctx.set(ContextKeys.SECONDARY_LIVING_ENTITIES, secondary_entities);

        ExecutableEffectContext kill_ctx = new ExecutableEffectContext(entity);
        kill_ctx.set(ContextKeys.EVENT, event);
        kill_ctx.set(ContextKeys.TARGET_POSITION, victim.position());
        kill_ctx.set(ContextKeys.SECONDARY_LIVING_ENTITIES, secondary_entities);

        dispatch(kill_ctx, ModTriggerTypes.KILL_ENTITY);
        dispatch(death_ctx, ModTriggerTypes.DEATH);
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        ExecutableEffectContext context = new ExecutableEffectContext(player);
        context.set(ContextKeys.EVENT, event);
        context.set(ContextKeys.TARGET_POSITION, player.position());

        dispatch(context, ModTriggerTypes.BREAK_BLOCK);
    }

    @SubscribeEvent
    public static void onBulletTick(BulletTickEvent event) {
        IBulletObject bullet = event.getBullet();
        LivingEntity entity = bullet.getOwner();

        ExecutableEffectContext context = new ExecutableEffectContext(entity);
        context.set(ContextKeys.EVENT, event);
        context.set(ContextKeys.TARGET_POSITION, bullet.getPosition());
        context.set(ContextKeys.BULLET, bullet);

        dispatchBullet(context, ModTriggerTypes.BULLET_TICK);
    }

    @SubscribeEvent
    public static void rightClickTick(RightClickTickEvent event) {
        ServerPlayer player = event.getPlayer();
        ExecutableEffectContext context = new ExecutableEffectContext(player);
        context.set(ContextKeys.EVENT, event);
        InteractionHand hand = player.getUsedItemHand();
        context.set(ContextKeys.HAND, hand);
        context.set(ContextKeys.ITEM, player.getItemInHand(hand).getItem());
        context.set(ContextKeys.TARGET_POSITION, player.position());

        dispatch(context, ModTriggerTypes.RIGHT_CLICK_TICK);
    }

    @SubscribeEvent
    public static void rightClickTick(EntityItemPickupEvent event) {
        LivingEntity entity = event.getEntity();
        ExecutableEffectContext context = new ExecutableEffectContext(entity);
        context.set(ContextKeys.EVENT, event);
        ItemEntity itemEntity = event.getItem();

        context.set(ContextKeys.ITEM, itemEntity.getItem().getItem());
        context.set(ContextKeys.DOUBLE, List.of((double) itemEntity.getItem().getCount()));
        context.set(ContextKeys.TARGET_POSITION, entity.position());
        context.set(ContextKeys.ENTITY, List.of(itemEntity));

        dispatch(context, ModTriggerTypes.PICKUP_ITEM);
    }

    @SubscribeEvent
    public static void onLoot(GeneralLootModifyEvent event) {
        LivingEntity entity = event.getEntity();
        ExecutableEffectContext context = new ExecutableEffectContext(entity);
        context.set(ContextKeys.EVENT, event);
        context.set(ContextKeys.TARGET_POSITION, entity.position());

        dispatch(context, ModTriggerTypes.LOOT);
    }

    /**
     * TNT 相关内容
     */
    @SubscribeEvent
    public static void onTntSpawn(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof IsaacBomb bomb)) return;
        if (bomb.getOwner() == null) return;

        ExecutableEffectContext context = new ExecutableEffectContext(bomb.getOwner());
        context.set(ContextKeys.EVENT, event);
        context.set(ContextKeys.ENTITY, List.of(bomb));
        context.set(ContextKeys.TARGET_POSITION, bomb.position());

        dispatch(context, ModTriggerTypes.TNT_SPAWNED);
    }

    /** 在产生爆炸的时候，根据炸弹自身的TriggerModule来触发对应效果 */
    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (!(event.getExplosion().getDirectSourceEntity() instanceof IsaacBomb bomb)) return;
        if (bomb.getOwner() == null) return;

        ExecutableEffectContext context = new ExecutableEffectContext(bomb.getOwner());
        context.set(ContextKeys.TARGET_POSITION, bomb.position());
        context.set(ContextKeys.ENTITY, List.of(bomb));
        context.set(ContextKeys.SECONDARY_LIVING_ENTITIES,
                event.getAffectedEntities().stream()
                        .filter(LivingEntity.class::isInstance)
                        .map(LivingEntity.class::cast)
                        .toList()
        );

        // 触发对应爆炸效果
        bomb.getCachedEffect().apply(context);
    }


}

