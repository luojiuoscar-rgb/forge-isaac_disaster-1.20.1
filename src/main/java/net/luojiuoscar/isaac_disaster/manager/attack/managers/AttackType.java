package net.luojiuoscar.isaac_disaster.manager.attack.managers;

import java.util.HashMap;
import java.util.Map;

public enum AttackType {
    BULLET(0),
    LASER(100);






    private final int id;
    private final double priority;

    // ================= 构造与字段 =================
    AttackType(int priority) {
        this.id = this.ordinal();
        this.priority = priority;
    }

    public int getId() {
        return id;
    }

    public double getPriority() {
        return priority;
    }

    // ================= 反查支持 =================
    private static final Map<Integer, AttackType> BY_ID = new HashMap<>();

    static {
        for (AttackType type : values()) {
            BY_ID.put(type.id, type);
        }
    }

    /** 根据id获取对应的枚举类型 */
    public static AttackType byId(int id) {
        return BY_ID.get(id);
    }

    /** 根据id直接获取优先级 */
    public static double getPriorityById(int id) {
        AttackType type = BY_ID.get(id);
        return type == null ? 0 : type.priority;
    }
}
