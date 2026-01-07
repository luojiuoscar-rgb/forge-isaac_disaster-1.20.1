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

    // 任务实例
    private static class ScheduledTask {
        final int taskId;
        final ResourceLocation typeId; // 任务类型
        final UUID owner; // null = 全局任务
        int timeLeft;
        final int initialTicks;
        int repeats;
        final Runnable runnable;

        ScheduledTask(int taskId, ResourceLocation typeId, UUID owner, int ticks, int repeats, Runnable runnable) {
            this.taskId = taskId;
            this.typeId = typeId;
            this.owner = owner;
            this.timeLeft = ticks;
            this.initialTicks = ticks;
            this.repeats = repeats;
            this.runnable = runnable;
        }
    }

    /** ---------------- 全局任务 API ---------------- */

    /**
     * 添加全局任务
     * @param typeId 任务类型
     * @param ticks 倒计时
     * @param repeats 执行次数 (0=执行一次, >0 执行 n 次)
     * @param override 是否覆盖已有同 typeId 全局任务
     * @param runnable 执行逻辑
     * @return 任务实例 taskId
     */
    public static int schedule(ResourceLocation typeId, int ticks, int repeats, boolean override, Runnable runnable) {
        return scheduleInternal(typeId, null, ticks, repeats, override, runnable);
    }

    /** ---------------- 玩家任务 API ---------------- */

    /**
     * 添加玩家任务
     * @param playerUUID 玩家 UUID
     * @param typeId 任务类型
     * @param ticks 倒计时
     * @param repeats 执行次数
     * @param override 是否覆盖该玩家已有同 typeId 任务
     * @param runnable 执行逻辑
     * @return 任务实例 taskId
     */
    public static int scheduleForPlayer(UUID playerUUID, ResourceLocation typeId, int ticks, int repeats, boolean override, Runnable runnable) {
        return scheduleInternal(typeId, playerUUID, ticks, repeats, override, runnable);
    }

    /**
     * 内部调度逻辑
     */
    private static int scheduleInternal(ResourceLocation typeId, UUID owner, int ticks, int repeats, boolean override, Runnable runnable) {
        if (override) {
            // 清除已有同 typeId 的任务
            clearByType(typeId, owner);
        }

        int taskId = NEXT_TASK_ID++;
        ScheduledTask task = new ScheduledTask(taskId, typeId, owner, ticks, repeats, runnable);
        TASKS.put(taskId, task);

        if (owner != null) {
            PLAYER_TASK_INDEX.computeIfAbsent(owner, k -> new HashSet<>()).add(taskId);
        }

        return taskId;
    }

    /** ---------------- 清除任务 ---------------- */

    /**
     * 清除全局或玩家指定 typeId 的任务
     * @param typeId 任务类型
     * @param owner 玩家 UUID，null = 全局
     */
    public static void clearByType(ResourceLocation typeId, UUID owner) {
        Iterator<Map.Entry<Integer, ScheduledTask>> it = TASKS.entrySet().iterator();
        while (it.hasNext()) {
            ScheduledTask task = it.next().getValue();
            if (task.typeId.equals(typeId) && Objects.equals(task.owner, owner)) {
                it.remove();
                if (owner != null) {
                    Set<Integer> playerTasks = PLAYER_TASK_INDEX.get(owner);
                    if (playerTasks != null) {
                        playerTasks.remove(task.taskId);
                        if (playerTasks.isEmpty()) PLAYER_TASK_INDEX.remove(owner);
                    }
                }
            }
        }
    }

    /**
     * 清除玩家的所有任务
     */
    public static void clearPlayerTasks(UUID playerUUID) {
        Set<Integer> tasks = PLAYER_TASK_INDEX.remove(playerUUID);
        if (tasks == null) return;
        for (int taskId : tasks) {
            TASKS.remove(taskId);
        }
    }

    /** ---------------- 查询任务 ---------------- */

    /**
     * 获取玩家当前所有任务 taskId
     */
    public static Set<Integer> getPlayerTasks(UUID playerUUID) {
        return PLAYER_TASK_INDEX.getOrDefault(playerUUID, Collections.emptySet());
    }

    /** ---------------- tick ---------------- */

    /**
     * 每tick调用一次（主线程）
     */
    public static void tick(MinecraftServer server) {
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
                    iterator.remove();
                    if (task.owner != null) {
                        Set<Integer> playerTasks = PLAYER_TASK_INDEX.get(task.owner);
                        if (playerTasks != null) {
                            playerTasks.remove(task.taskId);
                            if (playerTasks.isEmpty()) PLAYER_TASK_INDEX.remove(task.owner);
                        }
                    }
                } else {
                    task.timeLeft = task.initialTicks;
                }
            }
        }
    }
}
