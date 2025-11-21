package net.luojiuoscar.isaac_disaster.capability.player;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.FlyUpdateS2CPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@AutoRegisterCapability
public class PlayerStatModifier {

    public static class StatInstance{
        public UUID uuid;
        public Attribute attribute;
        private double value;
        public Double maxVal;
        public Double minVal;
        private int operationType;

        public StatInstance(UUID uuid, double value, @Nullable Double maxVal, @Nullable Double minVal,
                            @Nullable Attribute attribute, int operationType){
            this.uuid = uuid;
            this.value = value;
            this.maxVal = maxVal;
            this.minVal = minVal;
            this.attribute = attribute;
            this.operationType = operationType;
        }

        public double getValue() {
            if (maxVal != null && value > maxVal) return maxVal;
            if (minVal != null && value < minVal) return minVal;
            return value;
        }

        public void add(double val){
            this.value += val;
        }

        public void set(double val){
            this.value = val;
        }

        /**
         * operation type
         * 0 -> add
         * 1 -> multiply_base
         * 2 -> multiply_total
         */
        public int getOperationType(){
            return operationType;
        }

        /**
         * operation type
         * 0 -> add
         * 1 -> multiply_base
         * 2 -> multiply_total
         */
        public void setOperationType(int operationType){
            this.operationType = Math.min(2, Math.max(operationType, 0));
        }
    }

    /** ← 原来是 Set，现在换成 Map 存 StatInstance **/
    private final Map<UUID, StatInstance> playerModifiers;

    private double flyTimeCurrent;

    public PlayerStatModifier(){
        playerModifiers = new HashMap<>();
        init();
    }

    public void init(){
        playerModifiers.clear();

        flyTimeCurrent = 0;
    }

    // -----------------------------
    // Getter
    // -----------------------------
    public StatInstance getStatInstance(UUID uuid){
        return playerModifiers.get(uuid);
    }

    public void removeStatInstance(UUID uuid){
        playerModifiers.remove(uuid);
    }

    public double getFlyTimeCurrent(){
        return flyTimeCurrent;
    }

    // -----------------------------
    // Setter
    // -----------------------------
    /** set or create */
    public void setModifierValue(UUID uuid, double amount, @Nullable Double maxVal, @Nullable Double minVal,
                                 @NotNull Attribute attribute, int operationType){
        StatInstance inst = playerModifiers.get(uuid);
        if (inst != null){
            inst.set(amount);
            inst.attribute = attribute;
        }else{
            playerModifiers.put(uuid, new StatInstance(uuid, amount, maxVal, minVal, attribute, operationType));
        }
    }

    /** add or create */
    public void addModifierValue(UUID uuid, double amount, @Nullable Double maxVal, @Nullable Double minVal,
                                 @NotNull Attribute attribute, int operationType){
        StatInstance inst = playerModifiers.get(uuid);
        if (inst != null){
            inst.add(amount);
            inst.attribute = attribute;
        }else{
            playerModifiers.put(uuid, new StatInstance(uuid, amount, maxVal, minVal, attribute, operationType));
        }
    }

    public void removeModifier(UUID uuid){
        playerModifiers.remove(uuid);
    }

    public void addCurrentFlyTime(ServerPlayer player, int amount){
        double flyTime = PlayerHelper.getFly(player);

        double pre = flyTimeCurrent % (flyTime / 20);

        flyTimeCurrent = Math.max(0, flyTimeCurrent + amount);

        if (flyTimeCurrent >= flyTime && player.getEffect(ModEffects.TRANSCENDENCE.get()) == null){
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }

        if (flyTimeCurrent < 0 || flyTimeCurrent > flyTime) return;

        double curr = pre + amount;
        if (curr >= (flyTime / 20) || curr < 0){
            int units = 20 - (int) Math.max((flyTimeCurrent / flyTime * 20), 0);
            ModMessages.sentToPlayer(new FlyUpdateS2CPacket(units), player);
        }
    }

    // 复制属性 attribute同步
    private void refreshAllModifiers(Player player, Map<UUID, StatInstance> source){
        for (StatInstance inst : source.values()) {
            StatManager.setModifier(player, inst.uuid, inst.attribute, inst.value,
                    inst.minVal, inst.maxVal, inst.operationType);
        }
    }

    public void copyFrom(PlayerStatModifier source, Player player) {
        this.playerModifiers.clear();
        refreshAllModifiers(player, source.playerModifiers);
    }

    public void saveNBTData(CompoundTag nbt) {

        ListTag listTag = new ListTag();
        for (StatInstance inst : playerModifiers.values()) {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("uuid", inst.uuid);
            tag.putDouble("value", inst.value);
            tag.putInt("operation_type", inst.operationType);

            if (inst.maxVal != null) tag.putDouble("max", inst.maxVal);
            if (inst.minVal != null) tag.putDouble("min", inst.minVal);

            if (inst.attribute != null) {
                ResourceLocation rl = ForgeRegistries.ATTRIBUTES.getKey(inst.attribute);
                if (rl != null) tag.putString("attribute", rl.toString());
            }

            listTag.add(tag);
        }
        nbt.put("player_modifiers", listTag);
    }

    public void loadNBTData(CompoundTag nbt) {

        this.flyTimeCurrent = 0;

        this.playerModifiers.clear();
        if (nbt.contains("player_modifiers", Tag.TAG_LIST)) {

            ListTag listTag = nbt.getList("player_modifiers", Tag.TAG_COMPOUND);

            for (int i = 0; i < listTag.size(); i++){
                CompoundTag tag = listTag.getCompound(i);

                UUID uuid = tag.getUUID("uuid");
                double value = tag.getDouble("value");
                int operationType = tag.getInt("operation_type");

                Double max = tag.contains("max") ? tag.getDouble("max") : null;
                Double min = tag.contains("min") ? tag.getDouble("min") : null;

                Attribute attribute = null;
                if (tag.contains("attribute")) {
                    String attrStr = tag.getString("attribute");
                    attribute = ForgeRegistries.ATTRIBUTES.getValue(ResourceLocation.parse(attrStr));
                }

                StatInstance inst = new StatInstance(uuid, value, max, min, attribute, operationType);
                playerModifiers.put(uuid, inst);
            }
        }
    }
}
