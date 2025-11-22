package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.entity.EffectModulesProvider;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackAfterHitEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackBeforeHitEntityEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackHitBlockEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.PlayerPerformAttackEvent;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.attack.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerCategory;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

import java.util.Set;

@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID)
public class TriggerModuleEvents {

    @SubscribeEvent
    public static void onShoot(PlayerPerformAttackEvent event) {
        Player player = event.getPlayer();
        IForgeRegistry<ITriggerModule> reg =
                RegistryManager.ACTIVE.getRegistry(ModTriggerModule.TRIGGER_MODULE_KEY);

        player.getCapability(EffectModulesProvider.EFFECT_MODULES).ifPresent(triggerModule -> {

            var queue = triggerModule.getTriggerModules().getCopy();

            //使用索引遍历，允许在遍历过程中添加/删除元素
            for (int i = 0; i < queue.getQueue().size(); i++) {
                TriggerModuleInstance inst = queue.getQueue().get(i);
                ITriggerModule val = reg.getValue(inst.id);
                if (val == null) continue;

                Set<TriggerCategory> types = val.getTriggerType();

                if (types.contains(TriggerCategory.ON_SHOOT)) {
                    // 将 queue 引用传入，允许在 onShoot 内部修改列表
                    val.onShoot(event, inst.stacks, queue);
                }
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

        entity.getCapability(EffectModulesProvider.EFFECT_MODULES).ifPresent(
                triggerModule -> {
                    var queue = triggerModule.getTriggerModules().getCopy();

                    for (var inst : queue.getQueue()){
                        ITriggerModule val = reg.getValue(inst.id);
                        if (val == null) continue;
                        Set<TriggerCategory> types = val.getTriggerType();

                        if (types.contains(TriggerCategory.HIT_ENTITY)){
                            val.onHitEntity(event, inst.stacks, queue);
                        }
                    }
                }
        );
    }

    @SubscribeEvent
    public static void beforeBulletHitEntity(IsaacAttackBeforeHitEntityEvent event) {
        IForgeRegistry<ITriggerModule> reg  =
                RegistryManager.ACTIVE.getRegistry(ModTriggerModule.TRIGGER_MODULE_KEY);

        IBulletObject bulletObject = event.getBulletObject();
        var queue = bulletObject.getTriggerModules().getCopy();

        for (var inst : queue.getQueue()){
            ITriggerModule val = reg.getValue(inst.id);
            if (val == null) continue;
            Set<TriggerCategory> types = val.getTriggerType();

            if (types.contains(TriggerCategory.BULLET_HIT_ENTITY_BEFORE)){
                val.beforeBulletHitEntity(event, inst.stacks, queue);
            }
        }
    }

    @SubscribeEvent
    public static void afterBulletHitEntity(IsaacAttackAfterHitEvent event) {
        IForgeRegistry<ITriggerModule> reg  =
                RegistryManager.ACTIVE.getRegistry(ModTriggerModule.TRIGGER_MODULE_KEY);

        IBulletObject bulletObject = event.getBulletObject();
        var queue = bulletObject.getTriggerModules().getCopy();

        for (var inst : queue.getQueue()){
            ITriggerModule val = reg.getValue(inst.id);
            if (val == null) continue;
            Set<TriggerCategory> types = val.getTriggerType();

            if (types.contains(TriggerCategory.BULLET_HIT_ENTITY_AFTER)){
                val.afterBulletHitEntity(event, inst.stacks, queue);
            }
        }
    }

    @SubscribeEvent
    public static void onBulletHitBlock(IsaacAttackHitBlockEvent event) {
        IForgeRegistry<ITriggerModule> reg  =
                RegistryManager.ACTIVE.getRegistry(ModTriggerModule.TRIGGER_MODULE_KEY);

        IBulletObject bulletObject = event.getBulletObject();
        var queue = bulletObject.getTriggerModules().getCopy();

        for (var inst : queue.getQueue()){
            ITriggerModule val = reg.getValue(inst.id);
            if (val == null) continue;
            Set<TriggerCategory> types = val.getTriggerType();

            if (types.contains(TriggerCategory.BULLET_HIT_BLOCK)){
                val.onBulletHitBlock(event, inst.stacks, queue);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        IForgeRegistry<ITriggerModule> reg  =
                RegistryManager.ACTIVE.getRegistry(ModTriggerModule.TRIGGER_MODULE_KEY);

        player.getCapability(EffectModulesProvider.EFFECT_MODULES).ifPresent(
                triggerModule -> {
                    var queue = triggerModule.getTriggerModules().getCopy();

                    for (var inst : queue.getQueue()){
                        ITriggerModule val = reg.getValue(inst.id);
                        if (val == null) continue;
                        Set<TriggerCategory> types = val.getTriggerType();

                        if (types.contains(TriggerCategory.ON_HURT)){
                            val.onHurt(event, inst.stacks, queue);
                        }
                        // 暂时将generic_kill作为判断依据
                        if (types.contains(TriggerCategory.ON_HURT_POSITIVE) &&
                                PlayerHelper.isSelfDamage(event.getSource())){
                            val.onHurtPositive(event, inst.stacks, queue);

                        }
                        if (types.contains(TriggerCategory.ON_HURT_NEGATIVE) &&
                                !PlayerHelper.isSelfDamage(event.getSource())){
                            val.onHurtNegative(event, inst.stacks, queue);
                        }
                    }
                }
        );
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        IForgeRegistry<ITriggerModule> reg  =
                RegistryManager.ACTIVE.getRegistry(ModTriggerModule.TRIGGER_MODULE_KEY);

        Player player = event.getPlayer();

        player.getCapability(EffectModulesProvider.EFFECT_MODULES).ifPresent(
                triggerModule -> {

                    var queue = triggerModule.getTriggerModules().getCopy();

                    for (var inst : queue.getQueue()){
                        ITriggerModule val = reg.getValue(inst.id);
                        if (val == null) continue;
                        Set<TriggerCategory> types = val.getTriggerType();

                        if (types.contains(TriggerCategory.HIT_ENTITY)){
                            val.onBlockBreak(event, inst.stacks, queue);
                        }
                    }
                }
        );
    }
}

