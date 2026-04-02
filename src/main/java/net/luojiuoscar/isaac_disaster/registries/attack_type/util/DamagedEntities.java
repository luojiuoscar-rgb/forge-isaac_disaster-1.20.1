package net.luojiuoscar.isaac_disaster.registries.attack_type.util;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * DamagedEntities
 * 类似 LinkedHashSet，但添加已存在元素时会把它移到末尾
 * 用于记录子弹/攻击已经伤害过的实体，避免重复伤害，同时保持顺序
 */
public class DamagedEntities implements Iterable<UUID> {

    private final LinkedHashSet<UUID> entities;

    public DamagedEntities() {
        this.entities = new LinkedHashSet<>();
    }

    /**
     * 添加实体
     * 如果已存在，则移动到末尾
     */
    public void add(UUID e) {
        if (entities.contains(e)) {
            entities.remove(e);
        }
        entities.add(e);
    }

    /**
     * 移除实体
     */
    public boolean remove(UUID e) {
        return entities.remove(e);
    }

    /**
     * 是否包含实体
     */
    public boolean contains(UUID e) {
        return entities.contains(e);
    }

    /**
     * 清空所有记录
     */
    public void clear() {
        entities.clear();
    }

    /**
     * 获取当前实体数量
     */
    public int size() {
        return entities.size();
    }

    /**
     * 返回有序列表（不可修改原集合）
     */
    public List<UUID> toList() {
        return new ArrayList<>(entities);
    }

    /**
     * 返回迭代器，可用于 for-each 遍历
     */
    @Override
    public @NotNull Iterator<UUID> iterator() {
        return entities.iterator();
    }

    public UUID get(int index) {
        if (index < 0 || index >= entities.size()) return null;

        int i = 0;
        for (UUID e : entities) {
            if (i == index) return e;
            i++;
        }
        return null; // 理论上不会到这里
    }

    /**
     * 获取集合中的第一个实体（可选）
     */
    public UUID first() {
        Iterator<UUID> it = entities.iterator();
        return it.hasNext() ? it.next() : null;
    }

    /**
     * 获取集合中的最后一个实体（可选）
     */
    public UUID last() {
        UUID last = null;
        for (UUID e : entities) {
            last = e;
        }
        return last;
    }
}