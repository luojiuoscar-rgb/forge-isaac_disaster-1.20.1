package net.luojiuoscar.isaac_disaster.manager;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.manager.data.PillShuffleData;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IExecutableEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.PillEffect;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
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
    private final Map<Integer, RegistryObject<IExecutableEffect>> pillEffectMap = new HashMap<>();

    // --------------- 注册 ----------------

    public void registerNewPillId(int pillId) {
        registeredPillIds.add(pillId);
    }

    /** 获取全部 PillEffect 注册对象 */
    private List<RegistryObject<IExecutableEffect>> getAllPillEffects() {
        IForgeRegistry<IExecutableEffect> reg =
                RegistryManager.ACTIVE.getRegistry(ModExecutableEffects.EXECUTABLE_EFFECT);

        // 筛选出 PillEffect 类型
        var effects = reg.getEntries().stream()
                .map(Map.Entry::getValue)
                .filter(e -> e instanceof PillEffect)
                .map(e -> ModExecutableEffects.EXECUTABLE_EFFECT_REGISTRY.getEntries().stream()
                        .filter(ro -> ro.get() == e)
                        .findFirst()
                        .orElseThrow()) // 必须能找到对应的 RegistryObject
                .toList();
        return new ArrayList<>(effects);
    }

    // --------------- 查询 ----------------

    public RegistryObject<IExecutableEffect> getEffectIdFromPill(int pillId) {
        return pillEffectMap.getOrDefault(pillId, ModExecutableEffects.I_FOUND_PILLS);
    }

    public RegistryObject<IExecutableEffect> getEffectFromPill(int pillId) {
        return getEffectIdFromPill(pillId);
    }

    public boolean isReady() {
        return (!registeredPillIds.isEmpty() && !getAllPillEffects().isEmpty());
    }

    // ---------------- load world ----------------

    public void loadFromWorld(ServerLevel level) {
        PillShuffleData data = PillShuffleData.get(level);
        pillEffectMap.clear();

        data.getPillEffectMap().forEach((pillId, rl) -> {
            RegistryObject<IExecutableEffect> ro = getAllPillEffects().stream()
                    .filter(r -> r.getId().equals(rl))
                    .findFirst()
                    .orElse(ModExecutableEffects.I_FOUND_PILLS);

            pillEffectMap.put(pillId, ro);
        });
    }

    // ---------------- 工具方法 ----------------

    public void shufflePills(ServerLevel level) {
        pillEffectMap.clear();

        if (!isReady()) return;

        List<Integer> pills = new ArrayList<>(registeredPillIds);
        List<RegistryObject<IExecutableEffect>> effects = getAllPillEffects();

        Collections.shuffle(pills);
        Collections.shuffle(effects);

        int size = Math.min(pills.size(), effects.size());
        for (int i = 0; i < size; i++) {
            pillEffectMap.put(pills.get(i), effects.get(i));
        }

        PillShuffleData data = PillShuffleData.get(level);
        data.clear();

        pillEffectMap.forEach((pillId, effectRO) ->
                data.setMapping(pillId, effectRO.getId())
        );

        try {
            level.getDataStorage().save();
        } catch (Exception e) {
            IsaacDisaster.LOGGER.error("Exception when shuffling pills: " + e);
        }
    }

    public void triggerRandomEffect(ServerPlayer player, boolean isHorse) {
        if (pillEffectMap.isEmpty()) return;

        List<RegistryObject<IExecutableEffect>> effects = new ArrayList<>(pillEffectMap.values());
        RegistryObject<IExecutableEffect> ro = effects.get(player.level().random.nextInt(effects.size()));

        IExecutableEffect effect = ro.get();

        if (effect instanceof PillEffect pillEffect) {
            ExecutableEffectContext context = new ExecutableEffectContext(player);
            context.set(ContextKeys.BOOLEAN, List.of(isHorse));
            pillEffect.apply(context);
        }
    }

    // ---------------- nbt ----------------

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

            RegistryObject<IExecutableEffect> ro = getAllPillEffects().stream()
                    .filter(r -> r.getId().equals(rl))
                    .findFirst()
                    .orElse(ModExecutableEffects.I_FOUND_PILLS);

            pillEffectMap.put(pillId, ro);
        }
    }
}