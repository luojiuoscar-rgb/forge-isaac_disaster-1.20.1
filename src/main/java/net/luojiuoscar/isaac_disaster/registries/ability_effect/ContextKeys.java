package net.luojiuoscar.isaac_disaster.registries.ability_effect;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.profile.PotionProfile;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

public final class ContextKeys {

    // 消耗品相关
    public static final ContextKey<ItemStack> ITEM_STACK = new ContextKey<>();
    public static final ContextKey<InteractionHand> HAND = new ContextKey<>();
    public static final ContextKey<Integer> AMPLIFIER = new ContextKey<>();
    public static final ContextKey<Item> ITEM = new ContextKey<>();

    // 药水相关
    public static final ContextKey<List<PotionProfile>> POTIONS = new ContextKey<>();

    // nested?
    public static final ContextKey<IAbilityEffect> ABILITY_EFFECT = new ContextKey<>();

    /** 刻意淆数据 */
    public static final ContextKey<List<Boolean>> BOOLEAN = new ContextKey<>();
    public static final ContextKey<List<Double>> DOUBLE = new ContextKey<>();

    // 事件触发相关
    public static final ContextKey<Event> EVENT = new ContextKey<>();
}