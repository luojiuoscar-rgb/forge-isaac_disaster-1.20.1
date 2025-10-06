package net.luojiuoscar.isaac_disaster.manager;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerStatModifierProvider;
import net.minecraft.network.chat.Component;
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
    /**
     * SPECIAL GETTERS
     */
    public static double getFlyTime(){return FLY_TIME.get();}
    public static double getDamageMultiplier1(){return DAMAGE_MULTIPLIER_1.get();}
    public static double getNearbyRange(){return NEARBY_RANGE.get();}
    public static double getHolyShieldStrength(){return HOLY_SHIELD_STRENGTH.get();}

    public static void updateAdder(Player player, AttributeInstance attribute, double totalBoost, UUID uuid, String name) {
        attribute.removeModifier(uuid);
        attribute.addPermanentModifier(new AttributeModifier(
                uuid,
                name,
                totalBoost,
                AttributeModifier.Operation.ADDITION
        ));

        player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                playerStatModifier -> {
                    playerStatModifier.setModifier(uuid, totalBoost);
                });
    }
    public static void updateMultiplier(Player player, AttributeInstance attribute, double totalBoost, UUID uuid, String name) {
        attribute.removeModifier(uuid);
        attribute.addPermanentModifier(new AttributeModifier(
                uuid,
                name,
                totalBoost,
                AttributeModifier.Operation.MULTIPLY_BASE
        ));

        player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                playerStatModifier -> {
                    playerStatModifier.setModifier(uuid, totalBoost);
                });
    }

    /**
     * MAX HEALTH
     */
    public static int getHealthBonus() {
        return HEALTH_BONUS.get();
    }
    public static void modifyMaxHealth(Player player, double ratio){
        modifyMaxHealth(player, ratio, UUIDManager.MAX_HEALTH_MODIFIER_ADDER);
    }
    public static void modifyMaxHealth(Player player, double ratio, UUID uuid) {
        // 防止玩家出于某种情况没有max_health属性 | 当玩家生命值超过设定上限时
        AttributeInstance healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute == null) return;

        // 获取当前已有的生命值上限加成 (若不存在则加值为0.0)
        AttributeModifier healthModifierAdder = healthAttribute.getModifier(uuid);
        double newBoost = getFinalMaxHealthAdder(ratio, healthModifierAdder, healthAttribute);

        updateAdder(player, healthAttribute, newBoost, uuid, "base_health_adder");
        player.setHealth(player.getHealth());
    }
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
    public static void gainAbsorption(Player player, float ratio){
        player.setAbsorptionAmount(player.getAbsorptionAmount() + getHealthBonus() * ratio);
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
        modifyMovementSpeedAdder(player, ratio, UUIDManager.MOVEMENT_SPEED_MODIFIER_ADDER);
    }
    public static void modifyMovementSpeedAdder(Player player, double ratio, UUID uuid){
        AttributeInstance movementSpeedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeedAttribute == null) return;

        // 获取当前已有的移速加成 (若不存在则加值为0.0)
        AttributeModifier movementSpeedAdder = movementSpeedAttribute.getModifier(uuid);
        double currentBoost = movementSpeedAdder != null ? movementSpeedAdder.getAmount() : 0.0;
        double newBoost = currentBoost + StatManager.getMovementSpeedBonus() * ratio;

        updateAdder(player, movementSpeedAttribute, newBoost, uuid, "base_speed_adder");
    }


    /**
     * DAMAGE
     */
    public static double getDamageBonus(){
        return DAMAGE_BONUS.get();
    }
    public static void modifyDamageAdder(Player player, double ratio) {
        modifyDamageAdder(player, ratio, UUIDManager.DAMAGE_MODIFIER_ADDER);
    }
    public static void modifyDamageAdder(Player player, double ratio, UUID uuid){
        AttributeInstance damageAttribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttribute == null) return;

        // 获取当前已有的加成 (若不存在则加值为0.0)
        AttributeModifier damageAdder = damageAttribute.getModifier(uuid);
        double currentBoost = damageAdder != null ? damageAdder.getAmount() : 0.0;

        // 计算新的加成值
        double newBoost = currentBoost + StatManager.getDamageBonus() * ratio;

        updateAdder(player, damageAttribute, newBoost, uuid, "base_damage_adder");
    }


    /**
     * 直接输入数值
     * @param amount 代表乘算数值。举例：0.5为+50%；-0.3为-30%
     */
    public static void modifyDamageMultiplier(Player player, double amount){
        modifyDamageMultiplier(player, amount, UUIDManager.DAMAGE_MODIFIER_MULTIPLIER);
    }
    public static void modifyDamageMultiplier(Player player, double amount, UUID uuid){
        AttributeInstance damageAttribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttribute == null) return;

        // 获取当前已有的加成 (若不存在则加值为0.0)
        AttributeModifier damageMultiplier = damageAttribute.getModifier(uuid);
        double currentBoost = damageMultiplier != null ? damageMultiplier.getAmount() : 0.0;

        // 计算新的加成值
        double newBoost = currentBoost + amount;

        updateMultiplier(player, damageAttribute, newBoost, uuid, "base_damage_multiplier");
    }


    /**
     * LUCK
     */
    public static double getLuckBonus(){
        return LUCK_BONUS.get();
    }
    public static void modifyLuckAdder(Player player, double ratio){
        modifyLuckAdder(player, ratio, UUIDManager.LUCK_MODIFIER_ADDER);
    }
    public static void modifyLuckAdder(Player player, double ratio, UUID uuid){
        AttributeInstance luckAttribute = player.getAttribute(Attributes.LUCK);
        if (luckAttribute == null) return;

        // 获取当前已有的加成 (若不存在则加值为0.0)
        AttributeModifier luckAdder = luckAttribute.getModifier(uuid);
        double currentBoost = luckAdder != null ? luckAdder.getAmount() : 0.0;

        // 计算新的加成值
        double newBoost = currentBoost + StatManager.getLuckBonus() * ratio;

        updateAdder(player, luckAttribute, newBoost, uuid, "base_luck_adder");

    }


    /**
     * SCALE
     */
    public static double getScaleBonus(){
        return SCALE_BONUS.get();
    }
    public static void modifyScaleAdder(Player player, double ratio){
        modifyScaleAdder(player, ratio, UUIDManager.SCALE_MODIFIER_ADDER);
    }
    public static void modifyScaleAdder(Player player, double ratio, UUID uuid){
        AttributeInstance scaleAttribute = player.getAttribute(ModAttributes.SCALE.get());
        if (scaleAttribute == null) return;

        // 获取当前已有的加成 (若不存在则加值为0.0)
        AttributeModifier scaleAdder = scaleAttribute.getModifier(uuid);
        double currentBoost = scaleAdder != null ? scaleAdder.getAmount() : 0.0;

        // 计算新的加成值
        double newBoost = currentBoost + StatManager.getScaleBonus() * ratio;

        updateAdder(player, scaleAttribute, newBoost, uuid, "base_scale_adder");
    }



}


