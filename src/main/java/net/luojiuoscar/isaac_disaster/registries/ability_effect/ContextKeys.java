package net.luojiuoscar.isaac_disaster.registries.ability_effect;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.profile.PotionProfile;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.RecursiveModuleQueue;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

public final class ContextKeys {
    // 消耗品相关
    public static final ContextKey<ItemStack> ITEM_STACK = new ContextKey<>();
    public static final ContextKey<InteractionHand> HAND = new ContextKey<>();
    public static final ContextKey<Double> AMPLIFIER = new ContextKey<>();
    public static final ContextKey<Item> ITEM = new ContextKey<>();

    // 药水相关
    public static final ContextKey<List<PotionProfile>> POTIONS = new ContextKey<>();

    // nested?
    public static final ContextKey<IExecutableEffect> EXECUTABLE_EFFECT = new ContextKey<>();

    // 事件触发相关
    public static final ContextKey<Event> EVENT = new ContextKey<>();
    public static final ContextKey<IBulletObject> BULLET = new ContextKey<>();
    public static final ContextKey<ResourceLocation> BULLET_COLOR_ID = new ContextKey<>();

    /** 用于效果的上下文查找。但是CompositeTriggerView不一定包含所有的上下文，如从TriggerModuleQueue中触发时。
     * 对于Bullet实体（直接持有CompositeTrigger)而言，能够获取到全部的上下文 */
    public static final ContextKey<TriggerModuleQueue> TRIGGER_MODULE_QUEUE = new ContextKey<>();
    public static final ContextKey<RecursiveModuleQueue> RECURSIVE_MODULE_QUEUE = new ContextKey<>();
    public static final ContextKey<CompositeTriggerView> COMPOSITE_TRIGGER_VIEW = new ContextKey<>();


    /** 刻意淆数据 */
    public static final ContextKey<Vec3> TARGET_POSITION = new ContextKey<>();

    public static final ContextKey<List<Boolean>> BOOLEAN = new ContextKey<>();
    public static final ContextKey<List<Double>> DOUBLE = new ContextKey<>();
    public static final ContextKey<List<LivingEntity>> SECONDARY_LIVING_ENTITIES = new ContextKey<>();
    public static final ContextKey<List<Entity>> ENTITY = new ContextKey<>();
    public static final ContextKey<List<ResourceLocation>> RESOURCE_LOCATIONS = new ContextKey<>();

}