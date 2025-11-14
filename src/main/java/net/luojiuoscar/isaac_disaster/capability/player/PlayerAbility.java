package net.luojiuoscar.isaac_disaster.capability.player;

import net.luojiuoscar.isaac_disaster.manager.attack.managers.AttackType;
import net.luojiuoscar.isaac_disaster.manager.attack.managers.BulletColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class PlayerAbility {
    private boolean holdRightClick;

    private int piercing;
    private int homing;
    private int spectral;
    private int controllable;

    private int extraTrinketSlotCounts;

    private Map<Integer, Boolean> itemFlags;
    private Map<Integer, Integer> attackType;
    private int bestBulletType;
    private Map<Integer, Integer> bulletColor; // bullet color id : count
    private int bestBulletColor;
    private LinkedHashMap<Integer, Integer> trajectories;

    public PlayerAbility() {
        itemFlags = new HashMap<>();
        attackType = new HashMap<>();
        bulletColor = new HashMap<>();
        trajectories = new LinkedHashMap<>();
        init();
    }

    public void init() {
        holdRightClick = false;
        piercing = 0;
        homing = 0;
        spectral = 0;
        controllable = 0;
        extraTrinketSlotCounts = 0;

        itemFlags.clear();
        attackType.clear();
        bulletColor.clear();
        trajectories.clear();
    }

    public void copyFrom(PlayerAbility source) {
        this.holdRightClick = source.holdRightClick;
        this.piercing = source.piercing;
        this.homing = source.homing;
        this.spectral = source.spectral;
        this.controllable = source.controllable;
        this.extraTrinketSlotCounts = source.extraTrinketSlotCounts;

        this.itemFlags = new HashMap<>(source.itemFlags);
        this.attackType = new HashMap<>(source.attackType);
        this.bulletColor = new HashMap<>(source.bulletColor);
        this.trajectories = new LinkedHashMap<>(source.trajectories);
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putBoolean("holdRightClick", holdRightClick);
        nbt.putInt("piercing", piercing);
        nbt.putInt("homing", homing);
        nbt.putInt("spectral", spectral);
        nbt.putInt("controllable", controllable);
        nbt.putInt("trinket_slot_counts", extraTrinketSlotCounts);

        ListTag itemFlagList = new ListTag();
        for (Map.Entry<Integer, Boolean> entry : itemFlags.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("item_id", entry.getKey());
            tag.putBoolean("flag", entry.getValue());
            itemFlagList.add(tag);
        }
        nbt.put("item_flags", itemFlagList);

        ListTag bulletTypeList = new ListTag();
        for (Map.Entry<Integer, Integer> entry : attackType.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("bullet_type_id", entry.getKey());
            tag.putInt("count", entry.getValue());
            bulletTypeList.add(tag);
        }
        nbt.put("bullet_types", bulletTypeList);

        ListTag bulletColorList = new ListTag();
        for (Map.Entry<Integer, Integer> entry : bulletColor.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("bullet_color_id", entry.getKey());
            tag.putInt("count", entry.getValue());
            bulletColorList.add(tag);
        }
        nbt.put("bullet_colors", bulletColorList);

        ListTag trajectoriesList = new ListTag();
        for (Map.Entry<Integer, Integer> entry : trajectories.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("trajectory_id", entry.getKey());
            tag.putInt("count", entry.getValue());
            trajectoriesList.add(tag);
        }
        nbt.put("trajectories", trajectoriesList);
    }

    public void loadNBTData(CompoundTag nbt) {
        this.holdRightClick = nbt.getBoolean("holdRightClick");
        this.spectral = nbt.getInt("spectral");
        this.piercing = nbt.getInt("piercing");
        this.homing = nbt.getInt("homing");
        this.controllable = nbt.getInt("controllable");
        this.extraTrinketSlotCounts = nbt.getInt("trinket_slot_counts");

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

        attackType.clear();
        if (nbt.contains("bullet_types", Tag.TAG_LIST)) {
            ListTag list = nbt.getList("bullet_types", Tag.TAG_COMPOUND);
            for (Tag t : list) {
                CompoundTag tag = (CompoundTag) t;
                int color = tag.getInt("bullet_type_id");
                int count = tag.getInt("count");
                attackType.put(color, count);
            }
        }

        bulletColor.clear();
        if (nbt.contains("bullet_colors", Tag.TAG_LIST)) {
            ListTag list = nbt.getList("bullet_colors", Tag.TAG_COMPOUND);
            for (Tag t : list) {
                CompoundTag tag = (CompoundTag) t;
                int color = tag.getInt("bullet_color_id");
                int count = tag.getInt("count");
                bulletColor.put(color, count);
            }
        }

        trajectories.clear();
        if (nbt.contains("trajectories", Tag.TAG_LIST)) {
            ListTag list = nbt.getList("trajectories", Tag.TAG_COMPOUND);
            for (Tag t : list) {
                CompoundTag tag = (CompoundTag) t;
                int color = tag.getInt("trajectory_id");
                int count = tag.getInt("count");
                trajectories.put(color, count);
            }
        }
    }

    public boolean isHoldRightClick() {
        return holdRightClick;
    }

    public void setHoldRightClick(boolean holdRightClick) {
        this.holdRightClick = holdRightClick;
    }

    public int getPiercing() {
        return piercing;
    }

    public void setPiercing(int amount) {
        piercing = amount;
    }

    public int getHoming() {
        return homing;
    }

    public void setHoming(int amount) {
        homing = amount;
    }

    public int getSpectral() {
        return spectral;
    }

    public void setSpectral(int amount) {
        spectral = amount;
    }

    public int getControllable() {
        return controllable;
    }

    public void setControllable(int amount) {
        controllable = amount;
    }

    public Map<Integer, Boolean> getItemFlags() {
        return Collections.unmodifiableMap(itemFlags);
    }

    public void setItemFlags(ServerPlayer player, int ItemId, boolean flag) {
        itemFlags.put(ItemId, flag);
    }

    public int getExtraTrinketSlotCounts() {
        return extraTrinketSlotCounts;
    }

    public void setExtraTrinketSlotCounts(int amount) {
        this.extraTrinketSlotCounts = amount;
    }

    public Map<Integer, Integer> getBulletTypeMap() {
        return new HashMap<>(attackType);
    }

    public void addAttackType(int id, int count) {
        int r = attackType.getOrDefault(id, 0) + count;
        if (r <= 0) {
            attackType.remove(id);
            return;
        }
        attackType.put(id, r);
        updateBestAttackType();
    }

    public void updateBestAttackType(){
        int bestId = AttackType.BULLET.getId();
        double bestPriority = AttackType.getPriorityById(bestId);

        for (int id : attackType.keySet()) {
            double priority = AttackType.getPriorityById(id);
            if (priority > bestPriority) {
                bestPriority = priority;
                bestId = id;
            }
        }
        this.bestBulletType = bestId;
    }

    public int getBestBulletType(){
        return bestBulletType;
    }

    public Map<Integer, Integer> getBulletColor(){
        return new HashMap<>(bulletColor);
    }

    public void updateBestBulletColor(){
        // 每一段时间更新
        int bestId = BulletColor.BASE.getId();
        double bestPriority = BulletColor.getPriorityById(bestId);

        for (int id : bulletColor.keySet()) {
            double priority = BulletColor.getPriorityById(id);
            if (priority > bestPriority) {
                bestPriority = priority;
                bestId = id;
            }
        }
        this.bestBulletColor = bestId;
    }

    public int getBestBulletColorId(){
        return bestBulletColor;
    }

    public void addBulletColor(int id, int count){
        int r = bulletColor.getOrDefault(id, 0) + count;
        if (r <= 0) {
            bulletColor.remove(id);
            return;
        }
        bulletColor.put(id, r);
        updateBestBulletColor();
    }

    public Map<Integer, Integer> getTrajectories() {
        return new LinkedHashMap<>(trajectories);
    }

    public void addTrajectory(int id, int count){
        int c = trajectories.getOrDefault(id, 0) + count;
        if (c <= 0) {
            trajectories.remove(id);
            return;
        }
        trajectories.put(id, c);
    }
}
