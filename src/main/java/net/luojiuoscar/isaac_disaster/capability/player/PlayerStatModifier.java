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

    // constructor
    public PlayerStatModifier(){
        maxHealthAdder = 0;
        movementSpeedAdder = 0;
    }


    public void setMaxHealthAdder(Player player, double amount){
        maxHealthAdder = amount;
        StatManager.updateHealthAdder(player, (Objects.requireNonNull(player.getAttribute(Attributes.MAX_HEALTH))), this.maxHealthAdder);
    }

    public void setMovementSpeedAdder(Player player, double amount){
        movementSpeedAdder = amount;
        StatManager.updateMovementSpeedAdder(player, (Objects.requireNonNull(player.getAttribute(Attributes.MOVEMENT_SPEED))), this.movementSpeedAdder);
    }

    public double getMaxHealth(){
        return maxHealthAdder;
    }
    public double getMovementSpeedAdder(){
        return movementSpeedAdder;
    }

    /**
     * 复制玩家属性
     * 无论何时、复制玩后立刻触发属性修改
     */
    public void copyFrom(PlayerStatModifier source, Player player) {
        // max health adder
        setMaxHealthAdder(player, source.maxHealthAdder);
        setMovementSpeedAdder(player, source.movementSpeedAdder);
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putDouble("max_health_adder", maxHealthAdder);
        nbt.putDouble("movement_speed_adder", movementSpeedAdder);
    }


    public void loadNBTData(CompoundTag nbt) {
        this.maxHealthAdder = nbt.getDouble("max_health_adder");
        this.movementSpeedAdder = nbt.getDouble("movement_speed_adder");
    }
}
