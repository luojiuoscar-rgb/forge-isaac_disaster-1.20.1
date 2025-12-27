package net.luojiuoscar.isaac_disaster.capability.player;

import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.combination.AttackCombinationRule;
import net.luojiuoscar.isaac_disaster.registries.attack_type.combination.ModCombinationRules;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.BulletColor;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

public class PlayerAbility {
    private boolean holdRightClick;

    private int piercing;
    private int homing;
    private int spectral;
    private int controllable;

    private int extraTrinketSlotCounts;
    private int chargeAmount;
    private int preChargeAmount;

    private final Map<Integer, Boolean> itemFlags;

    private final Map<ResourceLocation, Integer> attackType;
    private ResourceLocation bestAttackType;
    private AttackType cachedAttackType;
    private final Map<ResourceLocation, Integer> bulletColor; // bullet color id : count
    private ResourceLocation bestBulletColor;
    private final HashMap<ResourceLocation, Integer> trajectories;

    public PlayerAbility() {
        itemFlags = new HashMap<>();
        attackType = new HashMap<>();
        bulletColor = new HashMap<>();
        trajectories = new HashMap<>();
        init();
    }

    public void init() {
        holdRightClick = false;
        piercing = 0;
        homing = 0;
        spectral = 0;
        controllable = 0;
        extraTrinketSlotCounts = 0;
        chargeAmount = 0;

        bestBulletColor = ModBulletColor.BASE.getId();
        bestAttackType = ModAttackType.BULLET.getId();
        cachedAttackType = ModAttackType.BULLET.get();

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
        this.bestBulletColor = source.bestBulletColor;
        this.bestAttackType = source.bestAttackType;
        this.cachedAttackType = source.cachedAttackType;

        this.itemFlags.clear();
        this.itemFlags.putAll(source.itemFlags);
        this.attackType.clear();
        this.attackType.putAll(source.attackType);
        this.bulletColor.clear();
        this.bulletColor.putAll(source.bulletColor);
        this.trajectories.clear();
        this.trajectories.putAll(source.trajectories);
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putInt("piercing", piercing);
        nbt.putInt("homing", homing);
        nbt.putInt("spectral", spectral);
        nbt.putInt("controllable", controllable);
        nbt.putInt("trinket_slot_counts", extraTrinketSlotCounts);
        nbt.putString("best_bullet_color", bestBulletColor.toString());
        nbt.putString("best_attack_type", bestAttackType.toString());

        ListTag itemFlagList = new ListTag();
        for (Map.Entry<Integer, Boolean> entry : itemFlags.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("item_id", entry.getKey());
            tag.putBoolean("flag", entry.getValue());
            itemFlagList.add(tag);
        }
        nbt.put("item_flags", itemFlagList);

        ListTag bulletTypeList = new ListTag();
        for (Map.Entry<ResourceLocation, Integer> entry : attackType.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putString("bullet_type_id", entry.getKey().toString());
            tag.putInt("count", entry.getValue());
            bulletTypeList.add(tag);
        }
        nbt.put("bullet_types", bulletTypeList);

        ListTag bulletColorList = new ListTag();
        for (Map.Entry<ResourceLocation, Integer> entry : bulletColor.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putString("bullet_color_id", entry.getKey().toString());
            tag.putInt("count", entry.getValue());
            bulletColorList.add(tag);
        }
        nbt.put("bullet_colors", bulletColorList);

        ListTag trajectoriesList = new ListTag();
        for (Map.Entry<ResourceLocation, Integer> entry : trajectories.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putString("trajectory_id", entry.getKey().toString());
            tag.putInt("count", entry.getValue());
            trajectoriesList.add(tag);
        }
        nbt.put("trajectories", trajectoriesList);
    }

    public void loadNBTData(CompoundTag nbt) {
        this.spectral = nbt.getInt("spectral");
        this.piercing = nbt.getInt("piercing");
        this.homing = nbt.getInt("homing");
        this.controllable = nbt.getInt("controllable");
        this.extraTrinketSlotCounts = nbt.getInt("trinket_slot_counts");
        this.bestBulletColor = ResourceLocation.parse(nbt.getString("best_bullet_color"));
        this.bestAttackType = ResourceLocation.parse(nbt.getString("best_attack_type"));

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
                ResourceLocation type = ResourceLocation.parse(tag.getString("bullet_type_id"));
                int count = tag.getInt("count");
                attackType.put(type, count);
            }
        }

        bulletColor.clear();
        if (nbt.contains("bullet_colors", Tag.TAG_LIST)) {
            ListTag list = nbt.getList("bullet_colors", Tag.TAG_COMPOUND);
            for (Tag t : list) {
                CompoundTag tag = (CompoundTag) t;
                String colorStr = tag.getString("bullet_color_id");
                int count = tag.getInt("count");
                try{
                    ResourceLocation rl = ResourceLocation.parse(colorStr);
                    bulletColor.put(rl, count);
                }catch (Exception ignored) {}
            }
        }

        trajectories.clear();
        if (nbt.contains("trajectories", Tag.TAG_LIST)) {
            ListTag list = nbt.getList("trajectories", Tag.TAG_COMPOUND);
            for (Tag t : list) {
                CompoundTag tag = (CompoundTag) t;
                String trajIdStr = tag.getString("trajectory_id");
                int count = tag.getInt("count");
                try {
                    ResourceLocation rl = ResourceLocation.parse(trajIdStr);
                    trajectories.put(rl, count);
                } catch (Exception ignored) {}
            }
        }
    }

    public boolean isHoldingRightClick() {
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
        return new HashMap<>(itemFlags);
    }

    public void setItemFlags(int ItemId, boolean flag) {
        itemFlags.put(ItemId, flag);
    }

    public int getExtraTrinketSlotCounts() {
        return extraTrinketSlotCounts;
    }

    public void setExtraTrinketSlotCounts(int amount) {
        this.extraTrinketSlotCounts = amount;
    }

    public int getChargeAmount() {
        return chargeAmount;
    }

    public void setChargeAmount(int chargeAmount) {
        this.chargeAmount = chargeAmount;
    }

    public int getPreChargeAmount() {
        return preChargeAmount;
    }

    public void setPreChargeAmount(int preChargeAmount) {
        this.preChargeAmount = preChargeAmount;
    }

    public Map<ResourceLocation, Integer> getBulletTypeMap() {
        return new HashMap<>(attackType);
    }

    public AttackType getCachedAttackType() {
        return cachedAttackType;
    }

    public void addAttackType(ResourceLocation id, int count) {
        int r = attackType.getOrDefault(id, 0) + count;
        if (r <= 0) {
            attackType.remove(id);
        }else{
            attackType.put(id, r);
        }
        updateBestAttackType();
    }

    public void updateBestAttackType() {
        IForgeRegistry<AttackType> registry = RegistryManager.ACTIVE.getRegistry(ModAttackType.ATTACK_TYPE_KEY);
        if (registry == null) return;

        // 找到单体最高优先级
        ResourceLocation bestSingleId = ModAttackType.BULLET.getId();
        AttackType bestSingle = registry.getValue(bestSingleId);
        if (bestSingle == null) return;

        double bestSinglePriority = bestSingle.getPriority();

        for (ResourceLocation id : attackType.keySet()) {
            AttackType at = registry.getValue(id);
            if (at == null) continue;

            double priority = at.getPriority();
            if (priority > bestSinglePriority) {
                bestSinglePriority = priority;
                bestSingleId = id;
                bestSingle = at;
            }
        }

        // 尝试匹配组合规则
        AttackCombinationRule bestCombo = null;
        double bestComboPriority = -1;

        IForgeRegistry<AttackCombinationRule> comboRegistry =
                RegistryManager.ACTIVE.getRegistry(ModCombinationRules.ATTACK_COMBINATION_RULE_KEY);

        if (comboRegistry != null){
            for (AttackCombinationRule combo : comboRegistry.getValues()) {

                // 检查组合 triggerSet 是否都在当前攻击类型中
                boolean allInAttackType = combo.triggerSet.stream()
                        .map(RegistryObject::get)
                        .allMatch(trigger -> attackType.containsKey(trigger.getId()));

                if (!allInAttackType) continue;

                // 检查是否有更高优先级的单体攻击，不属于组合内
                boolean blocked = attackType.keySet().stream()
                        .map(registry::getValue)
                        .filter(at -> at != null)
                        .anyMatch(at -> at.getPriority() > combo.priority
                                && combo.triggerSet.stream().map(RegistryObject::get).noneMatch(t -> t == at));

                if (blocked) continue;

                // 找到优先级最大的组合
                double comboPriority = combo.priority;
                if (comboPriority > bestComboPriority) {
                    bestComboPriority = comboPriority;
                    bestCombo = combo;
                }
            }
        }

        // 最终：组合优先级高于单体优先级时才生效
        if (bestCombo != null) {
            this.bestAttackType = bestCombo.result.getId();
            this.cachedAttackType = bestCombo.result.get();
        } else {
            this.bestAttackType = bestSingleId;
            this.cachedAttackType = bestSingle;
        }
    }


    public Map<ResourceLocation, Integer> getAttackTypes() {
        return attackType;
    }

    public ResourceLocation getBestAttackType(){
        return bestAttackType;
    }

    public Map<ResourceLocation, Integer> getBulletColor(){
        return new HashMap<>(bulletColor);
    }

    public void updateBestBulletColor() {
        IForgeRegistry<BulletColor> registry = RegistryManager.ACTIVE.getRegistry(ModBulletColor.BULLET_COLOR_KEY);
        if (registry == null) return;

        double bestPriority = ModBulletColor.BASE.get().priority();
        ResourceLocation bestKey = ModBulletColor.BASE.getId();

        for (ResourceLocation key : this.bulletColor.keySet()) {
            BulletColor color = registry.getValue(key);
            if (color == null) continue;

            double priority = color.priority();
            if (priority > bestPriority) {
                bestPriority = priority;
                bestKey = key;
            }
        }

        this.bestBulletColor = bestKey;
    }

    public ResourceLocation getBestBulletColor() {
        return bestBulletColor;
    }

    public void addBulletColor(ResourceLocation key, int count) {
        int r = bulletColor.getOrDefault(key, 0) + count;

        if (r <= 0) {
            bulletColor.remove(key);
        } else {
            bulletColor.put(key, r);
        }

        updateBestBulletColor();
    }



    public Map<ResourceLocation, Integer> getTrajectories() {
        return new HashMap<>(trajectories);
    }

    public void addTrajectory(ResourceLocation rl, int count){
        int c = trajectories.getOrDefault(rl, 0) + count;
        if (c <= 0) {
            trajectories.remove(rl);
            return;
        }
        trajectories.put(rl, c);
    }
}
