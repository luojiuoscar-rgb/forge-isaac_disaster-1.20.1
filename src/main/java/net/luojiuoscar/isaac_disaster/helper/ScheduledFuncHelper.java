package net.luojiuoscar.isaac_disaster.helper;

import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

/**
 * 任务调度器
 * 支持全局任务和按玩家独立任务
 */
public class ScheduledFuncHelper {

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
    public static int scheduleForPlayer(UUID playerUUID,
                                        ResourceLocation typeId,
                                        int ticks,
                                        int initialTick,
                                        int repeats,
                                        boolean override,
                                        Runnable runnable) {
        return scheduleInternal(typeId, playerUUID, ticks, initialTick, repeats, override, runnable);
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

        // 延迟加入 TASKS
        PENDING_ADD.add(task);

        if (owner != null) {
            PLAYER_TASK_INDEX.computeIfAbsent(owner, k -> new HashSet<>()).add(taskId);
        }

        return taskId;
    }

    /** ---------------- 清除任务 ---------------- */
    public static void clearByType(ResourceLocation typeId, UUID owner) {
        for (ScheduledTask task : TASKS.values()) {
            if (task.typeId.equals(typeId) && Objects.equals(task.owner, owner)) {
                PENDING_REMOVE.add(task.taskId);
            }
        }

        // 避免漏掉 pendingAdd 中的任务
        PENDING_ADD.removeIf(task -> task.typeId.equals(typeId) && Objects.equals(task.owner, owner));
    }

    public static void clearPlayerTasks(UUID playerUUID) {
        Set<Integer> tasks = PLAYER_TASK_INDEX.getOrDefault(playerUUID, Collections.emptySet());
        PENDING_REMOVE.addAll(tasks);

        // pendingAdd 中也移除
        PENDING_ADD.removeIf(task -> playerUUID.equals(task.owner));
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
            if (task != null && task.owner != null) {
                Set<Integer> playerTasks = PLAYER_TASK_INDEX.get(task.owner);
                if (playerTasks != null) {
                    playerTasks.remove(task.taskId);
                    if (playerTasks.isEmpty()) PLAYER_TASK_INDEX.remove(task.owner);
                }
            }
        }
        PENDING_REMOVE.clear();
    }
}