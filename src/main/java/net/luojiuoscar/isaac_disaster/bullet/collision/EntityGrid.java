package net.luojiuoscar.isaac_disaster.bullet.collision;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.IdentityHashMap;

/** Spatial index for target-capable living entities using 2x2x2 block cells. */
public final class EntityGrid {
    private static final int CELL_SHIFT = 1;
    private final Map<CellKey, List<LivingEntity>> cells = new HashMap<>();
    private final IdentityHashMap<LivingEntity, Integer> queryMarks = new IdentityHashMap<>();
    private int queryToken;

    /** Rebuilds the target index from currently alive entities. */
    public void rebuild(Collection<? extends LivingEntity> entities) {
        cells.clear();
        queryMarks.clear();
        queryToken = 0;
        for (LivingEntity entity : entities) {
            if (entity != null && entity.isAlive()) index(entity);
        }
    }

    /** Adds one entity to every 2x2x2 cell touched by its bounding box. */
    public void index(LivingEntity entity) {
        AABB box = entity.getBoundingBox();
        int minX = cell(Mth.floor(box.minX));
        int maxX = cell(Mth.floor(box.maxX));
        int minY = cell(Mth.floor(box.minY));
        int maxY = cell(Mth.floor(box.maxY));
        int minZ = cell(Mth.floor(box.minZ));
        int maxZ = cell(Mth.floor(box.maxZ));
        for (int x = minX; x <= maxX; x++) for (int y = minY; y <= maxY; y++) for (int z = minZ; z <= maxZ; z++)
            cells.computeIfAbsent(new CellKey(x, y, z), ignored -> new ArrayList<>()).add(entity);
    }

    /** Returns unique indexed entities whose cells overlap the supplied bounds. */
    public List<LivingEntity> query(AABB bounds) {
        List<LivingEntity> result = new ArrayList<>();
        query(bounds, result);
        return result;
    }

    /** Fills a caller-owned result buffer, avoiding a list allocation for hot-path queries. */
    public void query(AABB bounds, List<LivingEntity> result) {
        result.clear();
        int minX = cell(Mth.floor(bounds.minX));
        int maxX = cell(Mth.floor(bounds.maxX));
        int minY = cell(Mth.floor(bounds.minY));
        int maxY = cell(Mth.floor(bounds.maxY));
        int minZ = cell(Mth.floor(bounds.minZ));
        int maxZ = cell(Mth.floor(bounds.maxZ));
        int token = ++queryToken;
        for (int x = minX; x <= maxX; x++) for (int y = minY; y <= maxY; y++) for (int z = minZ; z <= maxZ; z++) {
            List<LivingEntity> bucket = cells.get(new CellKey(x, y, z));
            if (bucket != null) {
                for (LivingEntity entity : bucket) {
                    Integer mark = queryMarks.get(entity);
                    if (mark == null || mark != token) { queryMarks.put(entity, token); result.add(entity); }
                }
            }
        }
    }

    public int cellCount() { return cells.size(); }
    private static int cell(int block) { return block >> CELL_SHIFT; }
    /** A collision-free key for the full signed cell coordinates. */
    private record CellKey(int x, int y, int z) { }
}
