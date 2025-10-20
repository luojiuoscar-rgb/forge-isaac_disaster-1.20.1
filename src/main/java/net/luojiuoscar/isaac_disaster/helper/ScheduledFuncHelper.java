package net.luojiuoscar.isaac_disaster.helper;

import net.minecraft.server.MinecraftServer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ScheduledFuncHelper {

    /**
     * id -> (剩余tick, 初始tick, 剩余执行次数)
     */
    private static final Map<String, TaskEntry> TASKS = new HashMap<>();

    /**
     * 执行逻辑
     */
    private static final Map<String, Runnable> RUNTIMES = new HashMap<>();

    private static class TaskEntry {
        int timeLeft;
        int initialTicks;
        int repeats;

        TaskEntry(int timeLeft, int initialTicks, int repeats) {
            this.timeLeft = timeLeft;
            this.initialTicks = initialTicks;
            this.repeats = repeats;
        }
    }

    /**
     * 清除指定id以及自动编号（如 test, test1, test2）
     */
    public static void clear(String baseId) {
        Iterator<String> it = TASKS.keySet().iterator();
        while (it.hasNext()) {
            String id = it.next();
            if (id.equals(baseId) || id.matches(baseId + "\\d+")) {
                it.remove();
                RUNTIMES.remove(id);
            }
        }
    }

    /**
     * 调度任务
     * 计划中的任务不会在退出游戏后保存！！会丢失！！
     * @param id       基础ID
     * @param ticks    倒计时
     * @param repeats  执行次数 (0=执行一次, >0 执行 n 次)
     * @param override 是否覆盖已有 id 及其后缀
     * @param runnable 执行逻辑
     * @return 最终使用的 id
     */
    public static String schedule(String id, int ticks, int repeats, boolean override, Runnable runnable) {
        if (override) {
            clear(id);
            TASKS.put(id, new TaskEntry(ticks, ticks, repeats));
            RUNTIMES.put(id, runnable);
            return id;
        }

        // 自动添加后缀
        String finalId = id;
        if (TASKS.containsKey(finalId) || RUNTIMES.containsKey(finalId)) {
            int suffix = 1;
            while (TASKS.containsKey(finalId + suffix) || RUNTIMES.containsKey(finalId + suffix)) {
                suffix++;
            }
            finalId = finalId + suffix;
        }

        TASKS.put(finalId, new TaskEntry(ticks, ticks, repeats));
        RUNTIMES.put(finalId, runnable);
        return finalId;
    }

    /**
     * 每tick调用一次（主线程）
     */
    public static void tick(MinecraftServer server) {
        Iterator<Map.Entry<String, TaskEntry>> iterator = TASKS.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, TaskEntry> entry = iterator.next();
            String id = entry.getKey();
            TaskEntry task = entry.getValue();

            task.timeLeft--;

            if (task.timeLeft <= 0) {
                Runnable r = RUNTIMES.get(id);
                if (r != null) {
                    r.run();
                }

                if (task.repeats > 0) {
                    task.repeats--;
                }

                if (task.repeats == 0) {
                    iterator.remove();
                    RUNTIMES.remove(id);
                } else {
                    task.timeLeft = task.initialTicks;
                }
            }
        }
    }
}
