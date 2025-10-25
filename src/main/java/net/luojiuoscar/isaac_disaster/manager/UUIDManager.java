package net.luojiuoscar.isaac_disaster.manager;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;

import java.util.*;

public class UUIDManager {
    // types
    public static List<UUID> MULTIPLIER_UUID = new ArrayList<>();
    public static Map<UUID, Attribute> ATTRIBUTE_FROM_UUID = new HashMap<>();

    // status
    public static final UUID MAX_HEALTH_MODIFIER_ADDER =
            UUID.nameUUIDFromBytes(("isaac_disaster:max_health_modifier_adder_location").getBytes());
    static {
        ATTRIBUTE_FROM_UUID.put(MAX_HEALTH_MODIFIER_ADDER, Attributes.MAX_HEALTH);}

    public static final UUID MOVEMENT_SPEED_MODIFIER_ADDER =
            UUID.nameUUIDFromBytes(("isaac_disaster:movement_speed_modifier_adder_uuid").getBytes());
    static {
        ATTRIBUTE_FROM_UUID.put(MOVEMENT_SPEED_MODIFIER_ADDER, Attributes.MOVEMENT_SPEED);}

    public static final UUID DAMAGE_MODIFIER_ADDER =
            UUID.nameUUIDFromBytes(("isaac_disaster:damage_modifier_adder_uuid").getBytes());
    static {
        ATTRIBUTE_FROM_UUID.put(DAMAGE_MODIFIER_ADDER, Attributes.ATTACK_DAMAGE);}

    public static final UUID DAMAGE_MODIFIER_MULTIPLIER =
            UUID.nameUUIDFromBytes(("isaac_disaster:damage_modifier_multiplier_uuid").getBytes());
    static {
        ATTRIBUTE_FROM_UUID.put(DAMAGE_MODIFIER_MULTIPLIER, Attributes.ATTACK_DAMAGE);
        MULTIPLIER_UUID.add(DAMAGE_MODIFIER_MULTIPLIER);}

    public static final UUID LUCK_MODIFIER_ADDER =
            UUID.nameUUIDFromBytes(("isaac_disaster:luck_modifier_adder").getBytes());
    static {
        ATTRIBUTE_FROM_UUID.put(LUCK_MODIFIER_ADDER, Attributes.LUCK);}

    public static final UUID SCALE_MODIFIER_ADDER =
            UUID.nameUUIDFromBytes(("isaac_disaster:scale_modifier_adder").getBytes());
    static {
        ATTRIBUTE_FROM_UUID.put(SCALE_MODIFIER_ADDER, ModAttributes.SCALE.get());}

    public static final UUID RANGE_MODIFIER_ADDER =
            UUID.nameUUIDFromBytes(("isaac_disaster:range_modifier_adder").getBytes());
    static {
        ATTRIBUTE_FROM_UUID.put(RANGE_MODIFIER_ADDER, ModAttributes.BULLET_RANGE.get());}

    public static final UUID ENTITY_REACH_MODIFIER_ADDER =
            UUID.nameUUIDFromBytes(("isaac_disaster:entity_reach_modifier_adder").getBytes());
    static {
        ATTRIBUTE_FROM_UUID.put(ENTITY_REACH_MODIFIER_ADDER, ForgeMod.ENTITY_REACH.get());}

    public static final UUID BLOCK_REACH_MODIFIER_ADDER =
            UUID.nameUUIDFromBytes(("isaac_disaster:block_reach_modifier_adder").getBytes());
    static {
        ATTRIBUTE_FROM_UUID.put(BLOCK_REACH_MODIFIER_ADDER, ForgeMod.BLOCK_REACH.get());}

    public static final UUID TEARS_MODIFIER_ADDER =
            UUID.nameUUIDFromBytes(("isaac_disaster:tears_modifier_adder").getBytes());
    static {
        ATTRIBUTE_FROM_UUID.put(TEARS_MODIFIER_ADDER, ModAttributes.TEARS.get());}

    public static final UUID TEARS_CORRECTION_MODIFIER_ADDER =
            UUID.nameUUIDFromBytes(("isaac_disaster:tears_correction_modifier_adder").getBytes());
    static {
        ATTRIBUTE_FROM_UUID.put(TEARS_CORRECTION_MODIFIER_ADDER, ModAttributes.TEARS_CORRECTION.get());}

    public static final UUID BULLET_SPEED_MODIFIER_ADDER =
            UUID.nameUUIDFromBytes(("isaac_disaster:bullet_speed_modifier_adder").getBytes());
    static {
        ATTRIBUTE_FROM_UUID.put(BULLET_SPEED_MODIFIER_ADDER, ModAttributes.BULLET_SPEED.get());}

    public static final UUID ATTACK_SPEED_MODIFIER_ADDER =
            UUID.nameUUIDFromBytes(("isaac_disaster:attack_speed_modifier_adder").getBytes());
    static {
        ATTRIBUTE_FROM_UUID.put(ATTACK_SPEED_MODIFIER_ADDER, Attributes.ATTACK_SPEED);}

    public static final UUID BLOCK_BREAKING_SPEED_BONUS =
            UUID.nameUUIDFromBytes(("isaac_disaster:block_breaking_speed_bonus").getBytes());
    static {
        ATTRIBUTE_FROM_UUID.put(BLOCK_BREAKING_SPEED_BONUS, ModAttributes.BLOCK_BREAKING_SPEED.get());}

    public static final UUID ATTACK_KNOCKBACK_BONUS =
            UUID.nameUUIDFromBytes(("isaac_disaster:attack_knockback_bonus").getBytes());
    static {
        ATTRIBUTE_FROM_UUID.put(ATTACK_KNOCKBACK_BONUS, Attributes.ATTACK_KNOCKBACK);}

    public static final UUID BULLET_SCALE_BONUS =
            UUID.nameUUIDFromBytes(("isaac_disaster:bullet_scale_bonus").getBytes());
    static {
        ATTRIBUTE_FROM_UUID.put(BULLET_SCALE_BONUS, ModAttributes.BULLET_SCALE.get());}

    public static final UUID BULLET_COUNT_BONUS =
            UUID.nameUUIDFromBytes(("isaac_disaster:bullet_count_bonus").getBytes());
    static {
        ATTRIBUTE_FROM_UUID.put(BULLET_COUNT_BONUS, ModAttributes.BULLET_COUNT.get());}

    public static final UUID PILL_QUALITY =
            UUID.nameUUIDFromBytes(("isaac_disaster:pill_quality").getBytes());
    static {
        ATTRIBUTE_FROM_UUID.put(PILL_QUALITY, ModAttributes.PILL_QUALITY.get());}

    public static final UUID FLY_TIME =
            UUID.nameUUIDFromBytes(("isaac_disaster:fly_time").getBytes());
    static {
        ATTRIBUTE_FROM_UUID.put(FLY_TIME, ModAttributes.FLY_TIME.get());}



    // passive items
    public static final UUID MONEY_IS_POWER_ADDER =
            UUID.nameUUIDFromBytes(("isaac_disaster:money_is_power_adder").getBytes());
    static {
        ATTRIBUTE_FROM_UUID.put(MONEY_IS_POWER_ADDER, Attributes.ATTACK_DAMAGE);}

    public static final UUID DAMAGE_FROM_PILLS_ADDER =
            UUID.nameUUIDFromBytes(("isaac_disaster:damage_from_pills_adder").getBytes());
    static {
        ATTRIBUTE_FROM_UUID.put(DAMAGE_FROM_PILLS_ADDER, Attributes.ATTACK_DAMAGE);}

    public static final UUID PERFECT_VISION =
            UUID.nameUUIDFromBytes(("isaac_disaster:perfect_vision").getBytes());
    static {
        ATTRIBUTE_FROM_UUID.put(PERFECT_VISION, Attributes.ATTACK_DAMAGE);
        MULTIPLIER_UUID.add(DAMAGE_MODIFIER_MULTIPLIER);}


}

