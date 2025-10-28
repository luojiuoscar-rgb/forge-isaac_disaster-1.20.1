package net.luojiuoscar.isaac_disaster.capability.player;


import net.luojiuoscar.isaac_disaster.helper.ColorHelper;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.PillRecordsSyncS2CPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


@AutoRegisterCapability
public class PlayerAbility {
    private boolean holdRightClick;

    private int piercing;
    private int homing;
    private int spectral;
    private int controllable;

    private Map<Integer, Integer> bulletFilters;

    private Map<Integer, Boolean> itemFlags;
    private int extraTrinketSlotCounts;

    // 记录玩家pillId -> EffectId的序列
    private Map<Integer, Integer> pillRecords;


    public PlayerAbility(){
        init();
    }

    public void init(){
        holdRightClick = false;
        piercing = 0;
        homing = 0;
        spectral = 0;
        controllable = 0;
        extraTrinketSlotCounts = 0;

        bulletFilters = new HashMap<>();
        bulletFilters.put(-1, ColorManager.FILTER_BASE); // -1视为当前颜色

        pillRecords = new HashMap<>();
        itemFlags = new HashMap<>();
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
        this.extraTrinketSlotCounts = source.extraTrinketSlotCounts;

        this.bulletFilters = new HashMap<>(source.bulletFilters);
        this.pillRecords = new HashMap<>(source.pillRecords);
        this.itemFlags = new HashMap<>(source.itemFlags);
    }
    public void saveNBTData(CompoundTag nbt) {
        nbt.putBoolean("holdRightClick", holdRightClick);
        nbt.putInt("piercing", piercing);
        nbt.putInt("homing", homing);
        nbt.putInt("spectral", spectral);
        nbt.putInt("controllable", controllable);
        nbt.putInt("trinket_slot_counts", extraTrinketSlotCounts);

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

        // 物品记录
        ListTag itemFlagList = new ListTag();
        for (Map.Entry<Integer, Boolean> entry : itemFlags.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("item_id", entry.getKey());
            tag.putBoolean("flag", entry.getValue());
            itemFlagList.add(tag);
        }
        nbt.put("item_flags", itemFlagList);
    }
    public void loadNBTData(CompoundTag nbt) {
        this.holdRightClick = nbt.getBoolean("holdRightClick");
        this.spectral = nbt.getInt("spectral");
        this.piercing = nbt.getInt("piercing");
        this.homing = nbt.getInt("homing");
        this.controllable = nbt.getInt("controllable");
        this.extraTrinketSlotCounts = nbt.getInt("trinket_slot_counts");

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

        pillRecords.clear();
        if (nbt.contains("pill_records", Tag.TAG_LIST)) {
            ListTag list = nbt.getList("pill_records", Tag.TAG_COMPOUND);
            for (Tag t : list) {
                CompoundTag tag = (CompoundTag) t;
                int pillId = tag.getInt("pill_id");
                int effectId = tag.getInt("effect_id");
                pillRecords.put(pillId, effectId);
            }
        }

        itemFlags.clear();
        if (nbt.contains("item_flags", Tag.TAG_LIST)) {
            ListTag list = nbt.getList("item_flags", Tag.TAG_COMPOUND);
            for (Tag t : list) {
                CompoundTag tag = (CompoundTag) t;
                int itemId = tag.getInt("item_id");
                boolean flag = tag.getBoolean("flag");
                itemFlags.put(itemId, flag);
            }
        }
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
        bulletFilters.put(-1, ColorManager.FILTER_BASE);
    }
    public Map<Integer, Integer> getFilters() {
        return Collections.unmodifiableMap(bulletFilters);
    }
    public void resetFilter(Player player){
        bulletFilters.put(-1, ColorHelper.blendFilters(ColorManager.FILTER_BASE,
                bulletFilters.keySet().stream().toList())); // 放到-1位置视为当前filter颜色
    }
    public Map<Integer, Integer> getPillRecordsMap() {
        return Collections.unmodifiableMap(pillRecords);
    }
    public void setPillRecord(ServerPlayer player, int pillId, int effectId) {
        bulletFilters.put(pillId, effectId);
        // 同步到客户端
        ModMessages.sentToPlayer(new PillRecordsSyncS2CPacket(pillId, effectId), player);
    }
    public Map<Integer, Boolean> getItemFlags() {
        return Collections.unmodifiableMap(itemFlags);
    }
    public void setItemFlags(ServerPlayer player, int ItemId, boolean flag){
        itemFlags.put(ItemId, flag);
    }
    public int getExtraTrinketSlotCounts(){return extraTrinketSlotCounts;}
    public void setExtraTrinketSlotCounts(int amount) {this.extraTrinketSlotCounts = amount;}
}
