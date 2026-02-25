package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.entity.EffectModulesProvider;
import net.luojiuoscar.isaac_disaster.event.custom.attack.*;
import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.BulletTickEvent;
import net.luojiuoscar.isaac_disaster.event.custom.misc.BeforeTriggerModuleActiveEvent;
import net.luojiuoscar.isaac_disaster.event.custom.misc.RightClickTickEvent;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModule;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID)
public class TriggerModuleEvents {

    @SubscribeEvent
    public static void getAttackContext(GetAttackContextEvent event) {
        LivingEntity entity = event.getPlayer();
        IForgeRegistry<ITriggerModule> reg =
                RegistryManager.ACTIVE.getRegistry(ModTriggerModule.TRIGGER_MODULE_KEY);

        entity.getCapability(EffectModulesProvider.EFFECT_MODULES).ifPresent(triggerModule -> {

            var queue = triggerModule.getTriggerModules().copy();

            BeforeTriggerModuleActiveEvent e = new BeforeTriggerModuleActiveEvent(event, queue);
            MinecraftForge.EVENT_BUS.post(e);

            queue.lock();

            for (var inst : queue.getQueue()) {
                ITriggerModule val = reg.getValue(inst.id);
                if (val == null) continue;

                val.getAttackContext(event, inst.stacks, queue);
            }
        });
    }

    @SubscribeEvent
    public static void beforePerformAttack(BeforePerformAttackEvent event) {
        LivingEntity entity = event.getEntity();
        IForgeRegistry<ITriggerModule> reg =
                RegistryManager.ACTIVE.getRegistry(ModTriggerModule.TRIGGER_MODULE_KEY);

        entity.getCapability(EffectModulesProvider.EFFECT_MODULES).ifPresent(triggerModule -> {

            var queue = triggerModule.getTriggerModules().copy();

            BeforeTriggerModuleActiveEvent e = new BeforeTriggerModuleActiveEvent(event, queue);
            MinecraftForge.EVENT_BUS.post(e);

            queue.lock();

            for (var inst : queue.getQueue()) {
                ITriggerModule val = reg.getValue(inst.id);
                if (val == null) continue;

                val.beforePerformAttack(event, inst.stacks, queue);
            }
        });
    }

    @SubscribeEvent
    public static void onHitEntity(LivingAttackEvent event) {
        // restricted damage types
        if (!PlayerHelper.isHitAllowedType(event.getSource())) return;
        if (!(event.getSource().getEntity() instanceof LivingEntity entity)) return;

        IForgeRegistry<ITriggerModule> reg  =
                RegistryManager.ACTIVE.getRegistry(ModTriggerModule.TRIGGER_MODULE_KEY);

        entity.getCapability(EffectModulesProvider.EFFECT_MODULES).ifPresent(triggerModule -> {
            var queue = triggerModule.getTriggerModules().copy();

            BeforeTriggerModuleActiveEvent e = new BeforeTriggerModuleActiveEvent(event, queue);
            MinecraftForge.EVENT_BUS.post(e);

            queue.lock();

            for (var inst : queue.getQueue()){
                ITriggerModule val = reg.getValue(inst.id);
                if (val == null) continue;

                val.onHitEntity(event, inst.stacks, queue);
            }
        });
    }

    @SubscribeEvent
    public static void beforeBulletHitEntity(IsaacAttackBeforeHitEntityEvent event) {
        IForgeRegistry<ITriggerModule> reg  =
                RegistryManager.ACTIVE.getRegistry(ModTriggerModule.TRIGGER_MODULE_KEY);

        IBulletObject bulletObject = event.getBulletObject();
        var queue = bulletObject.getTriggerModules().copy();

        BeforeTriggerModuleActiveEvent e = new BeforeTriggerModuleActiveEvent(event, queue);
        MinecraftForge.EVENT_BUS.post(e);

        queue.lock();

        for (var inst : queue.getQueue()){
            ITriggerModule val = reg.getValue(inst.id);
            if (val == null) continue;

            val.beforeBulletHitEntity(event, inst.stacks, queue);
        }
    }

    @SubscribeEvent
    public static void afterBulletHitEntity(IsaacAttackAfterHitEvent event) {
        IForgeRegistry<ITriggerModule> reg  =
                RegistryManager.ACTIVE.getRegistry(ModTriggerModule.TRIGGER_MODULE_KEY);

        IBulletObject bulletObject = event.getBulletObject();
        var queue = bulletObject.getTriggerModules().copy();

        BeforeTriggerModuleActiveEvent e = new BeforeTriggerModuleActiveEvent(event, queue);
        MinecraftForge.EVENT_BUS.post(e);

        queue.lock();

        for (var inst : queue.getQueue()){
            ITriggerModule val = reg.getValue(inst.id);
            if (val == null) continue;

            val.afterBulletHitEntity(event, inst.stacks, queue);
        }
    }

    @SubscribeEvent
    public static void onBulletHitBlock(IsaacAttackHitBlockEvent event) {
        IForgeRegistry<ITriggerModule> reg  =
                RegistryManager.ACTIVE.getRegistry(ModTriggerModule.TRIGGER_MODULE_KEY);

        IBulletObject bulletObject = event.getBulletObject();
        var queue = bulletObject.getTriggerModules().copy();

        BeforeTriggerModuleActiveEvent e = new BeforeTriggerModuleActiveEvent(event, queue);
        MinecraftForge.EVENT_BUS.post(e);

        queue.lock();

        for (var inst : queue.getQueue()){
            ITriggerModule val = reg.getValue(inst.id);
            if (val == null) continue;

            val.onBulletHitBlock(event, inst.stacks, queue);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        IForgeRegistry<ITriggerModule> reg  =
                RegistryManager.ACTIVE.getRegistry(ModTriggerModule.TRIGGER_MODULE_KEY);

        player.getCapability(EffectModulesProvider.EFFECT_MODULES).ifPresent(triggerModule -> {
            var queue = triggerModule.getTriggerModules().copy();

            BeforeTriggerModuleActiveEvent e = new BeforeTriggerModuleActiveEvent(event, queue);
            MinecraftForge.EVENT_BUS.post(e);

            queue.lock();

            for (var inst : queue.getQueue()){
                ITriggerModule val = reg.getValue(inst.id);
                if (val == null) continue;

                val.onHurt(event, inst.stacks, queue);
                // 暂时将generic_kill作为判断依据
                if (PlayerHelper.isSelfDamage(event.getSource())){
                    val.onHurtPositive(event, inst.stacks, queue);

                }
                if (!PlayerHelper.isSelfDamage(event.getSource())){
                    val.onHurtNegative(event, inst.stacks, queue);
                }
            }
        });
    }

    @SubscribeEvent
    public static void onKilledEntity(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        IForgeRegistry<ITriggerModule> reg  =
                RegistryManager.ACTIVE.getRegistry(ModTriggerModule.TRIGGER_MODULE_KEY);

        player.getCapability(EffectModulesProvider.EFFECT_MODULES).ifPresent(triggerModule -> {
            var queue = triggerModule.getTriggerModules().copy();

            BeforeTriggerModuleActiveEvent e = new BeforeTriggerModuleActiveEvent(event, queue);
            MinecraftForge.EVENT_BUS.post(e);

            queue.lock();

            for (var inst : queue.getQueue()){
                ITriggerModule val = reg.getValue(inst.id);
                if (val == null) continue;

                val.onKilledEntity(event, inst.stacks, queue);
            }
        });
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        IForgeRegistry<ITriggerModule> reg  =
                RegistryManager.ACTIVE.getRegistry(ModTriggerModule.TRIGGER_MODULE_KEY);

        Player player = event.getPlayer();

        player.getCapability(EffectModulesProvider.EFFECT_MODULES).ifPresent(triggerModule -> {
            var queue = triggerModule.getTriggerModules().copy();

            BeforeTriggerModuleActiveEvent e = new BeforeTriggerModuleActiveEvent(event, queue);
            MinecraftForge.EVENT_BUS.post(e);

            queue.lock();

            for (var inst : queue.getQueue()){
                ITriggerModule val = reg.getValue(inst.id);
                if (val == null) continue;

                val.onBlockBreak(event, inst.stacks, queue);
            }
        });
    }

    @SubscribeEvent
    public static void onBulletTick(BulletTickEvent event) {
        IBulletObject bullet = event.getBullet();
        if (!(bullet.getOwner() instanceof Player player)) return;

        IForgeRegistry<ITriggerModule> reg  =
                RegistryManager.ACTIVE.getRegistry(ModTriggerModule.TRIGGER_MODULE_KEY);

        player.getCapability(EffectModulesProvider.EFFECT_MODULES).ifPresent(triggerModule -> {
            var queue = triggerModule.getTriggerModules().copy();

            BeforeTriggerModuleActiveEvent e = new BeforeTriggerModuleActiveEvent(event, queue);
            MinecraftForge.EVENT_BUS.post(e);

            queue.lock();

            for (var inst : queue.getQueue()){
                ITriggerModule val = reg.getValue(inst.id);
                if (val == null) continue;

                val.onBulletTick(event, inst.stacks, queue);
            }
        });
    }

    @SubscribeEvent
    public static void onRightClickTick(RightClickTickEvent event) {
        ServerPlayer player = event.getPlayer();

        IForgeRegistry<ITriggerModule> reg  =
                RegistryManager.ACTIVE.getRegistry(ModTriggerModule.TRIGGER_MODULE_KEY);

        player.getCapability(EffectModulesProvider.EFFECT_MODULES).ifPresent(triggerModule -> {
            var queue = triggerModule.getTriggerModules().copy();

            BeforeTriggerModuleActiveEvent e = new BeforeTriggerModuleActiveEvent(event, queue);
            MinecraftForge.EVENT_BUS.post(e);

            queue.lock();

            for (var inst : queue.getQueue()){
                ITriggerModule val = reg.getValue(inst.id);
                if (val == null) continue;

                val.onRightClickTick(event, inst.stacks, queue);
            }
        });
    }
}

