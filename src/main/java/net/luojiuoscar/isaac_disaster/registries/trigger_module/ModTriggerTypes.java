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

    public static final TriggerType HIT_ENTITY =
            new TriggerType(ResourceLocation.fromNamespaceAndPath(
                    IsaacDisaster.MOD_ID, "hit_entity"));

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

    public static final TriggerType LEFT_CLICK_BLOCK =
            new TriggerType(ResourceLocation.fromNamespaceAndPath(
                    IsaacDisaster.MOD_ID, "left_click_block"));

    public static final TriggerType RIGHT_CLICK_BLOCK =
            new TriggerType(ResourceLocation.fromNamespaceAndPath(
                    IsaacDisaster.MOD_ID, "right_click_block"));
}
