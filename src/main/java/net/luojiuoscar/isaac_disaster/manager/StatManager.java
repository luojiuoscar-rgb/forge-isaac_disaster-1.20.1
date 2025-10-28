package net.luojiuoscar.isaac_disaster.manager;

import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerStatModifierProvider;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;

import static com.mojang.text2speech.Narrator.LOGGER;
import static net.luojiuoscar.isaac_disaster.Config.HOLY_SHIELD_STRENGTH;
import static net.luojiuoscar.isaac_disaster.Config.NEARBY_RANGE;

public enum StatManager {
    MAX_HEALTH("max_health", Attributes.MAX_HEALTH, false, true,
            () -> Config.HEALTH_BONUS.get(), -20.0, null){
        @Override
        public void apply(Player player, double ratio){
            StatManager.modifyAdd(player, getUUID(), ratio * getBonus(), getMinVal(), getMaxVal());
            player.setHealth(player.getHealth()); // 刷新血量状态，避免显示溢出
        }
    },
    MOVEMENT_SPEED("movement_speed", Attributes.MOVEMENT_SPEED, false, true,
            () -> Config.MOVEMENT_SPEED_BONUS.get(), null, null) {
        @Override
        public Component description(double value, Style style){
            // 四舍五入到两位小数
            double rounded = Math.round(value * 100.0) / 10.0;
            String formatted = String.format("%.1f", rounded) + "%";

            if (value > 0){ // 正数+
                formatted = "+" + formatted;
            }
            return Component.translatable("attribute." + IsaacDisaster.MOD_ID + "." + getKey())
                    .append(formatted).withStyle(style);
        }
    },
    DAMAGE("damage", Attributes.ATTACK_DAMAGE, false, true,
            () -> Config.DAMAGE_BONUS.get(), -0.9, null),
    DAMAGE_MULTIPLY_BASE("damage_multiply_base", Attributes.ATTACK_DAMAGE, true, false,
            () -> Config.DAMAGE_MULTIPLIER_BASE.get(), -0.9, null) {
        @Override
        public Component description(double value, Style style){
            // 四舍五入到两位小数
            double rounded = Math.round(value * 100);
            String formatted = String.format("%.1f", rounded) + "%";

            if (value > 0){ // 正数+
                formatted = "+" + formatted;
            }
            return Component.translatable("attribute." + IsaacDisaster.MOD_ID + "." + getKey())
                    .append(formatted).withStyle(style);
        }
    },
    LUCK("luck", Attributes.LUCK, false, true,
            () -> Config.LUCK_BONUS.get(), null, null),
    SCALE("scale", ModAttributes.SCALE.get(), false, false,
            () -> Config.SCALE_BONUS.get(), null, null){
        @Override
        public void apply(Player player, double ratio){
            StatManager.modifyAdd(player, getUUID(), ratio * getBonus(), getMinVal(), getMaxVal());
            player.refreshDimensions();
        }
    },
    RANGE("range", ModAttributes.BULLET_RANGE.get(), false, true,
            () -> Config.RANGE_BONUS.get(), null, null),
    ENTITY_REACH("entity_reach", ForgeMod.ENTITY_REACH.get(), false, true,
            () -> Config.ENTITY_REACH_BONUS.get(), -2.0, null),
    BLOCK_REACH("block_reach", ForgeMod.BLOCK_REACH.get(), false, true,
            () -> Config.BLOCK_REACH_BONUS.get(), -2.0, null),
    TEARS("tears", ModAttributes.TEARS.get(), false, true,
            () -> Config.TEARS_BONUS.get(), null, null),
    TEARS_CORRECTION("tears_correction", ModAttributes.TEARS_CORRECTION.get(), false, true,
            () -> Config.TEARS_CORRECTION_BONUS.get(), null, null),
    BULLET_SPEED("bullet_speed", ModAttributes.BULLET_SPEED.get(), false, true,
            () -> Config.BULLET_SPEED_BONUS.get(), null, null),
    ATTACK_SPEED("attack_speed", Attributes.ATTACK_SPEED, false, true,
            () -> Config.ATTACK_SPEED_BONUS.get(), null, null),
    BLOCK_BREAKING("block_breaking", ModAttributes.BLOCK_BREAKING_SPEED.get(), false, true,
            () -> Config.BLOCK_BREAKING_SPEED_BONUS.get(), 0.0, null),
    ATTACK_KNOCKBACK("attack_knockback", Attributes.ATTACK_KNOCKBACK, false, false,
            () -> Config.ATTACK_KNOCKBACK_BONUS.get(), null, null),
    BULLET_SCALE("bullet_scale", ModAttributes.BULLET_SCALE.get(), false, false,
            () -> Config.BULLET_SCALE_BONUS.get(), null, null),
    BULLET_COUNT("bullet_count", ModAttributes.BULLET_COUNT.get(), false, false,
            null, 1.0, null),
    PILL_QUALITY("pill_quality", ModAttributes.PILL_QUALITY.get(), false, false,
            null, null, null),
    FLY_TIME("fly_time", ModAttributes.FLY_TIME.get(), false, false,
            () -> Config.FLY_TIME.get(), null, null){
        @Override
        public void apply(Player player, double ratio){
            StatManager.modifyAdd(player, getUUID(), ratio * getBonus(), getMinVal(), getMaxVal());
            if (!PlayerHelper.canFly(player)){
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
            }
        }
    };


    private final UUID uuid;
    private final Attribute attribute;
    private final boolean isMultiplyBase;
    private final boolean isNormalType;
    private final String key;
    private final Supplier<Double> bonus;
    private final Double minVal;
    private final Double maxVal;


    // UUID -> 枚举反查Map
    private static final Map<UUID, StatManager> UUID_TO_ENUM = new HashMap<>();
    static {
        for (StatManager stat : values()) {
            UUID_TO_ENUM.put(stat.getUUID(), stat);
        }
    }


    StatManager(String key, Attribute attribute, boolean isMultiplyBase, boolean isNormalType, @Nullable Supplier<Double> bonus,
                @Nullable Double minVal, @Nullable Double maxVal) {
        this.uuid = UUID.nameUUIDFromBytes(("isaac_disaster:" + key).getBytes(StandardCharsets.UTF_8));
        this.attribute = attribute;
        this.isMultiplyBase = isMultiplyBase;
        this.isNormalType = isNormalType;
        this.key = key;
        this.bonus = bonus;
        this.minVal = minVal;
        this.maxVal = maxVal;
    }

    public UUID getUUID() { return uuid; }
    public Attribute getAttribute() { return attribute; }
    public boolean isMultiplyBase() { return isMultiplyBase; }
    public boolean isNormalType() {return isNormalType; }
    public String getKey() {return key; }
    public double getBonus() {
        if (bonus == null) return 1;
        return bonus.get();
    }
    public Double getMinVal() {return minVal; }
    public Double getMaxVal() {return maxVal; }

    /** 通用 apply 方法，可被 override */
    public void apply(Player player, double ratio){
        if(isMultiplyBase){
            StatManager.modifyMultiplyBase(player, uuid, ratio * getBonus(), minVal, maxVal);
        } else {
            StatManager.modifyAdd(player, uuid, ratio * getBonus(), minVal, maxVal);
        }
    }

    /** 获取属性的描述性文本 */
    public Component description(double ratio){
        return description(ratio, Style.EMPTY.withColor(ChatFormatting.WHITE));
    }
    public Component description(double ratio, Style style){
        // 四舍五入到两位小数
        double rounded = Math.round(ratio * 100 * getBonus()) / 100.0;

        // 判断是否为整数/一位/两位小数
        String formatted;
        if (Math.abs(rounded - Math.floor(rounded)) < 0.0001) {
            formatted = String.format("%.0f", rounded);
        } else if (Math.abs(rounded * 10 - Math.floor(rounded * 10)) < 0.0001) {
            formatted = String.format("%.1f", rounded);
        } else {
            formatted = String.format("%.2f", rounded);
        }

        if (ratio >= 0){ // 正数+
            formatted = "+" + formatted;
        }
        return Component.translatable("attribute." + IsaacDisaster.MOD_ID + "." + getKey())
                .append(formatted).withStyle(style);
    }

    /* ---------------------- STATICS ---------------------- */

    /** 根据 UUID 获取枚举实例 */
    public static StatManager fromUUID(UUID uuid){
        return UUID_TO_ENUM.get(uuid);
    }

    public static Set<UUID> getAllUUID(){
        return new HashSet<>(UUID_TO_ENUM.keySet());
    }

    public static Set<UUID> getAllNormalTypeUUID(){
        Set<UUID> uuids = new HashSet<>(UUID_TO_ENUM.keySet());
        uuids.removeIf(uuid -> !fromUUID(uuid).isNormalType);
        return uuids;
    }

    /* ---------------------- 通用修改方法 ---------------------- */

    public static void removeModifier(Player player, AttributeInstance attribute, UUID uuid) {
        attribute.removeModifier(uuid);
        player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                playerStatModifier -> playerStatModifier.removeModifier(uuid)
        );
    }

    public static void setModifierAdd(Player player, AttributeInstance attribute,
                                      double totalBoost, UUID uuid, String name) {
        attribute.removeModifier(uuid);
        attribute.addPermanentModifier(new AttributeModifier(uuid, name, totalBoost, AttributeModifier.Operation.ADDITION));
        player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                playerStatModifier -> playerStatModifier.setModifier(uuid, totalBoost)
        );
    }

    public static void setModifierMultiplyBase(Player player, AttributeInstance attribute,
                                               double totalBoost, UUID uuid, String name) {
        attribute.removeModifier(uuid);
        attribute.addPermanentModifier(new AttributeModifier(uuid, name, totalBoost, AttributeModifier.Operation.MULTIPLY_BASE));
        player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                playerStatModifier -> playerStatModifier.setModifier(uuid, totalBoost)
        );
    }

    public static void modifyAdd(Player player, UUID uuid, double amount,
                                 @javax.annotation.Nullable Double minValue, @javax.annotation.Nullable Double maxValue){
        AttributeInstance instance = player.getAttribute(StatManager.fromUUID(uuid).getAttribute());
        if(instance == null){
            LOGGER.debug("ADD FAILED");
            return;
        }

        AttributeModifier modifier = instance.getModifier(uuid);
        double current = modifier != null ? modifier.getAmount() : 0.0;
        double newAmount = current + amount;

        if(minValue != null && newAmount < minValue) newAmount = minValue;
        if(maxValue != null && newAmount > maxValue) newAmount = maxValue;

        setModifierAdd(player, instance, newAmount, uuid, "");
    }

    public static void modifyMultiplyBase(Player player, UUID uuid, double amount,
                                          @javax.annotation.Nullable Double minValue, @javax.annotation.Nullable Double maxValue){
        AttributeInstance instance = player.getAttribute(StatManager.fromUUID(uuid).getAttribute());
        if(instance == null){
            LOGGER.debug("MULTIPLY_BASE FAILED");
            return;
        }

        AttributeModifier modifier = instance.getModifier(uuid);
        double current = modifier != null ? modifier.getAmount() : 0.0;
        double newAmount = current + amount;

        if(minValue != null && newAmount < minValue) newAmount = minValue;
        if(maxValue != null && newAmount > maxValue) newAmount = maxValue;

        setModifierMultiplyBase(player, instance, newAmount, uuid, "");
    }

    /* ---------------------- 基础属性数值获取 ---------------------- */

    public static double getNearbyRange(){return NEARBY_RANGE.get();}
    public static double getHolyShieldStrength(){return HOLY_SHIELD_STRENGTH.get();}

    /* ---------------------- 生命 ---------------------- */

    public static void healHealth(Player player, float ratio){
        float amount = ratio * (float) MAX_HEALTH.getBonus();
        float newHealth = Math.min(player.getHealth() + amount, player.getMaxHealth());
        player.setHealth(newHealth);
    }
    public static Component healHealthDescription(float ratio){
        return healHealthDescription(ratio, Style.EMPTY.withColor(ChatFormatting.WHITE));
    }
    public static Component healHealthDescription(float ratio, Style style){
        // 四舍五入到两位小数
        double rounded = Math.round(ratio * 100 * MAX_HEALTH.getBonus()) / 100.0;

        return Component.translatable("item." + IsaacDisaster.MOD_ID + ".action.heal_health", rounded).withStyle(style);
    }

    public static void gainAbsorption(Player player, float ratio){
        player.setAbsorptionAmount(player.getAbsorptionAmount() + (float) MAX_HEALTH.getBonus() * ratio);
    }

    /* ---------------------- 子弹能力 ---------------------- */

    public static void modifyPiercing(Player player, int amount){
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> playerAbility.setPiercing(playerAbility.getPiercing() + amount)
        );
    }
    public static void modifyHoming(Player player, int amount){
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> playerAbility.setHoming(playerAbility.getHoming() + amount)
        );
    }
    public static void modifySpectral(Player player, int amount){
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> playerAbility.setSpectral(playerAbility.getSpectral() + amount)
        );
    }
    public static void modifyControllable(Player player, int amount){
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> playerAbility.setControllable(playerAbility.getControllable() + amount)
        );
    }

    public static void setBulletColor(Player player, int amount){
        AttributeInstance instance = player.getAttribute(ModAttributes.BULLET_COLOR.get());
        if (instance != null) instance.setBaseValue(amount);
    }
    public static void setBulletAlpha(Player player, int amount){
        AttributeInstance instance = player.getAttribute(ModAttributes.BULLET_ALPHA.get());
        if (instance != null) instance.setBaseValue(amount);
    }
    public static void setBulletFilter(Player player, int amount){
        AttributeInstance instance = player.getAttribute(ModAttributes.BULLET_FILTER.get());
        if (instance != null) instance.setBaseValue(amount);
    }
    public static void addBulletFilter(Player player, int amount){
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> playerAbility.addFilter(amount, player)
        );

    }
    public static void removeBulletFilter(Player player, int amount){
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> playerAbility.removeFilter(amount, player)
        );
    }

    /* ---------------------- 道具套装 ---------------------- */

    public static void modifySetWithId(Player player, int setId, int amount){
        if(player instanceof ServerPlayer serverPlayer){
            player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                    playerPassiveItem -> playerPassiveItem.modifySetCount(serverPlayer, setId, amount)
            );
        }
    }
}
