package net.luojiuoscar.isaac_disaster.capability.player;

import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackSelection;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackSelectionContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackSelector;
import net.luojiuoscar.isaac_disaster.registries.attack_type.tags.IChargeableAttack;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.BulletColor;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.networking.ChargeBarSync;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

import java.util.*;

public class PlayerAbility {
    private boolean holdRightClick;

    private int piercing;
    private int homing;
    private int spectral;
    private int controllable;

    private int extraTrinketSlotCounts;
    private final Map<ResourceLocation, Integer> chargeAmounts = new HashMap<>();
    private final Map<ResourceLocation, ChargeBarSync.State> chargeBarStates = new HashMap<>();

    private final Map<ResourceLocation, Integer> attackType;
    private AttackSelection attackSelection;
    private final LinkedHashMap<ResourceLocation, Integer> bulletColor; // priority-ordered color id : count
    private ResourceLocation bestBulletColor;
    private final Map<ResourceLocation, Integer> bulletVisuals;

    public PlayerAbility() {
        attackType = new HashMap<>();
        bulletColor = new LinkedHashMap<>();
        bulletVisuals = new HashMap<>();
        init();
    }

    public void init() {
        holdRightClick = false;
        piercing = 0;
        homing = 0;
        spectral = 0;
        controllable = 0;
        extraTrinketSlotCounts = 0;
        clearChargeStates();

        bestBulletColor = ModBulletColors.BASE.getId();

        attackType.clear();
        bulletColor.clear();
        bulletVisuals.clear();
        attackSelection = AttackSelector.select(attackType);
    }

    public void copyFrom(PlayerAbility source, ServerPlayer player) {
        this.piercing = source.piercing;
        this.homing = source.homing;
        this.spectral = source.spectral;
        this.controllable = source.controllable;
        this.extraTrinketSlotCounts = source.extraTrinketSlotCounts;
        this.bestBulletColor = source.bestBulletColor;

        this.attackType.clear();
        this.attackType.putAll(source.attackType);
        this.bulletColor.clear();
        this.bulletColor.putAll(source.bulletColor);
        this.bulletVisuals.clear();
        this.bulletVisuals.putAll(source.bulletVisuals);
        clearChargeStates();
        attackSelection = AttackSelector.select(new AttackSelectionContext(attackType, player));
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putInt("piercing", piercing);
        nbt.putInt("homing", homing);
        nbt.putInt("spectral", spectral);
        nbt.putInt("controllable", controllable);
        nbt.putInt("trinket_slot_counts", extraTrinketSlotCounts);
        nbt.putString("best_bullet_color", bestBulletColor.toString());

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

        ListTag visualList = new ListTag();
        for (var entry : bulletVisuals.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putString("visual_id", entry.getKey().toString());
            tag.putInt("count", entry.getValue());
            visualList.add(tag);
        }
        nbt.put("bullet_visuals", visualList);
    }

    public void loadNBTData(CompoundTag nbt) {
        this.spectral = nbt.getInt("spectral");
        this.piercing = nbt.getInt("piercing");
        this.homing = nbt.getInt("homing");
        this.controllable = nbt.getInt("controllable");
        this.extraTrinketSlotCounts = nbt.getInt("trinket_slot_counts");
        this.bestBulletColor = ResourceLocation.parse(nbt.getString("best_bullet_color"));

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
                    if (count > 0) bulletColor.put(rl, count);
                }catch (Exception ignored) {}
            }
        }

        sortBulletColors();
        bulletVisuals.clear();
        if (nbt.contains("bullet_visuals", Tag.TAG_LIST)) {
            for (Tag t : nbt.getList("bullet_visuals", Tag.TAG_COMPOUND)) {
                CompoundTag tag = (CompoundTag) t;
                try {
                    ResourceLocation visualId = ResourceLocation.parse(tag.getString("visual_id"));
                    int count = tag.getInt("count");
                    if (count > 0) bulletVisuals.put(visualId, count);
                } catch (Exception ignored) { }
            }
        }

        clearChargeStates();
        attackSelection = AttackSelector.select(attackType);
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

    public int getExtraTrinketSlotCounts() {
        return extraTrinketSlotCounts;
    }

    public void setExtraTrinketSlotCounts(int amount) {
        this.extraTrinketSlotCounts = amount;
    }

    public int getChargeAmount(ResourceLocation id) {
        return chargeAmounts.getOrDefault(id, 0);
    }

    public void setChargeAmount(ResourceLocation id, int amount) {
        chargeAmounts.put(Objects.requireNonNull(id, "charge bar ID"), amount);
    }

    public boolean hasChargeAmount(ResourceLocation id) {
        return chargeAmounts.containsKey(id);
    }

    public void clearChargeAmount(ResourceLocation id) {
        chargeAmounts.remove(id);
    }

    public ChargeBarSync.State getChargeBarState(ResourceLocation id) {
        return chargeBarStates.get(id);
    }

    public void setChargeBarState(ResourceLocation id, ChargeBarSync.State state) {
        if (state == null) chargeBarStates.remove(id);
        else chargeBarStates.put(id, state);
    }

    public Set<ResourceLocation> getVisibleChargeBarIds() {
        return Set.copyOf(chargeBarStates.keySet());
    }

    /** Runtime charge is shared by bar ID and is deliberately neither saved nor copied. */
    public void clearChargeStates() {
        chargeAmounts.clear();
        chargeBarStates.clear();
        holdRightClick = false;
    }

    public AttackSelection getAttackSelection() {
        return attackSelection;
    }

    public void tickAttacks(ServerPlayer player) {
        AttackSelection selection = attackSelection;
        AttackType main = selection.mainAttack();
        if (main instanceof IChargeableAttack || PlayerHelper.isHoldingIsaacHead(player)) {
            main.tickAttack(player);
        }
        for (AttackType additional : selection.additionalAttacks()) additional.tickAttack(player);
    }

    public void handleAttackInput(ServerPlayer player, boolean pressed) {
        AttackSelection selection = attackSelection;
        selection.mainAttack().handleChargeInput(player, pressed);
        for (AttackType additional : selection.additionalAttacks()) {
            additional.handleChargeInput(player, pressed);
        }
    }

    public void addAttackType(ResourceLocation id, int count, ServerPlayer player) {
        int r = attackType.getOrDefault(id, 0) + count;
        if (r <= 0) {
            attackType.remove(id);
        }else{
            attackType.put(id, r);
        }
        updateAttackSelection(player);
    }

    public void updateAttackSelection(ServerPlayer player) {
        replaceAttackSelection(AttackSelector.select(new AttackSelectionContext(attackType, player)), player);
    }

    private void replaceAttackSelection(AttackSelection selection, ServerPlayer player) {
        AttackSelection previous = attackSelection;
        if (previous.mainAttack() != selection.mainAttack()
                && previous.mainAttack() instanceof IChargeableAttack charge) {
            charge.clearCharge(player);
        }
        for (AttackType oldAdditional : previous.additionalAttacks()) {
            if (!selection.additionalAttacks().contains(oldAdditional)
                    && oldAdditional instanceof IChargeableAttack charge) {
                charge.clearCharge(player);
            }
        }
        attackSelection = selection;
    }

    public Map<ResourceLocation, Integer> getAttackTypes() {
        return Map.copyOf(attackType);
    }


    public Map<ResourceLocation, Integer> getBulletColor(){
        return new LinkedHashMap<>(bulletColor);
    }

    public void updateBestBulletColor() {
        sortBulletColors();
        this.bestBulletColor = bulletColor.isEmpty() ? ModBulletColors.BASE.getId() : bulletColor.keySet().iterator().next();
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

    public void addBulletVisual(ResourceLocation id, int count) {
        if (count == 0) return;
        int next = bulletVisuals.getOrDefault(id, 0) + count;
        if (next <= 0) bulletVisuals.remove(id);
        else bulletVisuals.put(id, next);
    }

    public Set<ResourceLocation> getBulletVisuals() {
        return Set.copyOf(bulletVisuals.keySet());
    }

    private void sortBulletColors() {
        IForgeRegistry<BulletColor> registry = RegistryManager.ACTIVE.getRegistry(ModBulletColors.BULLET_COLOR_KEY);
        if (registry == null) return;
        LinkedHashMap<ResourceLocation, Integer> sorted = new LinkedHashMap<>();
        bulletColor.entrySet().stream()
                .filter(entry -> entry.getValue() > 0 && registry.getValue(entry.getKey()) != null)
                .sorted(Map.Entry.<ResourceLocation, Integer>comparingByKey((a, b) -> {
                    int priority = Double.compare(registry.getValue(b).priority(), registry.getValue(a).priority());
                    return priority != 0 ? priority : a.toString().compareTo(b.toString());
                }))
                .forEach(entry -> sorted.put(entry.getKey(), entry.getValue()));
        bulletColor.clear();
        bulletColor.putAll(sorted);
    }
}
