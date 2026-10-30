package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.luojiuoscar.isaac_disaster.bullet.collision.SweptCollision;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.mom_knife.MomKnifePose;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Contacts follow the same blade samples as entity damage, including a sub-tick turnaround. */
final class PlayerMomsKnifeBlockContacts {
    private PlayerMomsKnifeBlockContacts() {}

    static List<BlockHitResult> find(ServerLevel level, List<PlayerMomsKnifeMotion.Blade> path, Set<BlockPos> previous) {
        Map<BlockPos, OrderedHit> hits = new HashMap<>();
        for (int i = 1; i < path.size(); i++) {
            var from = path.get(i - 1);
            var to = path.get(i);
            AABB bounds = from.bounds().minmax(to.bounds());
            for (BlockPos cursor : BlockPos.betweenClosed(BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ),
                    BlockPos.containing(bounds.maxX, bounds.maxY, bounds.maxZ))) {
                if (previous.contains(cursor) || hits.containsKey(cursor) || !level.hasChunkAt(cursor)) continue;
                var state = level.getBlockState(cursor);
                if (state.isAir()) continue;
                Contact first = null;
                for (AABB part : state.getCollisionShape(level, cursor).toAabbs()) {
                    Contact hit = contact(from, to, part.move(cursor));
                    if (hit != null && (first == null || hit.fraction() < first.fraction())) first = hit;
                }
                if (first != null) {
                    BlockPos pos = cursor.immutable();
                    hits.put(pos, new OrderedHit(i - 1 + first.fraction(),
                            new BlockHitResult(first.point(), first.face(), pos, false)));
                }
            }
        }
        List<OrderedHit> ordered = new ArrayList<>(hits.values());
        ordered.sort(Comparator.comparingDouble(OrderedHit::order)
                .thenComparingLong(hit -> hit.hit().getBlockPos().asLong()));
        return ordered.stream().map(OrderedHit::hit).toList();
    }

    @Nullable
    static Contact contact(PlayerMomsKnifeMotion.Blade from, PlayerMomsKnifeMotion.Blade to, AABB box) {
        AABB expanded = box.inflate(MomKnifePose.BLADE_RADIUS);
        Contact first = segment(from.base(), from.tip(), expanded, true, 0);
        int samples = (int) Math.ceil((MomKnifePose.BLADE_TIP_DISTANCE - MomKnifePose.BLADE_BASE_DISTANCE)
                / MomKnifePose.BLADE_RADIUS);
        for (int i = 0; i <= samples; i++) {
            double fraction = (double) i / samples;
            Contact hit = segment(from.base().lerp(from.tip(), fraction), to.base().lerp(to.tip(), fraction),
                    expanded, false, 0);
            if (hit != null && (first == null || hit.fraction() < first.fraction())) first = hit;
        }
        Contact last = segment(to.base(), to.tip(), expanded, true, 1);
        return first == null ? last : first;
    }

    @Nullable
    private static Contact segment(Vec3 start, Vec3 end, AABB box, boolean cap, double time) {
        var hit = SweptCollision.segmentAabbDetailed(start, end, box);
        if (!hit.hit() || hit.outwardFace() == null) return null;
        return new Contact(cap ? time : hit.parameter(), start.lerp(end, hit.parameter()), hit.outwardFace());
    }

    record Contact(double fraction, Vec3 point, Direction face) {}
    private record OrderedHit(double order, BlockHitResult hit) {}
}
