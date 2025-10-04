package net.luojiuoscar.isaac_disaster.capability.player;


import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.FlyUpdateS2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import org.checkerframework.checker.units.qual.C;

import java.util.Objects;


@AutoRegisterCapability
public class PlayerStatModifier {
    //将玩家新增的属性附加到玩家身上
    private double maxHealthAdder;
    private double movementSpeedAdder;
    private double damageAdder;
    private double damageMultiplier;
    private double luckAdder;
    private double flyTime;
    private double flyTimeCurrent;

    // constructor
    public PlayerStatModifier(){
        maxHealthAdder = 0;
        movementSpeedAdder = 0;
        damageAdder = 0;
        damageMultiplier = 0;
        luckAdder = 0;
        flyTime = 0;
        flyTimeCurrent = 0;
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
    public double getLuckAdder(){
        return luckAdder;
    }
    public double getFlyTime(){
        return flyTime;
    }
    public double getFlyTimeCurrent(){
        return flyTimeCurrent;
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

    public void setLuckAdder(Player player, double amount){
        luckAdder = amount;
        StatManager.updateLuckAdder(player, (Objects.requireNonNull(player.getAttribute(Attributes.LUCK))), this.luckAdder);
    }

    public void setFlyTime(Player player, double amount){
        flyTime = amount;
        if (flyTime > 0){
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        }else{
            player.getAbilities().flying = false;
            player.getAbilities().mayfly = false;
            player.onUpdateAbilities();
        }

    }

    public void addFlyTime(Player player, double amount){
        setFlyTime(player, flyTime + amount);
        if (flyTime > 0){
            addCurrentFlyTime((ServerPlayer) player, (int) -amount);
        }
    }

    public void addCurrentFlyTime(ServerPlayer player, int amount){
        double pre = flyTimeCurrent % (flyTime / 20);

        flyTimeCurrent = Math.max(0, flyTimeCurrent + amount);

        // 时间到达上限（且没有飞升效果）则取消飞行
        if (flyTimeCurrent >= flyTime && player.getEffect(ModEffects.TRANSCENDENCE.get()) == null){
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }

        if (flyTimeCurrent < 0 || flyTimeCurrent > flyTime) return;
        // 当飞行能力变化的情况越过了1/20（即存在状态更新）时，发包更新客户端数据
        double curr = pre + amount;
        if (curr >= (flyTime / 20) || curr < 0){
            // 把百分比映射为 0..20
            int units = 20 - (int) Math.max((flyTimeCurrent / flyTime * 20), 0);
            ModMessages.sentToPlayer(new FlyUpdateS2CPacket(units), player);
        }
    }


    /**
     * 复制玩家属性
     * 复制后立刻触发属性修改；以确保玩家属性正确继承
     */
    public void copyFrom(PlayerStatModifier source, Player player) {
        setMaxHealthAdder(player, source.maxHealthAdder);
        setMovementSpeedAdder(player, source.movementSpeedAdder);
        setDamageAdder(player, source.damageAdder);
        setDamageMultiplier(player, source.damageMultiplier);
        setLuckAdder(player, source.luckAdder);
        setFlyTime(player, source.flyTime);
        this.flyTime = 0;
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putDouble("max_health_adder", maxHealthAdder);
        nbt.putDouble("movement_speed_adder", movementSpeedAdder);
        nbt.putDouble("damage_adder", damageAdder);
        nbt.putDouble("damage_multiplier", damageMultiplier);
        nbt.putDouble("luck_adder", luckAdder);
        nbt.putDouble("fly_time", flyTime);
    }

    public void loadNBTData(CompoundTag nbt) {
        this.maxHealthAdder = nbt.getDouble("max_health_adder");
        this.movementSpeedAdder = nbt.getDouble("movement_speed_adder");
        this.damageAdder = nbt.getDouble("damage_adder");;
        this.damageMultiplier = nbt.getDouble("damage_multiplier");
        this.luckAdder = nbt.getDouble("luck_adder");
        this.flyTime = nbt.getDouble("fly_time");
        this.flyTimeCurrent = 0;
    }
}
