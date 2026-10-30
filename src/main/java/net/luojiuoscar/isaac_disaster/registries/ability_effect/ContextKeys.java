package net.luojiuoscar.isaac_disaster.registries.ability_effect;

import java.util.List;
import net.luojiuoscar.isaac_disaster.system.combat_effect.attachment.EffectAttachmentNode;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletStickContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.profile.PotionProfile;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.periodic_module.PeriodicModuleQueue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.Event;

public final class ContextKeys {
    // 消耗品相关
    public static final ContextKey<ItemStack> ITEM_STACK = new ContextKey<>();
    public static final ContextKey<InteractionHand> HAND = new ContextKey<>();
    public static final ContextKey<Double> AMPLIFIER = new ContextKey<>();
    public static final ContextKey<Item> ITEM = new ContextKey<>();
    public static final ContextKey<Integer> COUNT = new ContextKey<>();

    // 药水相关
    public static final ContextKey<List<PotionProfile>> POTIONS = new ContextKey<>();

    // nested?
    public static final ContextKey<IExecutableEffect> EXECUTABLE_EFFECT = new ContextKey<>();

    // 事件触发相关
    public static final ContextKey<ResourceLocation> SOURCE = new ContextKey<>();
    public static final ContextKey<Event> EVENT = new ContextKey<>();
    public static final ContextKey<IBulletObject> BULLET = new ContextKey<>();
    public static final ContextKey<BulletStickContext> BULLET_STICK = new ContextKey<>();
    public static final ContextKey<ResourceLocation> BULLET_COLOR_ID = new ContextKey<>();
    public static final ContextKey<AttackContext> ATTACK_CONTEXT = new ContextKey<>();
    public static final ContextKey<EffectAttachmentNode> EFFECT_ATTACHMENT_NODE = new ContextKey<>();
    public static final ContextKey<PeriodicModuleQueue> PERIODIC_MODULE_QUEUE = new ContextKey<>();
    public static final ContextKey<CompositeTriggerView> COMPOSITE_TRIGGER_VIEW = new ContextKey<>();


    /** 刻意淆数据 */
    public static final ContextKey<Vec3> TARGET_POSITION = new ContextKey<>();

    public static final ContextKey<List<Boolean>> BOOLEAN = new ContextKey<>();
    public static final ContextKey<List<Double>> DOUBLE = new ContextKey<>();
    public static final ContextKey<List<LivingEntity>> SECONDARY_LIVING_ENTITIES = new ContextKey<>();
    public static final ContextKey<List<Entity>> ENTITY = new ContextKey<>();
    public static final ContextKey<List<ResourceLocation>> RESOURCE_LOCATIONS = new ContextKey<>();

}
