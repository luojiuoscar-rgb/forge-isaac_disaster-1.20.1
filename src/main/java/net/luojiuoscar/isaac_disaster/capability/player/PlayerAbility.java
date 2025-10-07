package net.luojiuoscar.isaac_disaster.capability.player;


import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.UUIDManager;
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
public class PlayerAbility {
    private boolean holdRightClick;


    // constructor
    public PlayerAbility(){
        holdRightClick = false;
    }

    public boolean isHoldRightClick(){
        return holdRightClick;
    }

    public void setHoldRightClick(boolean holdRightClick){
        this.holdRightClick = holdRightClick;
    }


    /**
     * 复制玩家属性
     * 复制后立刻触发属性修改；以确保玩家属性正确继承
     */
    public void copyFrom(PlayerAbility source, Player player) {
        this.holdRightClick = source.holdRightClick;
    }

    public void saveNBTData(CompoundTag nbt) {

        nbt.putBoolean("holdRightClick", holdRightClick);
    }
    public void loadNBTData(CompoundTag nbt) {

        this.holdRightClick = nbt.getBoolean("holdRightClick");
    }
}
