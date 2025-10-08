package net.luojiuoscar.isaac_disaster.capability.player;


import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.helper.ColorHelper;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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



    // constructor
    public PlayerAbility(){
        holdRightClick = false;
        piercing = 0;
        homing = 0;
        spectral = 0;
        controllable = 0;

        bulletFilters = new HashMap<>();
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

        this.bulletFilters.clear();
        this.bulletFilters = new HashMap<>(source.bulletFilters);
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putBoolean("holdRightClick", holdRightClick);
        nbt.putInt("piercing", piercing);
        nbt.putInt("homing", homing);
        nbt.putInt("spectral", spectral);
        nbt.putInt("controllable", controllable);

        // 保存滤镜
        ListTag filterList = new ListTag();
        for (Map.Entry<Integer, Integer> entry : bulletFilters.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("color", entry.getKey());
            tag.putInt("count", entry.getValue());
            filterList.add(tag);
        }
        nbt.put("bullet_filters", filterList);
    }
    public void loadNBTData(CompoundTag nbt) {
        this.holdRightClick = nbt.getBoolean("holdRightClick");
        this.spectral = nbt.getInt("spectral");
        this.piercing = nbt.getInt("piercing");
        this.homing = nbt.getInt("homing");
        this.controllable = nbt.getInt("controllable");

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
    }
}
