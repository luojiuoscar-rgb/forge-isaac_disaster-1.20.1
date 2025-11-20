package net.luojiuoscar.isaac_disaster.manager.item_managers;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPillEffect;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.pill_effects.*;
import net.luojiuoscar.isaac_disaster.manager.data.PillShuffleData;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.PillEffectId;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class PillEffectManager {
    private static final PillEffectManager INSTANCE = new PillEffectManager();
    private PillEffectManager() {}
    public static PillEffectManager getInstance() {
        return INSTANCE;
    }

    // 已注册的所有药丸效果
    private final Map<Integer, IPillEffect> registeredEffects = new HashMap<>();
    // 注册过的药丸ID
    private final Set<Integer> registeredPillIds = new HashSet<>();
    // 当前生效中的药丸ID → 效果ID 映射表
    private final Map<Integer, Integer> PillEffectMap = new HashMap<>();

    //================ 注册部分 =================//
    public void registerEffect(IPillEffect item) {
        int pillEffectId = item.getPillEffectId();
        if (registeredEffects.containsKey(pillEffectId)) {
            throw new IllegalArgumentException("重复的PillEffect ID: " + pillEffectId);
        }
        registeredEffects.put(pillEffectId, item);
    }

    public void registerEffects(IPillEffect... items) {
        for (IPillEffect item : items) {
            registerEffect(item);
        }
    }

    public void registerNewPillId(int pillId) {
        registeredPillIds.add(pillId);
    }

    //================ 查询部分 =================//
    public IPillEffect getEffectFromPill(int pillId) {
        int effectId = getEffectIdFromPill(pillId);
        return registeredEffects.get(effectId);
    }

    public int getEffectIdFromPill(int pillId) {
        return PillEffectMap.getOrDefault(pillId, PillEffectId.I_FOUND_PILLS.getId());  // 默认 IFoundPills
    }

    public IPillEffect getEffectFromEffectId(int effectId) {
        return registeredEffects.get(effectId);
    }

    public Map<Integer, Integer> getPillEffectMap() {
        return Map.copyOf(this.PillEffectMap);
    }

    public int getPillIdFromEffectId(int effectId) {
        for (Map.Entry<Integer, Integer> entry : PillEffectMap.entrySet()) {
            if (entry.getValue() == effectId) {
                return entry.getKey();
            }
        }
        return 0; // 未找到时返回 0
    }

    public boolean isReady() {
        return !registeredEffects.isEmpty() && !registeredPillIds.isEmpty();
    }

    //================ 世界交互部分 =================//
    /**
     * 在服务器启动或世界加载时调用
     * 从已保存的PillShuffleData恢复映射
     */
    public void loadFromWorld(ServerLevel level) {
        PillShuffleData data = PillShuffleData.get(level);
        PillEffectMap.clear();
        PillEffectMap.putAll(data.getPillEffectMap());
    }

    /**
     * 重新随机药丸效果并自动保存到世界数据
     */
    public void shufflePills(ServerLevel level) {
        PillEffectMap.clear();

        if (!isReady()) {
            return;
        }

        List<Integer> pills = new ArrayList<>(registeredPillIds);
        List<Integer> effects = new ArrayList<>(registeredEffects.keySet());

        // 打乱顺序
        Collections.shuffle(pills);
        Collections.shuffle(effects);

        int size = Math.min(pills.size(), effects.size());
        for (int i = 0; i < size; i++) {
            PillEffectMap.put(pills.get(i), effects.get(i));
        }

        // 写入SavedData
        PillShuffleData data = PillShuffleData.get(level);
        data.clear();
        PillEffectMap.forEach(data::setMapping);

        // 立即写入磁盘
        try {
            level.getDataStorage().save();
        } catch (Exception e) {}
    }

    //================ NBT序列化部分 =================//
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        PillEffectMap.forEach((pillId, effectId) -> tag.putInt(String.valueOf(pillId), effectId));
        return tag;
    }

    public void loadFromTag(CompoundTag tag) {
        PillEffectMap.clear();
        for (String key : tag.getAllKeys()) {
            PillEffectMap.put(Integer.parseInt(key), tag.getInt(key));
        }
    }

    //================ 特殊方法 =================//
    public void triggerRandomEffect(Player player, boolean isHorsePill){
        RandomSource random = player.getRandom();

        ArrayList<Integer> keyList = new ArrayList<>(registeredEffects.keySet());

        int effectId = keyList.get(random.nextInt(keyList.size()));

        if (isHorsePill){
            getEffectFromEffectId(effectId).onUseH(player, true);
        }else{
            getEffectFromEffectId(effectId).onUse(player, true);
        }
    }


    //================ 初始化 =================//
    public void init() {
        registerEffects(
                new IFoundPills(),
                new BallsOfSteel(),
                new HealthDown(),
                new HealthUp(),
                new FullHealth(),
                new BadGas(),
                new BadTrip(),
                new ExplosiveDiarrhea(),
                new Puberty(),
                new RangeDown(),
                new RangeUp(),
                new SpeedDown(),
                new SpeedUp(),
                new TearsDown(),
                new TearsUp(),
                new LuckDown(),
                new LuckUp(),
                new Telepills(),
                new Hematemesis(),
                new Paralysis(),
                new ICanSeeForever(),
                new Pheromones(),
                new Amnesia(),
                new LemonParty(),
                new Percs(),
                new Addicted(),
                new OneMakesYouLarger(),
                new OneMakesYouSmall(),
                new PowerPill(),
                new RetroVision(),
                new FriendsTillTheEnd(),
                new SomethingsWrong(),
                new ImDrowsy(),
                new ImExcited(),
                new ShotSpeedDown(),
                new ShotSpeedUp(),
                new ExperimentalPill(),
                new Gulp(),
                new Vurp(),
                new RUAWizard(),
                new Energy48(),
                new QuestionPill(),
                new BombsAreKey()
        );
    }
}
