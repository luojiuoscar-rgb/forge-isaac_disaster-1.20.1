package net.luojiuoscar.isaac_disaster.capability.player;

import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.PillRecordsSyncS2CPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class PlayerItemUseRecord {

    public record PillData(int id, boolean isHorse, long sequence) {}
    public record CardData(int id, long sequence) {}
    public record ActiveData(int id, long sequence) {}
    public record PillEffectData(ResourceLocation id, boolean isHorse, long sequence) {}

    private final int MAX_RECORDS = 3;

    private final List<PillData> pillRecords;
    private final List<CardData> cardRecords;
    private final List<ActiveData> activeRecords;
    private final List<PillEffectData> pillEffectRecords;

    private final Map<Integer, ResourceLocation> pillEffectMap;

    private long sequenceCounter = 0;

    public PlayerItemUseRecord() {
        pillRecords = new ArrayList<>(MAX_RECORDS);
        cardRecords = new ArrayList<>(MAX_RECORDS);
        activeRecords = new ArrayList<>(MAX_RECORDS);
        pillEffectRecords = new ArrayList<>(MAX_RECORDS);
        pillEffectMap = new HashMap<>();
        init();
    }

    public void init() {
        pillRecords.clear();
        cardRecords.clear();
        activeRecords.clear();
        pillEffectRecords.clear();
        pillEffectMap.clear();
        sequenceCounter = 0;
    }

    // 添加药丸记录
    public void addPillRecord(int id, boolean isHorse) {
        if (pillRecords.size() >= MAX_RECORDS) pillRecords.remove(0);
        pillRecords.add(new PillData(id, isHorse, sequenceCounter++));
    }

    // 添加卡牌记录
    public void addCardRecord(int id) {
        if (cardRecords.size() >= MAX_RECORDS) cardRecords.remove(0);
        cardRecords.add(new CardData(id, sequenceCounter++));
    }

    // 添加主动道具记录
    public void addActiveRecord(int id) {
        if (activeRecords.size() >= MAX_RECORDS) activeRecords.remove(0);
        activeRecords.add(new ActiveData(id, sequenceCounter++));
    }

    // 添加效果记录
    public void addPillEffectRecord(ResourceLocation id, boolean isHorse) {
        if (pillEffectRecords.size() >= MAX_RECORDS) pillEffectRecords.remove(0);
        pillEffectRecords.add(new PillEffectData(id, isHorse, sequenceCounter++));
    }

    public List<PillData> getPillRecords() {
        return new ArrayList<>(pillRecords);
    }

    public List<CardData> getCardRecords() {
        return new ArrayList<>(cardRecords);
    }

    public List<ActiveData> getActiveRecords() {
        return new ArrayList<>(activeRecords);
    }

    public List<PillEffectData> getPillEffectRecords() {
        return new ArrayList<>(pillEffectRecords);
    }

    public Map<Integer, ResourceLocation> getPillEffectMap() {
        return Collections.unmodifiableMap(pillEffectMap);
    }

    public void setPillEffectRecord(ServerPlayer pl, int pillId, ResourceLocation effectRl) {
        pillEffectMap.put(pillId, effectRl);
        ModMessages.sentToPlayer(new PillRecordsSyncS2CPacket(pillId, effectRl), pl);
    }

    public void removePillEffectRecord(int pillId) {
        pillEffectMap.remove(pillId);
    }

    /** NBT 保存 */
    public void saveNBTData(CompoundTag nbt) {
        // PillRecords
        ListTag pillList = new ListTag();
        for (PillData pill : pillRecords) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("id", pill.id);
            tag.putBoolean("isHorse", pill.isHorse);
            tag.putLong("sequence", pill.sequence);
            pillList.add(tag);
        }
        nbt.put("PillRecords", pillList);

        // CardRecords
        ListTag cardList = new ListTag();
        for (CardData card : cardRecords) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("id", card.id);
            tag.putLong("sequence", card.sequence);
            cardList.add(tag);
        }
        nbt.put("CardRecords", cardList);

        // ActiveRecords
        ListTag activeList = new ListTag();
        for (ActiveData active : activeRecords) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("id", active.id);
            tag.putLong("sequence", active.sequence);
            activeList.add(tag);
        }
        nbt.put("ActiveRecords", activeList);

        // EffectRecords
        if (nbt.contains("EffectRecords", Tag.TAG_LIST)) {
            ListTag list = nbt.getList("EffectRecords", Tag.TAG_COMPOUND);
            for (Tag t : list) {
                CompoundTag tag = (CompoundTag) t;
                pillEffectRecords.add(
                        new PillEffectData(
                                ResourceLocation.parse(tag.getString("id")),
                                tag.getBoolean("isHorse"),
                                tag.getLong("sequence")
                        )
                );
            }
        }

        // PillEffectMap
        ListTag pillEffectMapList = new ListTag();
        for (Map.Entry<Integer, ResourceLocation> entry : pillEffectMap.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("pill_id", entry.getKey());
            tag.putString("effect_id", entry.getValue().toString());
            pillEffectMapList.add(tag);
        }
        nbt.put("PillEffectMap", pillEffectMapList);

        nbt.putLong("SequenceCounter", sequenceCounter);
    }

    /** NBT 读取 */
    public void loadNBTData(CompoundTag nbt) {
        pillRecords.clear();
        cardRecords.clear();
        activeRecords.clear();
        pillEffectRecords.clear();
        pillEffectMap.clear();

        // PillRecords
        if (nbt.contains("PillRecords", Tag.TAG_LIST)) {
            ListTag list = nbt.getList("PillRecords", Tag.TAG_COMPOUND);
            for (Tag t : list) {
                CompoundTag tag = (CompoundTag) t;
                pillRecords.add(new PillData(tag.getInt("id"), tag.getBoolean("isHorse"), tag.getLong("sequence")));
            }
        }

        // CardRecords
        if (nbt.contains("CardRecords", Tag.TAG_LIST)) {
            ListTag list = nbt.getList("CardRecords", Tag.TAG_COMPOUND);
            for (Tag t : list) {
                CompoundTag tag = (CompoundTag) t;
                cardRecords.add(new CardData(tag.getInt("id"), tag.getLong("sequence")));
            }
        }

        // ActiveRecords
        if (nbt.contains("ActiveRecords", Tag.TAG_LIST)) {
            ListTag list = nbt.getList("ActiveRecords", Tag.TAG_COMPOUND);
            for (Tag t : list) {
                CompoundTag tag = (CompoundTag) t;
                activeRecords.add(new ActiveData(tag.getInt("id"), tag.getLong("sequence")));
            }
        }

        // EffectRecords
        ListTag effectListTag = new ListTag();
        for (PillEffectData effect : pillEffectRecords) {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", effect.id().toString());
            tag.putBoolean("isHorse", effect.isHorse());
            tag.putLong("sequence", effect.sequence());
            effectListTag.add(tag);
        }
        nbt.put("EffectRecords", effectListTag);

        // PillEffectMap
        if (nbt.contains("PillEffectMap", Tag.TAG_LIST)) {
            ListTag list = nbt.getList("PillEffectMap", Tag.TAG_COMPOUND);
            for (Tag t : list) {
                CompoundTag tag = (CompoundTag) t;
                pillEffectMap.put(
                        tag.getInt("pill_id"),
                        ResourceLocation.parse(tag.getString("effect_id"))
                );
            }
        }

        if (nbt.contains("SequenceCounter")) {
            sequenceCounter = nbt.getLong("SequenceCounter");
        }
    }

    /** 复制 */
    public void copyFrom(PlayerItemUseRecord source) {
        pillRecords.clear();
        cardRecords.clear();
        activeRecords.clear();
        pillEffectRecords.clear();

        pillRecords.addAll(source.pillRecords);
        cardRecords.addAll(source.cardRecords);
        activeRecords.addAll(source.activeRecords);
        pillEffectRecords.addAll(source.pillEffectRecords);

        pillEffectMap.putAll(source.pillEffectMap);
        sequenceCounter = source.sequenceCounter;
    }
}
