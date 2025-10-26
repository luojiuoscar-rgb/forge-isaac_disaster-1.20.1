package net.luojiuoscar.isaac_disaster.capability.player;


import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.FlyUpdateS2CPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@AutoRegisterCapability
public class PlayerStatModifier {
    private Map<UUID, Double> playerModifiers;
    private double flyTimeCurrent;

    private int doubleShotDelay;

    // constructor
    public PlayerStatModifier(){
        init();
    }

    public void init(){
        playerModifiers = new HashMap<>();

        flyTimeCurrent = 0;
        doubleShotDelay = 0;
    }



    /**
     * Getter
     */
    public double getModifier(UUID uuid){
        return playerModifiers.getOrDefault(uuid, 0.0);
    }

    public double getFlyTimeCurrent(){
        return flyTimeCurrent;
    }
    public int getDoubleShotDelay(){
        return doubleShotDelay;
    }
    /**
     * Setter
     */
    public void setModifier(UUID uuid, double amount){
        playerModifiers.put(uuid, amount);
    }
    public void removeModifier(UUID uuid){
        playerModifiers.remove(uuid);
    }


    public void addCurrentFlyTime(ServerPlayer player, int amount){
        double flyTime = PlayerHelper.getFly(player);

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
    public void setDoubleShotDelay(Player player, int amount){
        this.doubleShotDelay = amount;
    }
    public void modifyDoubleShotDelay(Player player, int amount){
        this.doubleShotDelay += amount;
    }


    private void refreshAllFromSource(Player player, PlayerStatModifier source){
        for (Map.Entry<UUID, Double> entry : source.playerModifiers.entrySet()) {
            UUID uuid = entry.getKey();
            AttributeInstance instance = player.getAttribute(StatManager.fromUUID(uuid).getAttribute());
            if (instance == null) continue;

            // 重新设置uuid 并且通过这个设置函数更新玩家的attribute记录
            if (!StatManager.fromUUID(uuid).isMultiplyBase()){
                StatManager.setModifierAdd(
                        player,
                        instance,
                        entry.getValue(),
                        uuid,
                        ""
                );
            }
            else{
                StatManager.setModifierMultiplyBase(
                        player,
                        instance,
                        entry.getValue(),
                        uuid,
                        ""
                );
            }
        }
    }



    /**
     * 复制玩家属性
     * 复制后立刻触发属性修改；以确保玩家属性正确继承
     */
    public void copyFrom(PlayerStatModifier source, Player player) {
        refreshAllFromSource(player, source);
        this.doubleShotDelay = 0;
    }

    public void saveNBTData(CompoundTag nbt) {

        nbt.putInt("double_shot_delay", doubleShotDelay);

        // 保存 playerModifiers
        ListTag listTag = new ListTag();
        for (Map.Entry<UUID, Double> entry : playerModifiers.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("uuid", entry.getKey());
            tag.putDouble("value", entry.getValue());
            listTag.add(tag);
        }
        nbt.put("player_modifiers", listTag);
    }
    public void loadNBTData(CompoundTag nbt) {

        this.flyTimeCurrent = 0;
        this.doubleShotDelay = nbt.getInt("double_shot_delay");

        // 读取 playerModifiers
        this.playerModifiers.clear();
        if (nbt.contains("player_modifiers", Tag.TAG_LIST)) {
            ListTag listTag = nbt.getList("player_modifiers", Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag tag = listTag.getCompound(i);
                UUID uuid = tag.getUUID("uuid");
                double value = tag.getDouble("value");
                this.playerModifiers.put(uuid, value);
            }
        }
    }
}
