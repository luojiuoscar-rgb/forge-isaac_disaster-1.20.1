package net.luojiuoscar.isaac_disaster.helper;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

/**
 * 任务调度器
 * 支持全局任务和按玩家独立任务
 */
@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID)
public class ScheduledFuncHelper {
    private ScheduledFuncHelper() {
    }

    // 自动生成 taskId
    private static int NEXT_TASK_ID = 1;

    // taskId -> Task
    private static final Map<Integer, ScheduledTask> TASKS = new HashMap<>();

    // 玩家 UUID -> taskId 集合，用于快速查找/清理
    private static final Map<UUID, Set<Integer>> PLAYER_TASK_INDEX = new HashMap<>();

    // 延迟加入/删除集合
    private static final Set<ScheduledTask> PENDING_ADD = new HashSet<>();
    private static final Set<Integer> PENDING_REMOVE = new HashSet<>();

    // 任务实例
    private static class ScheduledTask {
        final int taskId;
        final ResourceLocation typeId; // 任务类型
        final UUID owner; // null = 全局任务
        int timeLeft;
        final int intervalTicks; // 固定周期
        int repeats;
        final Runnable runnable;

        ScheduledTask(int taskId,
                      ResourceLocation typeId,
                      UUID owner,
                      int intervalTicks,
                      int initialTick,
                      int repeats,
                      Runnable runnable) {

            this.taskId = taskId;
            this.typeId = typeId;
            this.owner = owner;
            this.intervalTicks = intervalTicks;
            this.repeats = repeats;
            this.runnable = runnable;

            // 第一次执行时间 = interval - initialTick
            int firstDelay = intervalTicks - initialTick;
            if (firstDelay <= 0) firstDelay = 1;

            this.timeLeft = firstDelay;
        }
    }

    /** ---------------- 全局任务 API ---------------- */
    public static int schedule(ResourceLocation typeId,
                               int ticks,
                               int initialTick,
                               int repeats,
                               boolean override,
                               Runnable runnable) {
        return scheduleInternal(typeId, null, ticks, initialTick, repeats, override, runnable);
    }

    /** ---------------- 玩家任务 API ---------------- */
    /**
     * 为玩家创建一个重复执行的任务
     * @param ticks 循环时间
     * @param initialTick 首次启动时，处在的时间轴位置。用于决定第一次循环何时触发
     * @param repeats 总共执行次数。为0不触发
     * @param override 是否替代掉具有同id的人物。玩家之间互相独立
     * @param runnable 任务
     */
    public static int scheduleForPlayer(UUID playerUUID,
                                        ResourceLocation typeId,
                                        int ticks,
                                        int initialTick,
                                        int repeats,
                                        boolean override,
                                        Runnable runnable) {
        return scheduleInternal(typeId, playerUUID, ticks, initialTick, repeats, override, runnable);
    }

    /**
     * Appends finite executions without changing an existing task's interval or callback.
     * New tasks first run on the next scheduler pass; nonpositive counts create no task.
     * Existing unbounded tasks remain unbounded.
     */
    public static int scheduleOrAppendForPlayer(UUID playerUUID, ResourceLocation typeId,
                                               int ticks, int count, Runnable runnable) {
        Objects.requireNonNull(playerUUID, "player UUID");
        Objects.requireNonNull(typeId, "task type");
        Objects.requireNonNull(runnable, "task callback");
        if (count <= 0) return 0;
        ScheduledTask existing = null;
        for (ScheduledTask task : TASKS.values()) {
            if (task.typeId.equals(typeId) && playerUUID.equals(task.owner)
                    && !PENDING_REMOVE.contains(task.taskId)) {
                existing = task;
                break;
            }
        }
        if (existing == null) {
            for (ScheduledTask task : PENDING_ADD) {
                if (task.typeId.equals(typeId) && playerUUID.equals(task.owner)) {
                    existing = task;
                    break;
                }
            }
        }
        if (existing != null) {
            if (existing.repeats >= 0) {
                existing.repeats = (int) Math.min(Integer.MAX_VALUE, (long) existing.repeats + count);
            }
            return existing.taskId;
        }
        int interval = Math.max(1, ticks);
        ScheduledTask task = new ScheduledTask(NEXT_TASK_ID++, typeId, playerUUID,
                interval, interval - 1, count, runnable);
        return enqueue(task);
    }

    /** 内部调度逻辑 */
    private static int scheduleInternal(ResourceLocation typeId,
                                        UUID owner,
                                        int ticks,
                                        int initialTick,
                                        int repeats,
                                        boolean override,
                                        Runnable runnable) {

        if (override) {
            clearByType(typeId, owner);
        }

        int taskId = NEXT_TASK_ID++;
        ScheduledTask task = new ScheduledTask(
                taskId,
                typeId,
                owner,
                ticks,
                initialTick,
                repeats,
                runnable
        );

        return enqueue(task);
    }

    private static int enqueue(ScheduledTask task) {
        // 延迟加入 TASKS
        PENDING_ADD.add(task);

        if (task.owner != null) {
            PLAYER_TASK_INDEX.computeIfAbsent(task.owner, k -> new HashSet<>()).add(task.taskId);
        }

        return task.taskId;
    }

    /** ---------------- 清除任务 ---------------- */
    public static void clearByType(ResourceLocation typeId, UUID owner) {
        for (ScheduledTask task : TASKS.values()) {
            if (task.typeId.equals(typeId) && Objects.equals(task.owner, owner)) {
                PENDING_REMOVE.add(task.taskId);
            }
        }

        // 避免漏掉 pendingAdd 中的任务
        PENDING_ADD.removeIf(task -> {
            if (!task.typeId.equals(typeId) || !Objects.equals(task.owner, owner)) return false;
            removeFromPlayerIndex(task);
            return true;
        });
    }

    public static void clearPlayerTasks(UUID playerUUID) {
        Set<Integer> tasks = PLAYER_TASK_INDEX.getOrDefault(playerUUID, Collections.emptySet());
        PENDING_REMOVE.addAll(tasks);

        // pendingAdd 中也移除
        PENDING_ADD.removeIf(task -> {
            if (!playerUUID.equals(task.owner)) return false;
            removeFromPlayerIndex(task);
            return true;
        });
    }

    /** ---------------- 查询任务 ---------------- */
    public static Set<Integer> getPlayerTasks(UUID playerUUID) {
        return PLAYER_TASK_INDEX.getOrDefault(playerUUID, Collections.emptySet());
    }

    /** ---------------- tick ---------------- */
    public static void tick(MinecraftServer server) {
        // 延迟加入新任务
        for (ScheduledTask task : PENDING_ADD) {
            TASKS.put(task.taskId, task);
        }
        PENDING_ADD.clear();

        // 遍历执行
        Iterator<Map.Entry<Integer, ScheduledTask>> iterator = TASKS.entrySet().iterator();
        while (iterator.hasNext()) {
            ScheduledTask task = iterator.next().getValue();
            if (PENDING_REMOVE.contains(task.taskId)) continue;
            task.timeLeft--;

            if (task.timeLeft <= 0) {
                if (task.runnable != null) {
                    task.runnable.run();
                }

                if (task.repeats > 0) task.repeats--;

                if (task.repeats == 0) {
                    PENDING_REMOVE.add(task.taskId);
                } else {
                    task.timeLeft = task.intervalTicks;
                }
            }
        }

        // 延迟删除
        for (int taskId : PENDING_REMOVE) {
            ScheduledTask task = TASKS.remove(taskId);
            if (task != null) removeFromPlayerIndex(task);
        }
        PENDING_REMOVE.clear();
    }

    private static void removeFromPlayerIndex(ScheduledTask task) {
        if (task.owner == null) return;
        Set<Integer> playerTasks = PLAYER_TASK_INDEX.get(task.owner);
        if (playerTasks == null) return;
        playerTasks.remove(task.taskId);
        if (playerTasks.isEmpty()) PLAYER_TASK_INDEX.remove(task.owner);
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        TASKS.clear();
        PLAYER_TASK_INDEX.clear();
        PENDING_ADD.clear();
        PENDING_REMOVE.clear();
        NEXT_TASK_ID = 1;
    }
}
