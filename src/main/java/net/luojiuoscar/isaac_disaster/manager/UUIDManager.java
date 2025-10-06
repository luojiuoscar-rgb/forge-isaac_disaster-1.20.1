package net.luojiuoscar.isaac_disaster.manager;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;

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
        ATTRIBUTE_FROM_UUID.put(DAMAGE_MODIFIER_ADDER, Attributes.ATTACK_DAMAGE);
        MULTIPLIER_UUID.add(DAMAGE_MODIFIER_MULTIPLIER);}

    public static final UUID LUCK_MODIFIER_ADDER =
            UUID.nameUUIDFromBytes(("isaac_disaster:luck_modifier_adder").getBytes());
    static {
        ATTRIBUTE_FROM_UUID.put(LUCK_MODIFIER_ADDER, Attributes.LUCK);}

    public static final UUID SCALE_MODIFIER_ADDER =
            UUID.nameUUIDFromBytes(("isaac_disaster:scale_modifier_adder").getBytes());
    static {
        ATTRIBUTE_FROM_UUID.put(SCALE_MODIFIER_ADDER, ModAttributes.SCALE.get());}

    // passive items
    public static final UUID MONEY_IS_POWER_ADDER =
            UUID.nameUUIDFromBytes(("isaac_disaster:money_is_power_adder").getBytes());
    static {
        ATTRIBUTE_FROM_UUID.put(MONEY_IS_POWER_ADDER, Attributes.ATTACK_DAMAGE);}
}

