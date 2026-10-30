package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.luojiuoscar.isaac_disaster.bullet.core.TrajectoryEvaluator;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.trajectory.ModTrajectoryModules;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryModule;
import net.luojiuoscar.isaac_disaster.registries.trajectory.ReturningKnifeTrajectory;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectorySpec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.Comparator;
import java.util.function.DoubleFunction;
import java.util.function.Function;

/** Adapts shared trajectory motion to a knife's timed sweep and blade geometry. */
final class PlayerMomsKnifeTrajectory {
    private PlayerMomsKnifeTrajectory() {}

    static String selectReturning(IBulletObject knife) {
        return knife.getTrajectorySpecs().stream().filter(spec -> {
            var module = ModTrajectoryModules.resolve(spec.id());
            return module instanceof ReturningKnifeTrajectory && module.appliesTo(knife);
        }).sorted(Comparator.comparingInt((TrajectorySpec spec) ->
                ModTrajectoryModules.resolve(spec.id()).priority()).reversed().thenComparing(spec -> spec.id().toString()))
                .map(spec -> spec.id().toString()).findFirst().orElse("");
    }

    @Nullable
    static ReturningKnifeTrajectory returning(String id) {
        if (id.isEmpty()) return null;
        ResourceLocation key = ResourceLocation.tryParse(id);
        return key != null && ModTrajectoryModules.resolve(key) instanceof ReturningKnifeTrajectory returning ? returning : null;
    }

    static List<PlayerMomsKnifeTrail.Sample> returningSamples(ReturningKnifeTrajectory profile,
            double fromAge, double toAge, double duration, int viewSteps, double launchedAt,
            DoubleFunction<PlayerMomsKnifeMotion.Blade> pose) {
        TreeSet<Double> fractions = new TreeSet<>();
        int steps = profile.sweepSegments(fromAge, toAge, duration) + Math.max(0, viewSteps - 1);
        double start = Math.max(0, Math.min(duration, fromAge));
        double end = Math.max(0, Math.min(duration, toAge));
        for (int i = 0; i <= steps; i++) fractions.add((double) i / steps);
        if (toAge > fromAge) {
            for (int i = 0; i <= steps; i++) {
                double fraction = (start + (end - start) * i / steps - fromAge) / (toAge - fromAge);
                fractions.add(Math.max(0, Math.min(1, fraction)));
            }
            double apex = (duration / 2 - fromAge) / (toAge - fromAge);
            if (apex > 0 && apex <= 1) {
                fractions.add(apex);
                double beforeApex = apex - Math.min(1e-6, duration * 1e-6) / (toAge - fromAge);
                if (beforeApex > 0) fractions.add(beforeApex);
            }
        }
        List<PlayerMomsKnifeTrail.Sample> result = new ArrayList<>();
        for (double fraction : fractions) {
            result.add(new PlayerMomsKnifeTrail.Sample(launchedAt + fromAge + (toAge - fromAge) * fraction,
                    pose.apply(fraction)));
        }
        return result;
    }

    static List<PlayerMomsKnifeMotion.Blade> apply(IBulletObject knife,
            List<PlayerMomsKnifeMotion.Blade> base, Vec3 facing, boolean dock) {
        return apply(knife, base, facing, dock, ModTrajectoryModules::resolve);
    }

    static List<PlayerMomsKnifeMotion.Blade> apply(IBulletObject knife,
            List<PlayerMomsKnifeMotion.Blade> base, Vec3 facing, boolean dock,
            Function<ResourceLocation, TrajectoryModule<?>> modules) {
        boolean applicable = knife.getTrajectorySpecs().stream().anyMatch(spec -> {
            TrajectoryModule<?> module = modules.apply(spec.id());
            return module != null && module.appliesTo(knife);
        });
        if (!applicable) return base;

        Vec3 start = knife.getPosition();
        Vec3 baseStart = base.get(0).anchor();
        var last = base.get(base.size() - 1);
        Vec3 baseVelocity = last.anchor().subtract(baseStart);
        // Returning knives reverse their base motion; offset modules must use that current frame.
        if (baseVelocity.lengthSqr() > 1e-12) knife.getTrajectoryRuntime().kinematics().orient(baseVelocity);
        knife.getTrajectoryRuntime().updateAnchor(knife.getOwner());
        var motion = TrajectoryEvaluator.evaluate(knife, baseVelocity, distance(base), 1, modules);
        Vec3 end = dock ? last.anchor() : motion.desiredPosition();
        if (end == null) end = start.add(baseVelocity);
        Vec3 direction = last.tip().subtract(last.base()).normalize();
        if (!dock && baseVelocity.lengthSqr() > 1e-12
                && motion.desiredVelocity() != null && motion.desiredVelocity().lengthSqr() > 1e-12) {
            Vec3 from = baseVelocity.normalize();
            Vec3 to = motion.desiredVelocity().normalize();
            Vector3d rotated = new Quaterniond().rotationTo(from.x, from.y, from.z, to.x, to.y, to.z)
                    .transform(new Vector3d(direction.x, direction.y, direction.z));
            direction = PlayerMomsKnifeMotion.unit(new Vec3(rotated.x, rotated.y, rotated.z));
        }

        // Preserve base sweep samples (including an apex inside one tick) and trajectory samples.
        TreeSet<Double> fractions = new TreeSet<>();
        for (int i = 0; i < base.size(); i++) fractions.add((double) i / (base.size() - 1));
        for (int i = 1; i <= motion.path().size(); i++) fractions.add((double) i / motion.path().size());
        List<Vec3> basePoints = base.stream().map(PlayerMomsKnifeMotion.Blade::anchor).toList();
        List<Vec3> points = new ArrayList<>();
        points.add(start);
        points.addAll(motion.path());
        if (points.size() == 1) points.add(end);
        Vec3 endpointCorrection = end.subtract(points.get(points.size() - 1));
        List<PlayerMomsKnifeMotion.Blade> result = new ArrayList<>(fractions.size());
        for (double fraction : fractions) {
            Vec3 point = sample(basePoints, fraction).add(sample(points, fraction))
                    .subtract(baseStart.add(baseVelocity.scale(fraction))).add(endpointCorrection.scale(fraction));
            result.add(PlayerMomsKnifeMotion.blade(point,
                    PlayerMomsKnifeMotion.interpolateDirection(facing, direction, fraction)));
        }
        return result;
    }

    static double distance(List<PlayerMomsKnifeMotion.Blade> path) {
        double distance = 0;
        for (int i = 1; i < path.size(); i++) distance += path.get(i - 1).anchor().distanceTo(path.get(i).anchor());
        return distance;
    }

    static List<PlayerMomsKnifeMotion.Blade> limitDistance(List<PlayerMomsKnifeMotion.Blade> path, double budget) {
        if (distance(path) <= budget) return path;
        List<PlayerMomsKnifeMotion.Blade> result = new ArrayList<>();
        result.add(path.get(0));
        double remaining = Math.max(0, budget);
        for (int i = 1; i < path.size(); i++) {
            var from = path.get(i - 1);
            var to = path.get(i);
            double length = from.anchor().distanceTo(to.anchor());
            if (length > remaining) {
                double fraction = remaining / length;
                Vec3 direction = PlayerMomsKnifeMotion.interpolateDirection(
                        from.tip().subtract(from.base()).normalize(), to.tip().subtract(to.base()).normalize(), fraction);
                result.add(PlayerMomsKnifeMotion.blade(from.anchor().lerp(to.anchor(), fraction), direction));
                break;
            }
            result.add(to);
            remaining -= length;
        }
        return result;
    }

    private static Vec3 sample(List<Vec3> points, double fraction) {
        double cursor = fraction * (points.size() - 1);
        int index = Math.min(points.size() - 2, (int) cursor);
        return points.get(index).lerp(points.get(index + 1), cursor - index);
    }
}
