package net.luojiuoscar.isaac_disaster.helper;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

public class EventHelper {

    /**
     * 根据权重随机触发一个事件
     *
     * @param level  当前世界
     * @param player 触发的玩家（可用于生成物品或消息等）
     * @param rand   随机源
     * @param events 可变参数，每一项为事件和权重数组：{Runnable action, double weight}
     */
    public static void triggerWeightedEvent(ServerLevel level, Player player, RandomSource rand, EventWeight... events) {
        double totalWeight = 0.0;
        for (EventWeight ew : events) totalWeight += ew.weight;

        double r = rand.nextDouble() * totalWeight;
        double cumulative = 0.0;

        for (EventWeight ew : events) {
            cumulative += ew.weight;
            if (r <= cumulative) {
                ew.action.run();
                break;
            }
        }
    }

    public static class EventWeight {
        public final Runnable action;
        public final double weight;

        public EventWeight(Runnable action, double weight) {
            this.action = action;
            this.weight = weight;
        }
    }
}
