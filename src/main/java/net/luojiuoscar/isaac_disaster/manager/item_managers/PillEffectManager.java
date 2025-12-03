package net.luojiuoscar.isaac_disaster.manager.item_managers;

import net.luojiuoscar.isaac_disaster.manager.data.PillShuffleData;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.IPillEffect;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.ModPillEffect;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;

public class PillEffectManager {

    private static final PillEffectManager INSTANCE = new PillEffectManager();
    private PillEffectManager() {}
    public static PillEffectManager getInstance() {
        return INSTANCE;
    }

    /** 所有注册过的胶囊 ID（int） */
    private final Set<Integer> registeredPillIds = new HashSet<>();

    /** 当前药丸ID → 效果RegistryObject 的映射 */
    private final Map<Integer, RegistryObject<IPillEffect>> pillEffectMap = new HashMap<>();

    // ---------------
    // 注册
    // ---------------

    public void registerNewPillId(int pillId) {
        registeredPillIds.add(pillId);
    }

    /** 获取全部 PillEffect 注册对象 */
    private List<RegistryObject<IPillEffect>> getAllPillEffects() {
        return new ArrayList<>(ModPillEffect.PILL_EFFECT_REGISTRY.getEntries());
    }

    // ---------------
    // 查询
    // ---------------
    /** 获取某胶囊的效果 RegistryObject */
    public RegistryObject<IPillEffect> getEffectIdFromPill(int pillId) {
        return pillEffectMap.getOrDefault(pillId, ModPillEffect.I_FOUND_PILLS);
    }

    /** 获取某胶囊的真实效果实例 */
    public RegistryObject<IPillEffect> getEffectFromPill(int pillId) {
        return getEffectIdFromPill(pillId);
    }

    public boolean isReady() {
        return (!registeredPillIds.isEmpty() && !ModPillEffect.PILL_EFFECT_REGISTRY.getEntries().isEmpty());
    }


    // ----------------
    // load
    // ----------------

    /** load world saved data → restore pillEffectMap */
    public void loadFromWorld(ServerLevel level) {
        PillShuffleData data = PillShuffleData.get(level);
        pillEffectMap.clear();

        // SavedData 中用 ResourceLocation 字符串保存
        data.getPillEffectMap().forEach((pillId, rl) -> {
            RegistryObject<IPillEffect> ro = ModPillEffect.PILL_EFFECT_REGISTRY.getEntries().stream()
                    .filter(e -> e.getId().equals(rl))
                    .findFirst()
                    .orElse(ModPillEffect.I_FOUND_PILLS);

            pillEffectMap.put(pillId, ro);
        });
    }


    // ----------------
    // 工具方法
    // ----------------

    /** 随机生成新的胶囊 → 效果映射 */
    public void shufflePills(ServerLevel level) {
        pillEffectMap.clear();

        if (!isReady()) return;

        List<Integer> pills = new ArrayList<>(registeredPillIds);
        List<RegistryObject<IPillEffect>> effects = getAllPillEffects();

        Collections.shuffle(pills);
        Collections.shuffle(effects);

        int size = Math.min(pills.size(), effects.size());
        for (int i = 0; i < size; i++) {
            pillEffectMap.put(pills.get(i), effects.get(i));
        }

        // 保存到 savedData
        PillShuffleData data = PillShuffleData.get(level);
        data.clear();

        pillEffectMap.forEach((pillId, effectRO) ->
                data.setMapping(pillId, effectRO.getId())
        );

        try {
            level.getDataStorage().save();
        } catch (Exception ignored) {}
    }

    public void triggerRandomEffect(ServerPlayer player, boolean isHorse) {
        if (pillEffectMap.isEmpty()) return;

        List<RegistryObject<IPillEffect>> effects = new ArrayList<>(pillEffectMap.values());
        RegistryObject<IPillEffect> ro = effects.get(player.level().random.nextInt(effects.size()));

        IPillEffect effect = ro.get();

        // trigger
        effect.redirectAndUse(player, isHorse);
        effect.redirectAndMakeSound(player, isHorse);
    }

    // ----------------
    // nbt
    // ----------------

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();

        pillEffectMap.forEach((pillId, effectRO) ->
                tag.putString(String.valueOf(pillId), effectRO.getId().toString())
        );

        return tag;
    }

    public void loadFromTag(CompoundTag tag) {
        pillEffectMap.clear();

        for (String key : tag.getAllKeys()) {
            int pillId = Integer.parseInt(key);
            ResourceLocation rl = ResourceLocation.parse(tag.getString(key));

            RegistryObject<IPillEffect> ro = ModPillEffect.PILL_EFFECT_REGISTRY.getEntries().stream()
                    .filter(e -> e.getId().equals(rl))
                    .findFirst()
                    .orElse(ModPillEffect.I_FOUND_PILLS);

            pillEffectMap.put(pillId, ro);
        }
    }
}
