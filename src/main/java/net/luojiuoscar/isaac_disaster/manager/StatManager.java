package net.luojiuoscar.isaac_disaster.manager;

import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.entity.EffectModulesProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerStatModifierProvider;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;

import static net.luojiuoscar.isaac_disaster.Config.HOLY_SHIELD_STRENGTH;
import static net.luojiuoscar.isaac_disaster.Config.NEARBY_RANGE;

public enum StatManager {
    MAX_HEALTH("max_health", Attributes.MAX_HEALTH, 0, true,
            () -> Config.HEALTH_BONUS.get(), -20.0, null){
        @Override
        public void apply(Player player, double ratio){
            super.apply(player, ratio);
            player.setHealth(player.getHealth()); // 刷新血量状态，避免显示溢出

            if (player.getMaxHealth() + player.getAbsorptionAmount() <= 0)
                player.kill();
        }
    },
    MOVEMENT_SPEED("movement_speed", Attributes.MOVEMENT_SPEED, 0, true,
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
    DAMAGE("damage", Attributes.ATTACK_DAMAGE, 0, true,
            () -> Config.DAMAGE_BONUS.get(), null, null),
    DAMAGE_MULTIPLY_BASE("damage_multiply_base", Attributes.ATTACK_DAMAGE, 1, false,
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
    LUCK("luck", Attributes.LUCK, 0, true,
            () -> Config.LUCK_BONUS.get(), null, null),
    SCALE("scale", ModAttributes.SCALE.get(), 0, false,
            () -> Config.SCALE_BONUS.get(), 0.25, 10.0){
        @Override
        public void apply(Player player, double ratio){
            super.apply(player, ratio);
            player.refreshDimensions();
        }
    },
    RANGE("bullet_range", ModAttributes.BULLET_RANGE.get(), 0, true,
            () -> Config.RANGE_BONUS.get(), null, null),
    ENTITY_REACH("entity_reach", ForgeMod.ENTITY_REACH.get(), 0, true,
            () -> Config.ENTITY_REACH_BONUS.get(), -2.0, null),
    BLOCK_REACH("block_reach", ForgeMod.BLOCK_REACH.get(), 0, true,
            () -> Config.BLOCK_REACH_BONUS.get(), -2.0, null),
    TEARS("tears", ModAttributes.TEARS.get(), 0, true,
            () -> Config.TEARS_BONUS.get(), null, null),
    TEARS_CORRECTION("tears_correction", ModAttributes.TEARS_CORRECTION.get(), 0, true,
            () -> Config.TEARS_CORRECTION_BONUS.get(), null, null),
    BULLET_SPEED("bullet_speed", ModAttributes.BULLET_SPEED.get(), 0, true,
            () -> Config.BULLET_SPEED_BONUS.get(), null, null),
    ATTACK_SPEED("attack_speed", Attributes.ATTACK_SPEED, 0, true,
            () -> Config.ATTACK_SPEED_BONUS.get(), null, null),
    BLOCK_BREAKING("block_breaking_speed", ModAttributes.BLOCK_BREAKING_SPEED.get(), 0, true,
            () -> Config.BLOCK_BREAKING_SPEED_BONUS.get(), 0.0, null),
    ATTACK_KNOCKBACK("attack_knockback", Attributes.ATTACK_KNOCKBACK, 0, false,
            () -> Config.ATTACK_KNOCKBACK_BONUS.get(), null, null),
    BULLET_SCALE("bullet_scale", ModAttributes.BULLET_SCALE.get(), 0, false,
            () -> Config.BULLET_SCALE_BONUS.get(), null, null),
    BULLET_COUNT("bullet_count", ModAttributes.BULLET_COUNT.get(), 0, false,
            null, 1.0, null),
    PILL_QUALITY("pill_quality", ModAttributes.PILL_QUALITY.get(), 0, false,
            null, null, null),
    FLY_TIME("fly_time", ModAttributes.FLY_TIME.get(), 0, false,
            () -> Config.FLY_TIME.get(), null, null){
        @Override
        public void apply(Player player, double ratio){
            super.apply(player, ratio);

            if (!PlayerHelper.canFly(player)){
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
            }
        }
    };


    private final UUID uuid;
    private final Attribute attribute;
    private final int operationType;
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


    StatManager(String key, Attribute attribute, int operationType, boolean isNormalType, @Nullable Supplier<Double> bonus,
                @Nullable Double minVal, @Nullable Double maxVal) {
        this.uuid = UUID.nameUUIDFromBytes(("isaac_disaster:" + key).getBytes(StandardCharsets.UTF_8));
        this.attribute = attribute;
        this.operationType = operationType;
        this.isNormalType = isNormalType;
        this.key = key;
        this.bonus = bonus;
        this.minVal = minVal;
        this.maxVal = maxVal;
    }

    public UUID getUUID() { return uuid; }
    public Attribute getAttribute() { return attribute; }
    public int getOperationType() { return operationType; }
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
        if (!(player instanceof ServerPlayer p)) return;
        StatManager.addModifier(p, uuid, attribute,ratio * getBonus(), minVal, maxVal, operationType);
    }

    public void refresh(ServerPlayer player) {
        player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                playerStatModifier -> {
                    var statInst = playerStatModifier.getStatInstance(uuid);
                    if (statInst == null) return;

                    double val = statInst.getDisplayValue();
                    AttributeInstance inst = player.getAttribute(attribute);
                    if (inst == null) return;

                    StatManager.setModifierValue(inst, uuid, val, operationType);
                }
        );
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

    public static void removeModifier(ServerPlayer player, @Nullable AttributeInstance attribute, UUID uuid) {
        if (attribute == null) return;
        attribute.removeModifier(uuid);
        player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                playerStatModifier -> playerStatModifier.removeModifier(uuid)
        );
    }

    /** 存在则修改，不存在则创建，不会覆盖 */
    public static void setModifier(ServerPlayer player, UUID uuid, @Nullable Attribute attribute, double amount,
                                   @Nullable Double minValue, @Nullable Double maxValue, int operationType) {
        if (attribute == null) return;
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;

        player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                playerStatModifier -> {
                    playerStatModifier.setModifierValue(uuid, amount, maxValue, minValue, attribute, operationType);

                    var statInst = playerStatModifier.getStatInstance(uuid);
                    if (statInst == null) return;

                    statInst.updateValue(player);
                    double val = statInst.getDisplayValue();

                    setModifierValue(instance, uuid, val, operationType);
                }
        );
    }

    /** 存在则修改，不存在则创建，不会覆盖 */
    public static void addModifier(ServerPlayer player, UUID uuid, @Nullable Attribute attribute, double amount,
                                   @Nullable Double minValue, @Nullable Double maxValue, int operationType){
        if (attribute == null) return;
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;

        player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                playerStatModifier -> {
                    playerStatModifier.addModifierValue(uuid, amount, maxValue, minValue, attribute, operationType);

                    var statInst = playerStatModifier.getStatInstance(uuid);
                    if (statInst == null) return;

                    statInst.updateValue(player);
                    double val = statInst.getDisplayValue();

                    setModifierValue(instance, uuid, val, operationType);
                }
        );
    }

    /**
     * 设置modifier的最终值
     * @param operationType 0 -> add; 1-> multiply_base; 2 -> multiply_total
     */
    public static void setModifierValue(AttributeInstance instance, UUID uuid, double value, int operationType){
        AttributeModifier.Operation operation = AttributeModifier.Operation.ADDITION;
        if (operationType == 1){
            operation = AttributeModifier.Operation.MULTIPLY_BASE;
        }else if (operationType == 2){
            operation = AttributeModifier.Operation.MULTIPLY_TOTAL;
        }

        instance.removeModifier(uuid);

        instance.addPermanentModifier(new AttributeModifier(
                uuid,
                "",
                value,
                operation
        ));
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

    public static void addPiercing(Player player, int amount){
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> playerAbility.setPiercing(playerAbility.getPiercing() + amount)
        );
    }
    public static void addHoming(Player player, int amount){
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> playerAbility.setHoming(playerAbility.getHoming() + amount)
        );
    }
    public static void addSpectral(Player player, int amount){
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> playerAbility.setSpectral(playerAbility.getSpectral() + amount)
        );
    }
    public static void addControllable(Player player, int amount){
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> playerAbility.setControllable(playerAbility.getControllable() + amount)
        );
    }
    /* ---------------------- 注册项目 ---------------------- */
    public static void addAttackType(ServerPlayer player, ResourceLocation id, int count){
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> playerAbility.addAttackType(id, count)
        );
    }

    public static void addBulletColor(ServerPlayer player, ResourceLocation rl, int count){
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> playerAbility.addBulletColor(rl, count)
        );
    }

    public static void addTrajectory(ServerPlayer player, ResourceLocation rl, int count){
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> playerAbility.addTrajectory(rl, count)
        );
    }

    public static void addTriggerModule(LivingEntity entity, ResourceLocation rl, int count){
        IForgeRegistry<ITriggerModule> reg =
                RegistryManager.ACTIVE.getRegistry(ModTriggerModule.TRIGGER_MODULE_KEY);

        entity.getCapability(EffectModulesProvider.EFFECT_MODULES).ifPresent(
                effectModules -> {
                    effectModules.getTriggerModules().add(rl, count);

                    ITriggerModule module = reg.getValue(rl);
                    if (module == null) return;

                    TriggerModuleQueue queue = effectModules.getTriggerModules().copy();
                    queue.lock();

                    if (count > 0){
                        module.onAdded(entity, queue);
                    }else if (count < 0){
                        module.onRemove(entity, queue);
                    }
                }
        );
    }

    public static void addRecursiveModule(LivingEntity entity, ResourceLocation rl, int count){
        entity.getCapability(EffectModulesProvider.EFFECT_MODULES).ifPresent(
                effectModules -> effectModules.getRecursiveModuleQueue().add(entity, rl, count)
        );
    }
    /* ---------------------- 道具套装 ---------------------- */

    public static void modifySetWithId(Player player, ResourceLocation rl, int amount){
        if(player instanceof ServerPlayer serverPlayer){
            player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                    playerPassiveItem -> playerPassiveItem.modifySetCount(serverPlayer, rl, amount)
            );
        }
    }
}
