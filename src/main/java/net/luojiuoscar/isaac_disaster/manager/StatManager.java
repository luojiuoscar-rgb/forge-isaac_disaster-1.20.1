package net.luojiuoscar.isaac_disaster.manager;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerStatModifierProvider;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

import static net.luojiuoscar.isaac_disaster.Config.*;

/**
 * 属性控制器，统一管理所有道具对玩家的属性修改
 */
public class StatManager {
    // status
    private static final UUID MAX_HEALTH_MODIFIER_ADDER_UUID =
            UUID.nameUUIDFromBytes(("isaac_disaster:max_health_modifier_adder_location").getBytes());
    private static final UUID MOVEMENT_SPEED_MODIFIER_ADDER_UUID =
            UUID.nameUUIDFromBytes(("isaac_disaster:movement_speed_modifier_adder_uuid").getBytes());
    private static final UUID DAMAGE_MODIFIER_ADDER_UUID =
            UUID.nameUUIDFromBytes(("isaac_disaster:damage_modifier_adder_uuid").getBytes());
    private static final UUID DAMAGE_MODIFIER_MULTIPLiER_UUID =
            UUID.nameUUIDFromBytes(("isaac_disaster:damage_modifier_multiplier_uuid").getBytes());
    private static final UUID LUCK_MODIFIER_ADDER =
            UUID.nameUUIDFromBytes(("isaac_disaster:luck_modifier_adder").getBytes());


    /**
     * MAX HEALTH
     */
    public static int getHealthBonus() {
        return HEALTH_BONUS.get(); // 从配置中获取
    }

    /**
     * 修改玩家最大生命值
     * @param player 目标玩家
     * @param ratio 相对于*基础值*的比例变化
     */
    public static void modifyMaxHealth(Player player, double ratio) {
        // 防止玩家出于某种情况没有max_health属性 | 当玩家生命值超过设定上限时
        AttributeInstance healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute == null) {
            return;
        }

        // 获取当前已有的生命值上限加成 (若不存在则加值为0.0)
        AttributeModifier healthModifierAdder = healthAttribute.getModifier(MAX_HEALTH_MODIFIER_ADDER_UUID);
        double newBoost = getFinalMaxHealthAdder(ratio, healthModifierAdder, healthAttribute);

        player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                playerStatModifier -> {playerStatModifier.setMaxHealthAdder(player, newBoost);
                });
    }

    /**
     * 计算最终加成的函数
     */
    private static double getFinalMaxHealthAdder(double ratio, AttributeModifier healthModifierAdder, AttributeInstance healthAttribute) {
        double currentBoost = healthModifierAdder != null ? healthModifierAdder.getAmount() : 0.0;

        // 计算新的加成值
        double newBoost = currentBoost + StatManager.getHealthBonus() * ratio;

        // 确保玩家的血量有保底
        if(ratio < 0){
            double totalMaxHealth = healthAttribute.getValue();
            if(newBoost + totalMaxHealth <= 0){
                newBoost = -totalMaxHealth;
            }
        }

        // 更新healthModifier，需要在Capability中记录后运行
        return newBoost;
    }

    /**
     * 恢复生命；可以用来扣血
     * @param ratio 相对于*基础值*的回复量
     */
    public static void healHealth(Player player, float ratio){
        float amount = ratio * StatManager.getHealthBonus();
        //回复amount点生命值
        float currentHealth = player.getHealth();
        float maxHealth = player.getMaxHealth();
        // 确保生命值不超过最大生命值；可以低于0，即死亡
        float newHealth = Math.min(currentHealth + amount, maxHealth);
        player.setHealth(newHealth);

    }

    public static void updateHealthAdder(Player player, AttributeInstance attribute, double totalBoost) {
        // 移除旧修饰符
        attribute.removeModifier(MAX_HEALTH_MODIFIER_ADDER_UUID);
        // 添加新修饰符
        attribute.addPermanentModifier(new AttributeModifier(
                MAX_HEALTH_MODIFIER_ADDER_UUID,
                "isaac_disaster:max_health_boost",
                totalBoost,
                AttributeModifier.Operation.ADDITION
        ));
        // 刷新显示
        player.setHealth(player.getHealth());
    }


    /**
     * MOVEMENT SPEED
     */
    public static double getMovementSpeedBonus(){
        return MOVEMENT_SPEED_BONUS.get();
    }

    public static double getMovementSpeedLimit(){
        return MOVEMENT_SPEED_LIMIT.get();
    }

    public static void modifyMovementSpeedAdder(Player player, double ratio){
        AttributeInstance movementSpeedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeedAttribute == null) {
            return;
        }

        // 获取当前已有的移速加成 (若不存在则加值为0.0)
        AttributeModifier movementSpeedAdder = movementSpeedAttribute.getModifier(MOVEMENT_SPEED_MODIFIER_ADDER_UUID);
        double currentBoost = movementSpeedAdder != null ? movementSpeedAdder.getAmount() : 0.0;
        double newBoost = currentBoost + StatManager.getMovementSpeedBonus() * ratio;

        player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                playerStatModifier -> {playerStatModifier.setMovementSpeedAdder(player, newBoost);
                });
    }


    public static void updateMovementSpeedAdder(Player player, AttributeInstance attribute, double totalBoost) {
        // 不能超过限制的最大值或小于-0.07；但可以溢出
        totalBoost = Math.max(Math.min(totalBoost, getMovementSpeedLimit()),-0.07);
        // 移除旧修饰符
        attribute.removeModifier(MOVEMENT_SPEED_MODIFIER_ADDER_UUID);
        // 添加新修饰符
        attribute.addPermanentModifier(new AttributeModifier(
                MOVEMENT_SPEED_MODIFIER_ADDER_UUID,
                "isaac_disaster:movement_speed_adder",
                totalBoost,
                AttributeModifier.Operation.ADDITION
        ));
    }

    /**
     * DAMAGE
     */
    public static double getDamageBonus(){
        return DAMAGE_BONUS.get();
    }

    public static void modifyDamageAdder(Player player, double ratio){
        AttributeInstance damageAttribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttribute == null) {
            return;
        }

        // 获取当前已有的加成 (若不存在则加值为0.0)
        AttributeModifier damageAdder = damageAttribute.getModifier(DAMAGE_MODIFIER_ADDER_UUID);
        double currentBoost = damageAdder != null ? damageAdder.getAmount() : 0.0;

        // 计算新的加成值
        double newBoost = currentBoost + StatManager.getDamageBonus() * ratio;

        player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                playerStatModifier -> {playerStatModifier.setDamageAdder(player, newBoost);
                });
    }

    public static void updateDamageAdder(Player player, AttributeInstance attribute, double totalBoost) {
        // 不能小于-0.9 但可以溢出
        totalBoost = Math.max(totalBoost,-0.9);
        // 移除旧修饰符
        attribute.removeModifier(DAMAGE_MODIFIER_ADDER_UUID);
        // 添加新修饰符
        attribute.addPermanentModifier(new AttributeModifier(
                DAMAGE_MODIFIER_ADDER_UUID,
                "isaac_disaster:damage_adder",
                totalBoost,
                AttributeModifier.Operation.ADDITION
        ));
    }


    /**
     * 直接输入数值
     * @param amount 代表乘算数值。举例：0.5为+50%；-0.3为-30%
     */
    public static void modifyDamageMultiplier(Player player, double amount){
        AttributeInstance damageAttribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttribute == null) {
            return;
        }

        // 获取当前已有的加成 (若不存在则加值为0.0)
        AttributeModifier damageMultiplier = damageAttribute.getModifier(DAMAGE_MODIFIER_MULTIPLiER_UUID);
        double currentBoost = damageMultiplier != null ? damageMultiplier.getAmount() : 0.0;

        // 计算新的加成值
        double newBoost = currentBoost + amount;

        player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                playerStatModifier -> {playerStatModifier.setDamageMultiplier(player, newBoost);
                });
    }

    public static void updateDamageMultiplier(Player player, AttributeInstance attribute, double totalBoost) {
        // 不能小于-0.9 但可以溢出
        totalBoost = Math.max(totalBoost,-0.9);
        // 移除旧修饰符
        attribute.removeModifier(DAMAGE_MODIFIER_MULTIPLiER_UUID);
        // 添加新修饰符
        attribute.addPermanentModifier(new AttributeModifier(
                DAMAGE_MODIFIER_MULTIPLiER_UUID,
                "isaac_disaster:damage_adder",
                totalBoost,
                AttributeModifier.Operation.MULTIPLY_BASE
        ));
    }

    /**
     * LUCK
     */
    public static double getLuckBonus(){
        return LUCK_BONUS.get();
    }

    public static void modifyLuckAdder(Player player, double ratio){
        AttributeInstance luckAttribute = player.getAttribute(Attributes.LUCK);
        if (luckAttribute == null) {
            return;
        }

        // 获取当前已有的加成 (若不存在则加值为0.0)
        AttributeModifier luckAdder = luckAttribute.getModifier(LUCK_MODIFIER_ADDER);
        double currentBoost = luckAdder != null ? luckAdder.getAmount() : 0.0;

        // 计算新的加成值
        double newBoost = currentBoost + StatManager.getLuckBonus() * ratio;

        player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                playerStatModifier -> {playerStatModifier.setLuckAdder(player, newBoost);
                });
    }

    public static void updateLuckAdder(Player player, AttributeInstance attribute, double totalBoost) {
        // 移除旧修饰符
        attribute.removeModifier(LUCK_MODIFIER_ADDER);
        // 添加新修饰符
        attribute.addPermanentModifier(new AttributeModifier(
                LUCK_MODIFIER_ADDER,
                "isaac_disaster:luck_adder",
                totalBoost,
                AttributeModifier.Operation.ADDITION
        ));
    }

    /**
     * FLY
     */
    public static double getFlyTime(){
        return FLY_TIME.get();
    }
}