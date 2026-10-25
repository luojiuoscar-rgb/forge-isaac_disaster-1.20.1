package net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.client;

import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.core.HolyLightSnapshot;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/** Projects an independent decal onto exposed collision tops within one block of its impact. */
final class HolyLightScorchProjection {
    private static final double EPSILON = 0.0001D;
    private final HolyLightSnapshot snapshot;
    private final BlockPos[] positions;
    private final BlockState[] states;
    private final int minY;
    private final int maxY;
    private final List<Face> faces = new ArrayList<>();
    private long checkedTick = Long.MIN_VALUE;

    HolyLightScorchProjection(HolyLightSnapshot snapshot) {
        this.snapshot = snapshot;
        var point = snapshot.position();
        minY = Mth.floor(point.y - 1.0D) - 1;
        maxY = Mth.floor(point.y + 0.5D);
        int minX = Mth.floor(point.x - snapshot.radius());
        int maxX = Mth.floor(point.x + snapshot.radius());
        int minZ = Mth.floor(point.z - snapshot.radius());
        int maxZ = Mth.floor(point.z + snapshot.radius());
        positions = new BlockPos[(maxX - minX + 1) * (maxZ - minZ + 1) * (maxY - minY + 1)];
        states = new BlockState[positions.length];
        int index = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) positions[index++] = new BlockPos(x, y, z);
            }
        }
    }

    List<Face> faces(ClientLevel level) {
        long tick = level.getGameTime();
        if (checkedTick == tick) return faces;
        boolean changed = checkedTick == Long.MIN_VALUE;
        checkedTick = tick;
        for (int i = 0; i < positions.length; i++) {
            BlockPos pos = positions[i];
            // getBlockState must never be the operation which requests a missing chunk.
            BlockState state = level.hasChunkAt(pos) ? level.getBlockState(pos) : null;
            changed |= states[i] != state;
            states[i] = state;
        }
        if (changed) rebuild(level);
        return faces;
    }

    private void rebuild(ClientLevel level) {
        faces.clear();
        int height = maxY - minY + 1;
        List<AABB> boxes = new ArrayList<>();
        for (int column = 0; column < positions.length; column += height) {
            boxes.clear();
            for (int offset = 0; offset < height; offset++) {
                int index = column + offset;
                if (states[index] == null) continue;
                BlockPos pos = positions[index];
                for (AABB box : states[index].getCollisionShape(level, pos).toAabbs()) {
                    boxes.add(box.move(pos));
                }
            }
            for (AABB box : boxes) projectTop(box, boxes);
        }
    }

    private void projectTop(AABB box, List<AABB> occluders) {
        var point = snapshot.position();
        if (box.maxY < point.y - 1.0D - EPSILON || box.maxY > point.y + 0.5D + EPSILON) return;
        double radius = snapshot.radius();
        double x0 = Math.max(box.minX, point.x - radius);
        double x1 = Math.min(box.maxX, point.x + radius);
        double z0 = Math.max(box.minZ, point.z - radius);
        double z1 = Math.min(box.maxZ, point.z + radius);
        if (x1 - x0 <= EPSILON || z1 - z0 <= EPSILON) return;
        List<Face> pieces = new ArrayList<>();
        pieces.add(new Face(x0, z0, x1, z1, box.maxY));
        for (AABB above : occluders) {
            if (above.maxY <= box.maxY + EPSILON) continue;
            List<Face> remainder = new ArrayList<>();
            for (Face piece : pieces) subtract(piece, above, remainder);
            pieces = remainder;
            if (pieces.isEmpty()) return;
        }
        faces.addAll(pieces);
    }

    private static void subtract(Face face, AABB box, List<Face> result) {
        double x0 = Math.max(face.x0, box.minX);
        double x1 = Math.min(face.x1, box.maxX);
        double z0 = Math.max(face.z0, box.minZ);
        double z1 = Math.min(face.z1, box.maxZ);
        if (x1 <= x0 + EPSILON || z1 <= z0 + EPSILON) {
            result.add(face);
            return;
        }
        add(result, face.x0, face.z0, x0, face.z1, face.y);
        add(result, x1, face.z0, face.x1, face.z1, face.y);
        add(result, x0, face.z0, x1, z0, face.y);
        add(result, x0, z1, x1, face.z1, face.y);
    }

    private static void add(List<Face> result, double x0, double z0, double x1, double z1, double y) {
        if (x1 - x0 > EPSILON && z1 - z0 > EPSILON) result.add(new Face(x0, z0, x1, z1, y));
    }

    record Face(double x0, double z0, double x1, double z1, double y) { }
}
