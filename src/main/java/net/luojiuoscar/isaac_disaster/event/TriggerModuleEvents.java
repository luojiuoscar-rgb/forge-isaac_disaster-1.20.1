package net.luojiuoscar.isaac_disaster.event;

import java.util.ArrayList;
import java.util.List;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.entity.EffectModulesProvider;
import net.luojiuoscar.isaac_disaster.system.explosion.BombEffectSource;
import net.luojiuoscar.isaac_disaster.event.custom.attack.*;
import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.*;
import net.luojiuoscar.isaac_disaster.event.custom.misc.*;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectBatch;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.rule.TriggerModuleRuleContext;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.rule.TriggerModuleRules;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.rule.TriggerModuleSnapshot;
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
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID)
public class TriggerModuleEvents {

    public static void dispatch(ExecutableEffectContext context, TriggerType type) {
        LivingEntity entity = context.getEntity();

        // 获取queue并且触发所有模块
        entity.getCapability(EffectModulesProvider.EFFECT_MODULES).ifPresent(triggerModule -> {
            TriggerModuleSnapshot snapshot = triggerModule.getTriggerModules().snapshot();
            if (snapshot.isEmpty()) return;

            IForgeRegistry<TriggerModule> reg =
                    RegistryManager.ACTIVE.getRegistry(ModTriggerModules.TRIGGER_MODULE_KEY);
            if (reg == null) return;

            // 触发前事件
            BeforeTriggerModuleActiveEvent preEvent = new BeforeTriggerModuleActiveEvent(context);
            MinecraftForge.EVENT_BUS.post(preEvent);

            var batch = new ExecutableEffectBatch();
            for (var inst : snapshot.modules()) {
                TriggerModule module = reg.getValue(inst.id());
                if (module == null) continue;

                ExecutableEffectContext moduleContext = context.copy(null);
                moduleContext.set(ContextKeys.AMPLIFIER, (double) inst.stacks());
                TriggerModuleRuleContext ruleContext = new TriggerModuleRuleContext(inst, type, moduleContext, snapshot);
                batch.add(module.getTriggers(moduleContext), moduleContext, type,
                        () -> TriggerModuleRules.allows(ruleContext));
            }
            batch.fire();
        });
    }

    public static void dispatchBullet(ExecutableEffectContext context, TriggerType type){
        context.set(ContextKeys.AMPLIFIER, 1.);
        ModTriggerModules.BULLET_TRIGGER_MODULE.get().fire(context, type);
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

        dispatch(context, ModTriggerTypes.HIT_ENTITY_RESTRICTED);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.isCanceled() || !Float.isFinite(event.getAmount()) || event.getAmount() <= 0) return;
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker) || attacker.level().isClientSide) return;
        ExecutableEffectContext context = new ExecutableEffectContext(attacker);
        context.set(ContextKeys.EVENT, event);
        context.set(ContextKeys.DOUBLE, List.of((double) event.getAmount()));
        context.set(ContextKeys.TARGET_POSITION, event.getEntity().position());
        context.set(ContextKeys.SECONDARY_LIVING_ENTITIES, List.of(event.getEntity()));
        Entity direct = event.getSource().getDirectEntity();
        if (direct != null) context.set(ContextKeys.ENTITY, List.of(direct));
        dispatch(context, ModTriggerTypes.DEAL_DAMAGE);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        Entity sourceEntity = event.getSource().getEntity();
        Entity directEntity = event.getSource().getDirectEntity();
        ExecutableEffectContext context = new ExecutableEffectContext(entity);
        context.set(ContextKeys.EVENT, event);
        context.set(ContextKeys.TARGET_POSITION, entity.position());

        List<LivingEntity> secondary_entities = new ArrayList<>();
        if (sourceEntity instanceof LivingEntity livingSource) {
            secondary_entities.add(livingSource);
        }
        context.set(ContextKeys.SECONDARY_LIVING_ENTITIES, secondary_entities);
        context.set(ContextKeys.DOUBLE, List.of((double) event.getAmount()));
        if (directEntity != null){
            context.set(ContextKeys.ENTITY, List.of(directEntity));
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
        context.set(ContextKeys.TARGET_POSITION, event.getHit().getLocation());
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
        context.set(ContextKeys.TARGET_POSITION, event.getHit().getLocation());
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

    @SubscribeEvent
    public static void onBulletStickStart(BulletStickStartEvent event) {
        dispatchStick(event, ModTriggerTypes.BULLET_STICK_START);
    }

    @SubscribeEvent
    public static void onBulletStickTick(BulletStickTickEvent event) {
        dispatchStick(event, ModTriggerTypes.BULLET_STICK_TICK);
    }

    @SubscribeEvent
    public static void onBulletStickEnd(BulletStickEndEvent event) {
        dispatchStick(event, ModTriggerTypes.BULLET_STICK_END);
    }

    private static void dispatchStick(BulletStickEvent event, TriggerType type) {
        var stick = event.getStickContext();
        IBulletObject bullet = event.getBulletObject();
        LivingEntity owner = bullet.getOwner();
        if (owner == null) return;

        ExecutableEffectContext context = new ExecutableEffectContext(owner);
        context.set(ContextKeys.EVENT, event);
        context.set(ContextKeys.BULLET, bullet);
        context.set(ContextKeys.BULLET_STICK, stick);
        context.set(ContextKeys.TARGET_POSITION, stick.position());
        LivingEntity target = stick.target();
        if (target != null && target.isAlive() && !target.isRemoved() && target.level() == owner.level()) {
            context.set(ContextKeys.SECONDARY_LIVING_ENTITIES, List.of(target));
        }
        dispatchBullet(context, type);
    }

    @SubscribeEvent
    public static void onBulletEndOfLife(TearBulletEndOfLifeEvent event) {
        IBulletObject bullet = event.getBulletObject();

        if (bullet.getOwner() == null) return;
        ExecutableEffectContext context = new ExecutableEffectContext(bullet.getOwner());
        context.set(ContextKeys.EVENT, event);
        context.set(ContextKeys.BULLET, bullet);
        context.set(ContextKeys.TARGET_POSITION, bullet.getPosition());

        dispatchBullet(context, ModTriggerTypes.BULLET_END_OF_LIFE);
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

        List<LivingEntity> secondary_entities = new ArrayList<>();
        if (attacker instanceof LivingEntity entity) {
            secondary_entities.add(entity);
        }
        death_ctx.set(ContextKeys.SECONDARY_LIVING_ENTITIES, secondary_entities);

        if (attacker instanceof LivingEntity entity) {
            ExecutableEffectContext kill_ctx = new ExecutableEffectContext(entity);
            kill_ctx.set(ContextKeys.EVENT, event);
            kill_ctx.set(ContextKeys.TARGET_POSITION, victim.position());
            kill_ctx.set(ContextKeys.SECONDARY_LIVING_ENTITIES, secondary_entities);
            dispatch(kill_ctx, ModTriggerTypes.KILL_ENTITY);
        }
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
        if (entity == null) return;

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
    public static void onPickupItem(EntityItemPickupEvent event) {
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
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof BombEffectSource bomb)) return;
        if (bomb.getOwner() == null) return;
        if (!bomb.markEffectsInitialized()) return;

        ExecutableEffectContext context = new ExecutableEffectContext(bomb.getOwner());
        context.set(ContextKeys.EVENT, event);
        context.set(ContextKeys.ENTITY, List.of(event.getEntity()));
        context.set(ContextKeys.TARGET_POSITION, event.getEntity().position());

        dispatch(context, ModTriggerTypes.TNT_SPAWNED);
    }

    /** 在产生爆炸的时候，根据炸弹自身的TriggerModule来触发对应效果 */
    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getExplosion().getDirectSourceEntity() instanceof BombEffectSource bomb)) return;
        if (bomb.getOwner() == null) return;

        ExecutableEffectContext context = new ExecutableEffectContext(bomb.getOwner());
        context.set(ContextKeys.TARGET_POSITION, event.getExplosion().getPosition());
        context.set(ContextKeys.ENTITY, List.of(bomb.getBombEffectEntity()));
        context.set(ContextKeys.SECONDARY_LIVING_ENTITIES,
                event.getAffectedEntities().stream()
                        .filter(LivingEntity.class::isInstance)
                        .map(LivingEntity.class::cast)
                        .toList()
        );

        // 触发对应爆炸效果
        bomb.getCachedEffect().apply(context);
    }

    @SubscribeEvent
    public static void onAttackPlan(AttackPlanEvent event) {
        LivingEntity owner = event.getOwner();
        if (owner == null) return;

        ExecutableEffectContext context = new ExecutableEffectContext(owner);
        context.set(ContextKeys.EVENT, event);
        context.set(ContextKeys.TARGET_POSITION, event.getBaseContexts().isEmpty()
                ? owner.position()
                : event.getBaseContexts().get(0).getPos());
        dispatch(context, ModTriggerTypes.ATTACK_PLAN);
    }

    @SubscribeEvent
    public static void onAttackContextPrepare(AttackContextPrepareEvent event) {
        LivingEntity owner = event.getOwner();
        if (owner == null) return;

        ExecutableEffectContext context = new ExecutableEffectContext(owner);
        context.set(ContextKeys.EVENT, event);
        context.set(ContextKeys.ATTACK_CONTEXT, event.getAttackContext());
        context.set(ContextKeys.TARGET_POSITION, event.getAttackContext().getPos());
        dispatch(context, ModTriggerTypes.ATTACK_CONTEXT_PREPARE);
    }


}

