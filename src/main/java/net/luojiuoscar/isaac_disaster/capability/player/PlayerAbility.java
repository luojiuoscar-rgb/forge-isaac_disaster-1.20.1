package net.luojiuoscar.isaac_disaster.capability.player;


import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.helper.ColorHelper;
import net.luojiuoscar.isaac_disaster.item.pickup.Pill;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPillEffect;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PillEffectManager;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.PillQualitySyncS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.PillRecordsSyncS2CPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

import java.util.*;


@AutoRegisterCapability
public class PlayerAbility {
    private boolean holdRightClick;
    private int piercing;
    private int homing;
    private int spectral;
    private int controllable;
    private Map<Integer, Integer> bulletFilters;
    private int pillQuality;

    // 记录玩家pillId -> EffectId的序列
    private Map<Integer, Integer> pillRecords;


    public PlayerAbility(){
        holdRightClick = false;
        piercing = 0;
        homing = 0;
        spectral = 0;
        controllable = 0;

        bulletFilters = new HashMap<>();
        pillRecords = new HashMap<>();
    }

    public boolean isHoldRightClick(){
        return holdRightClick;
    }
    public void setHoldRightClick(boolean holdRightClick){
        this.holdRightClick = holdRightClick;
    }
    public int getPiercing() {
        return piercing;
    }
    public void setPiercing(int amount){
        piercing = amount;
    }
    public int getHoming() {
        return homing;
    }
    public void setHoming(int amount){
        homing = amount;
    }
    public int getSpectral() {
        return spectral;
    }
    public void setSpectral(int amount){
        spectral = amount;
    }
    public int getControllable() {
        return controllable;
    }
    public void setControllable(int amount){
        controllable = amount;
    }
    public void addFilter(int color, Player player) {
        bulletFilters.put(color, bulletFilters.getOrDefault(color, 0));
        resetFilter(player);
    }
    public void removeFilter(int color, Player player) {
        bulletFilters.remove(color, 1);
        if (bulletFilters.getOrDefault(color, 0) == 0){
            bulletFilters.remove(color);
        }
        resetFilter(player);
    }
    public void clearFilters() {
        bulletFilters.clear();
    }
    public Map<Integer, Integer> getFilters() {
        return Collections.unmodifiableMap(bulletFilters);
    }
    public void resetFilter(Player player){
        AttributeInstance instance = player.getAttribute(ModAttributes.BULLET_FILTER.get());
        if (instance != null) instance.setBaseValue(ColorHelper.blendFilters(ColorManager.FILTER_BASE,
                bulletFilters.keySet().stream().toList()));
    }
    public int getPillQuality(){return pillQuality;}
    public void setPillQuality(ServerPlayer player, int quality){
        this.pillQuality = quality;
        ModMessages.sentToPlayer(new PillQualitySyncS2CPacket(this.pillQuality), player);
    }
    public Map<Integer, Integer> getPillRecordsMap() {
        return Collections.unmodifiableMap(pillRecords);
    }
    public void setPillRecord(ServerPlayer player, int pillId, int effectId) {
        bulletFilters.put(pillId, effectId);
        // 同步到客户端
        ModMessages.sentToPlayer(new PillRecordsSyncS2CPacket(pillId, effectId), player);
    }

    /**
     * 复制玩家属性
     * 复制后立刻触发属性修改；以确保玩家属性正确继承
     */
    public void copyFrom(PlayerAbility source, Player player) {
        this.holdRightClick = source.holdRightClick;
        this.piercing = source.piercing;
        this.homing = source.homing;
        this.spectral = source.spectral;
        this.controllable = source.controllable;
        this.pillQuality = source.pillQuality;

        this.bulletFilters = new HashMap<>(source.bulletFilters);
        this.pillRecords = new HashMap<>(source.pillRecords);
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putBoolean("holdRightClick", holdRightClick);
        nbt.putInt("piercing", piercing);
        nbt.putInt("homing", homing);
        nbt.putInt("spectral", spectral);
        nbt.putInt("controllable", controllable);
        nbt.putInt("pill_quality", pillQuality);

        // 滤镜
        ListTag filterList = new ListTag();
        for (Map.Entry<Integer, Integer> entry : bulletFilters.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("color", entry.getKey());
            tag.putInt("count", entry.getValue());
            filterList.add(tag);
        }
        nbt.put("bullet_filters", filterList);

        // 药丸记录
        ListTag pillRecordsList = new ListTag();
        for (Map.Entry<Integer, Integer> entry : pillRecords.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("pill_id", entry.getKey());
            tag.putInt("effect_id", entry.getValue());
            pillRecordsList.add(tag);
        }
        nbt.put("pill_records", pillRecordsList);
    }
    public void loadNBTData(CompoundTag nbt) {
        this.holdRightClick = nbt.getBoolean("holdRightClick");
        this.spectral = nbt.getInt("spectral");
        this.piercing = nbt.getInt("piercing");
        this.homing = nbt.getInt("homing");
        this.controllable = nbt.getInt("controllable");
        this.pillQuality = nbt.getInt("pill_quality");

        bulletFilters.clear();
        if (nbt.contains("bullet_filters", Tag.TAG_LIST)) {
            ListTag list = nbt.getList("bullet_filters", Tag.TAG_COMPOUND);
            for (Tag t : list) {
                CompoundTag tag = (CompoundTag) t;
                int color = tag.getInt("color");
                int count = tag.getInt("count");
                bulletFilters.put(color, count);
            }
        }

        bulletFilters.clear();
        if (nbt.contains("pill_records", Tag.TAG_LIST)) {
            ListTag list = nbt.getList("pill_records", Tag.TAG_COMPOUND);
            for (Tag t : list) {
                CompoundTag tag = (CompoundTag) t;
                int pillId = tag.getInt("pill_id");
                int effectId = tag.getInt("effect_id");
                bulletFilters.put(pillId, effectId);
            }
        }
    }
}
