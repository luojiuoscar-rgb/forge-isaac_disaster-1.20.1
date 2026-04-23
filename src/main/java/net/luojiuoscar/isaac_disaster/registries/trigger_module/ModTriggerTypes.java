package net.luojiuoscar.isaac_disaster.registries.trigger_module;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.resources.ResourceLocation;

public class ModTriggerTypes {
    public static final TriggerType GET_ATTACK_CONTEXT =
            new TriggerType(ResourceLocation.fromNamespaceAndPath(
                    IsaacDisaster.MOD_ID, "get_attack_context"));

    public static final TriggerType BEFORE_PERFORM_ATTACK =
            new TriggerType(ResourceLocation.fromNamespaceAndPath(
                    IsaacDisaster.MOD_ID, "before_perform_attack"));

    public static final TriggerType BULLET_HIT_ENTITY_BEFORE =
            new TriggerType(ResourceLocation.fromNamespaceAndPath(
                    IsaacDisaster.MOD_ID, "bullet_hit_entity_before"));

    public static final TriggerType BULLET_HIT_ENTITY_AFTER =
            new TriggerType(ResourceLocation.fromNamespaceAndPath(
                    IsaacDisaster.MOD_ID, "bullet_hit_entity_after"));

    public static final TriggerType BULLET_END_OF_LIFE =
            new TriggerType(ResourceLocation.fromNamespaceAndPath(
                    IsaacDisaster.MOD_ID, "bullet_end_of_life"));

    /** 只会被“允许的伤害类别” 触发 */
    public static final TriggerType HIT_ENTITY_RESTRICTED =
            new TriggerType(ResourceLocation.fromNamespaceAndPath(
                    IsaacDisaster.MOD_ID, "hit_entity_restricted"));

    public static final TriggerType KILL_ENTITY =
            new TriggerType(ResourceLocation.fromNamespaceAndPath(
                    IsaacDisaster.MOD_ID, "kill_entity"));

    public static final TriggerType DEATH =
            new TriggerType(ResourceLocation.fromNamespaceAndPath(
                    IsaacDisaster.MOD_ID, "death"));

    public static final TriggerType BULLET_HIT_BLOCK =
            new TriggerType(ResourceLocation.fromNamespaceAndPath(
                    IsaacDisaster.MOD_ID, "bullet_hit_block"));

    public static final TriggerType ON_HURT =
            new TriggerType(ResourceLocation.fromNamespaceAndPath(
                    IsaacDisaster.MOD_ID, "on_hurt"));

    public static final TriggerType ON_HURT_POSITIVE =
            new TriggerType(ResourceLocation.fromNamespaceAndPath(
                    IsaacDisaster.MOD_ID, "on_hurt_positive"));

    public static final TriggerType ON_HURT_NEGATIVE =
            new TriggerType(ResourceLocation.fromNamespaceAndPath(
                    IsaacDisaster.MOD_ID, "on_hurt_negative"));

    public static final TriggerType BREAK_BLOCK =
            new TriggerType(ResourceLocation.fromNamespaceAndPath(
                    IsaacDisaster.MOD_ID, "break_block"));

    public static final TriggerType BULLET_TICK =
            new TriggerType(ResourceLocation.fromNamespaceAndPath(
                    IsaacDisaster.MOD_ID, "bullet_tick"));

    public static final TriggerType RIGHT_CLICK_TICK =
            new TriggerType(ResourceLocation.fromNamespaceAndPath(
                    IsaacDisaster.MOD_ID, "right_click_tick"));

    public static final TriggerType PICKUP_ITEM =
            new TriggerType(ResourceLocation.fromNamespaceAndPath(
                    IsaacDisaster.MOD_ID, "pickup_item"));

    public static final TriggerType LOOT =
            new TriggerType(ResourceLocation.fromNamespaceAndPath(
                    IsaacDisaster.MOD_ID, "loot"));

    public static final TriggerType TNT_SPAWNED =
            new TriggerType(ResourceLocation.fromNamespaceAndPath(
                    IsaacDisaster.MOD_ID, "tnt_spawned"));

    public static final TriggerType ON_EXPLOSION =
            new TriggerType(ResourceLocation.fromNamespaceAndPath(
                    IsaacDisaster.MOD_ID, "on_explosion"));

    // 空触发器是用于占位的
    public static final TriggerType EMTPY =
            new TriggerType(ResourceLocation.fromNamespaceAndPath(
                    IsaacDisaster.MOD_ID, "empty"));
}
