package net.luojiuoscar.isaac_disaster.manager;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerStatModifierProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.UUID;

import static net.luojiuoscar.isaac_disaster.Config.*;

/**
 * 属性控制器，统一管理所有道具对玩家的属性修改
 */
public class StatManager {


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
    public static void modifyAdder(Player player, UUID uuid, double amount,
                                   @Nullable Double minValue, @Nullable Double maxvalue){
        AttributeInstance instance = player.getAttribute(UUIDManager.ATTRIBUTE_FROM_UUID.get(uuid));
        if (instance == null) return;

        // 获取当前已有的加成 (若不存在则加值为0.0)
        AttributeModifier adder = instance.getModifier(uuid);
        double currentBoost = adder != null ? adder.getAmount() : 0.0;

        // 计算新的加成值
        double newBoost = currentBoost + amount;

        if (minValue != null && minValue > newBoost) newBoost = minValue;
        if (maxvalue != null && maxvalue < newBoost) newBoost = maxvalue;

        updateAdder(player, instance, newBoost, uuid, "");
    }
    public static void modifyMultiplier(Player player, UUID uuid, double amount,
                                        @Nullable Double minValue, @Nullable Double maxvalue){
        AttributeInstance instance = player.getAttribute(UUIDManager.ATTRIBUTE_FROM_UUID.get(uuid));
        if (instance == null) return;

        // 获取当前已有的加成 (若不存在则加值为0.0)
        AttributeModifier multiplier = instance.getModifier(uuid);
        double currentBoost = multiplier != null ? multiplier.getAmount() : 0.0;

        // 计算新的加成值
        double newBoost = currentBoost + amount;

        if (minValue != null && minValue > newBoost) newBoost = minValue;
        if (maxvalue != null && maxvalue < newBoost) newBoost = maxvalue;


        updateMultiplier(player, instance, newBoost, uuid, "");
    }


    /**
     * MAX HEALTH
     */
    public static int getHealthBonus() {
        return HEALTH_BONUS.get();
    }
    public static void modifyMaxHealth(Player player, double ratio){
        modifyAdder(player, UUIDManager.MAX_HEALTH_MODIFIER_ADDER,getHealthBonus()*ratio,
                -20.0, null);
        player.setHealth(player.getHealth()); // 刷新血量
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
        modifyAdder(player, UUIDManager.MOVEMENT_SPEED_MODIFIER_ADDER,getMovementSpeedBonus()*ratio,
                null, null);
    }

    /**
     * DAMAGE
     */
    public static double getDamageBonus(){
        return DAMAGE_BONUS.get();
    }
    public static void modifyDamageAdder(Player player, double ratio) {
        modifyAdder(player, UUIDManager.DAMAGE_MODIFIER_ADDER,getDamageBonus()*ratio,
                0.1, null);
    }
    /**
     * 直接输入数值
     * @param amount 代表乘算数值。举例：0.5为+50%；-0.3为-30%
     */
    public static void modifyDamageMultiplier(Player player, double amount){
        modifyMultiplier(player, UUIDManager.DAMAGE_MODIFIER_MULTIPLIER, amount, 0.1, null);
    }

    /**
     * LUCK
     */
    public static double getLuckBonus(){
        return LUCK_BONUS.get();
    }
    public static void modifyLuckAdder(Player player, double ratio){
        modifyAdder(player, UUIDManager.LUCK_MODIFIER_ADDER,getLuckBonus()*ratio,
                null, null);
    }

    /**
     * SCALE
     */
    public static double getScaleBonus(){
        return SCALE_BONUS.get();
    }
    public static void modifyScaleAdder(Player player, double ratio){
        modifyAdder(player, UUIDManager.SCALE_MODIFIER_ADDER,getScaleBonus()*ratio,
                null, null);    }

    /**
     * FLY
     */
    public static void modifyFlyTime(Player player, double ratio){
        player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                playerStatModifier -> {
                    playerStatModifier.addFlyTime(player, ratio*StatManager.getFlyTime());
                }
        );
    }

    /**
     * RANGE
     */
    public static double getRangeBonus(){return RANGE_BONUS.get();}
    public static void modifyRangeAdder(Player player, double ratio){
        modifyAdder(player, UUIDManager.RANGE_MODIFIER_ADDER,getRangeBonus()*ratio,
                null, null);    }

    /**
     * ENTITY REACH
     */
    public static double getEntityReachBonus(){return ENTITY_REACH_BONUS.get();}
    public static void modifyEntityReachAdder(Player player, double ratio){
        modifyAdder(player, UUIDManager.ENTITY_REACH_MODIFIER_ADDER, getEntityReachBonus()*ratio,
                null, null);    }

    /**
     * BLOCK REACH
     */
    public static double getBlockReachBonus(){return BLOCK_REACH_BONUS.get();}
    public static void modifyBlockReachAdder(Player player, double ratio){
        modifyAdder(player, UUIDManager.BLOCK_REACH_MODIFIER_ADDER, getBlockReachBonus()*ratio,
                null, null);    }

    /**
     * BULLET SPEED
     */
    public static double getBulletSpeed(){return BULLET_SPEED_BONUS.get();}
    public static void modifyBulletSpeedAdder(Player player, double ratio){
        modifyAdder(player, UUIDManager.BULLET_SPEED_MODIFIER_ADDER,getBulletSpeed()*ratio,
                null, null);    }

    /**
     * TEARS
     */
    public static double getTears(){return TEARS_BONUS.get();}
    public static void modifyTearsAdder(Player player, double ratio){
        modifyAdder(player, UUIDManager.TEARS_MODIFIER_ADDER,getTears()*ratio,
                null, null);    }

    /**
     * TEARS CORRECTION
     */
    public static double getTearsCorrectionBonus(){return TEARS_CORRECTION_BONUS.get();}
    public static void modifyTearsCorrectionAdder(Player player, double ratio){
        modifyAdder(player, UUIDManager.TEARS_CORRECTION_MODIFIER_ADDER, getTearsCorrectionBonus()*ratio,
                null, null);    }

    /**
     * ATTACK SPEED
     */
    public static double getAttackSpeed(){return ATTACK_SPEED_BONUS.get();}
    public static void modifyAttackSpeedAdder(Player player, double ratio){
        modifyAdder(player, UUIDManager.ATTACK_SPEED_MODIFIER_ADDER,getAttackSpeed()*ratio,
                null, null);    }

    /**
     * BULLET EFFECTS
     */
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

    /**
     * BLOCK BREAKING SPEED
     */
    public static double getBlockBreakingSpeed(){return BLOCK_BREAKING_SPEED_BONUS.get();}
    public static void modifyBlockBreakingSpeedAdder(Player player, double ratio){
        modifyAdder(player, UUIDManager.BLOCK_BREAKING_SPEED_BONUS,getBlockBreakingSpeed()*ratio,
                null, null);
    }

    /**
     * BLOCK SCALE
     */
    public static double getBulletScaleBonus(){return BULLET_SCALE_BONUS.get();}
    public static void modifyBulletScaleAdder(Player player, double ratio){
        modifyAdder(player, UUIDManager.BULLET_SCALE_BONUS,getBulletScaleBonus()*ratio,
                null, null);
    }

    /**
     * ATTACK KNOCKBACK
     */
    public static double getAttackKnockbackBonus(){return ATTACK_KNOCKBACK_BONUS.get();}
    public static void modifyAttackKnockBackAdder(Player player, double ratio){
        modifyAdder(player, UUIDManager.ATTACK_KNOCKBACK_BONUS,getAttackKnockbackBonus()*ratio,
                null, null);
    }

    /**
     * SET
     */
    public static void modifySetWithId(Player player, int setId, int amount){
        if (player instanceof ServerPlayer serverPlayer){
            player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                    playerPassiveItem -> playerPassiveItem.modifySetCount(serverPlayer, setId, amount)
            );
        }
    }

}


