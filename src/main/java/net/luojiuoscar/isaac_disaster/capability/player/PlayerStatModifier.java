package net.luojiuoscar.isaac_disaster.capability.player;


import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

import java.util.Objects;


@AutoRegisterCapability
public class PlayerStatModifier {
    //将玩家新增的属性附加到玩家身上
    private double maxHealthAdder;
    private double movementSpeedAdder;
    private double damageAdder;
    private double damageMultiplier;

    // constructor
    public PlayerStatModifier(){
        maxHealthAdder = 0;
        movementSpeedAdder = 0;
        damageAdder = 0;
        damageMultiplier = 0;
    }

    /**
     * Getter
     */
    public double getMaxHealth(){
        return maxHealthAdder;
    }
    public double getMovementSpeedAdder(){
        return movementSpeedAdder;
    }
    public double getDamageAdder(){
        return damageAdder;
    }
    public double getDamageMultiplier(){
        return damageMultiplier;
    }

    /**
     * Setter
     */
    public void setMaxHealthAdder(Player player, double amount){
        maxHealthAdder = amount;
        StatManager.updateHealthAdder(player, (Objects.requireNonNull(player.getAttribute(Attributes.MAX_HEALTH))), this.maxHealthAdder);
    }

    public void setMovementSpeedAdder(Player player, double amount){
        movementSpeedAdder = amount;
        StatManager.updateMovementSpeedAdder(player, (Objects.requireNonNull(player.getAttribute(Attributes.MOVEMENT_SPEED))), this.movementSpeedAdder);
    }

    public void setDamageAdder(Player player, double amount){
        damageAdder = amount;
        StatManager.updateDamageAdder(player, (Objects.requireNonNull(player.getAttribute(Attributes.ATTACK_DAMAGE))), this.damageAdder);
    }

    public void setDamageMultiplier(Player player, double amount){
        damageMultiplier = amount;
        StatManager.updateDamageMultiplier(player, (Objects.requireNonNull(player.getAttribute(Attributes.ATTACK_DAMAGE))), this.damageMultiplier);
    }

    /**
     * 复制玩家属性
     * 复制后立刻触发属性修改；以确保玩家属性正确继承
     */
    public void copyFrom(PlayerStatModifier source, Player player) {
        // max health adder
        setMaxHealthAdder(player, source.maxHealthAdder);
        setMovementSpeedAdder(player, source.movementSpeedAdder);
        setDamageAdder(player, source.damageAdder);
        setDamageMultiplier(player, source.damageMultiplier);
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putDouble("max_health_adder", maxHealthAdder);
        nbt.putDouble("movement_speed_adder", movementSpeedAdder);
        nbt.putDouble("damage_adder", damageAdder);
        nbt.putDouble("damage_multiplier", damageMultiplier);
    }

    public void loadNBTData(CompoundTag nbt) {
        this.maxHealthAdder = nbt.getDouble("max_health_adder");
        this.movementSpeedAdder = nbt.getDouble("movement_speed_adder");
        this.damageAdder = nbt.getDouble("damage_adder");;
        this.damageMultiplier = nbt.getDouble("damage_multiplier");
    }
}
