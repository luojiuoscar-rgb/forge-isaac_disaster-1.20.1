package net.luojiuoscar.isaac_disaster.manager;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

import static net.luojiuoscar.isaac_disaster.Config.BASE_HEALTH_BONUS;

/**
 * 属性控制器，统一管理所有道具对玩家的属性修改
 */
public class StatManager {
    private static final UUID MAX_HEALTH_MODIFIER_ADDER_UUID =
            UUID.nameUUIDFromBytes(("isaac_disaster:max_health_modifier_adder_location").getBytes());

    /**
     * 修改玩家最大生命值
     * @param player 目标玩家
     * @param changeRatio 相对基础值的比例变化
     * @param healRatio 基于本次最大生命变化量的回复比例
     */
    public static void modifyMaxHealth(Player player, float changeRatio, float healRatio) {
        // 防止玩家出于某种情况没有max_health属性 | 当玩家生命值超过设定上限时
        AttributeInstance healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute == null) {
            return;
        }

        // 获取当前已有的生命值上限加成 (若不存在则加值为0.0)
        AttributeModifier healthModifierAdder = healthAttribute.getModifier(MAX_HEALTH_MODIFIER_ADDER_UUID);
        double currentBoost = healthModifierAdder != null ? healthModifierAdder.getAmount() : 0.0;

        // 计算新的加成值
        double newBoost = currentBoost + StatManager.getBaseHealthBonus() * changeRatio;

        // 更新属性修饰符
        updateHealthModifier(healthAttribute, newBoost);

        // 计算并恢复生命值
        healHealth(player, StatManager.getBaseHealthBonus() * healRatio);
    }

    private static void healHealth(Player player, float amount){
        //回复amount点生命值
        if (amount > 0) {
            float currentHealth = player.getHealth();
            float maxHealth = player.getMaxHealth();
            // 确保生命值不超过最大生命值
            float newHealth = Math.min(currentHealth + amount, maxHealth);
            player.setHealth(newHealth);
        }
    }

    private static void updateHealthModifier(AttributeInstance attribute, double totalBoost) {
        // 先移除旧修饰符
        attribute.removeModifier(MAX_HEALTH_MODIFIER_ADDER_UUID);
        // 仅当有正加成时添加新修饰符
        if (totalBoost > 0) {
            attribute.addPermanentModifier(new AttributeModifier(
                    MAX_HEALTH_MODIFIER_ADDER_UUID,
                    "isaac_disaster:max_health_boost",
                    totalBoost,
                    AttributeModifier.Operation.ADDITION
            ));
        }
    }

    public static int getBaseHealthBonus() {
        return BASE_HEALTH_BONUS.get(); // 从配置中获取
    }
}