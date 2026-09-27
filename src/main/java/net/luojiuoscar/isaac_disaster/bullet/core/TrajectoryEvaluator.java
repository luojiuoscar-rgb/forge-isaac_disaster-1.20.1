package net.luojiuoscar.isaac_disaster.bullet.core;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.trajectory.ModTrajectoryModules;
import net.luojiuoscar.isaac_disaster.registries.trajectory.OffsetTrajectoryModule;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryContext;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryFrame;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryKinematics;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryModule;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryMotion;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryState;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectorySpec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

/**
 * Shared trajectory dispatcher used by server, client and laser segment simulation. Ordered specs
 * consume one absolute frame per tick; only the relative residual introduced by a later controller
 * is carried to the next tick. This prevents an absolute path from losing a composed acceleration
 * while also avoiding duplicate application of the base forward step.
 */
public final class TrajectoryEvaluator {
    private static final Set<ResourceLocation> WARNED_MISSING = ConcurrentHashMap.newKeySet();

    private TrajectoryEvaluator() {
    }

    /** Each attached prelude owns its distance once; inherited progress is already completed work. */
    public static double remainingFreeDistance(
        IBulletObject bullet,
        java.util.function.Function<ResourceLocation, TrajectoryModule<?>> modules) {
        double total = 0;
        var seen = new java.util.HashSet<ResourceLocation>();
        List<TrajectorySpec> specs = bullet.getTrajectorySpecs();
        boolean hasPrimary =
            specs.stream()
                .anyMatch(
                    s -> {
                        TrajectoryModule<?> m = modules.apply(s.id());
                        return m != null
                            && m.role() == TrajectoryModule.Role.PRIMARY
                            && m.appliesTo(bullet);
                    });
        for (TrajectorySpec spec : specs) {
            TrajectoryModule<?> module = modules.apply(spec.id());
            if (!seen.add(spec.id()) || module == null || !module.appliesTo(bullet)) continue;
            // Offset modules can declare that a primary controller already owns this free path.
            TrajectoryState state = bullet.getTrajectoryRuntime().states().get(spec.id());
            if (module.freeDistanceCoveredByPrimary(
                state == null ? module.createState() : state, hasPrimary)) continue;
            double distance = module.maximumFreeDistance(spec.amplifier(), specs, bullet.getRange());
            if (module.role() == TrajectoryModule.Role.PRIMARY && state != null)
                distance -= module.progressDistance(state);
            total += Math.max(0, distance);
        }
        return total;
    }

    public static void initialize(IBulletObject bullet) {
        IForgeRegistry<TrajectoryModule<?>> registry =
            RegistryManager.ACTIVE.getRegistry(ModTrajectoryModules.TRAJECTORY_MODULE_KEY);
        if (registry == null) return;
        for (TrajectorySpec spec : bullet.getTrajectorySpecs()) {
            TrajectoryModule<?> module = registry.getValue(spec.id());
            if (module == null) continue;
            TrajectoryState state =
                bullet.getTrajectoryRuntime().ensureState(spec.id(), module);
            module.initialize(
                new TrajectoryContext(
                    bullet,
                    new TrajectoryContext.Input(
                        bullet.getPosition(),
                        bullet.getVelocity(),
                        bullet.getVelocity(),
                        bullet.getAcceleration(),
                        0),
                    bullet.getTrajectoryRuntime().anchor(),
                    spec.amplifier(),
                    state));
        }
    }

    public static TrajectoryMotion evaluate(
        IBulletObject bullet, Vec3 baseVelocity, double baseProgress, double deltaTicks) {
        IForgeRegistry<TrajectoryModule<?>> registry =
            RegistryManager.ACTIVE.getRegistry(ModTrajectoryModules.TRAJECTORY_MODULE_KEY);
        return evaluate(
            bullet,
            baseVelocity,
            baseProgress,
            deltaTicks,
            id -> registry == null ? null : registry.getValue(id));
    }

    /** State comes exclusively from the projectile; registry lookup remains injectable for tests. */
    public static TrajectoryMotion evaluate(
        IBulletObject bullet,
        Vec3 baseVelocity,
        double baseProgress,
        double deltaTicks,
        java.util.function.Function<ResourceLocation, TrajectoryModule<?>> modules) {
        var shot = bullet.getTrajectoryRuntime();
        List<TrajectorySpec> all = shot.specs();
        Map<ResourceLocation, TrajectoryState> states = shot.states();
        Vec3 anchor = shot.anchor();
        List<TrajectorySpec> offsets =
            all.stream()
                .filter(s -> modules.apply(s.id()) instanceof OffsetTrajectoryModule)
                .sorted(java.util.Comparator.comparing(s -> s.id().toString()))
                .toList();
        if (offsets.isEmpty())
            return evaluatePrimary(
                bullet, all, states, anchor, baseVelocity, baseProgress, deltaTicks, modules, null);
        Map<ResourceLocation, TrajectoryState> runtime = states;
        TrajectoryKinematics k = shot.kinematics();
        if (!k.initialized() || shot.suspended()) {
            boolean resumed = k.initialized();
            k.position(bullet.getPosition());
            k.velocity(bullet.getVelocity());
            if (ModAttackTypes.LASER.getId().equals(bullet.getRootTypeId()))
                k.velocity(k.velocity().normalize().scale(baseVelocity.length()));
            k.acceleration(bullet.getAcceleration());
            k.offset(Vec3.ZERO);
            k.recovery(resumed ? 0 : 2);
            k.initializeLaunchBasis(bullet.getTrajectoryRuntime().launchDirection());
            k.orient(
                k.velocity().lengthSqr() > 1e-12
                    ? k.velocity()
                    : bullet.getTrajectoryRuntime().launchDirection());
            k.initialized(true);
        } else k.position(bullet.getPosition().subtract(k.offset()));
        shot.resume();
        List<TrajectorySpec> primary =
            all.stream()
                .filter(
                    s -> {
                        TrajectoryModule<?> module = modules.apply(s.id());
                        return module != null
                            && module.role() == TrajectoryModule.Role.PRIMARY
                            && module.appliesTo(bullet);
                    })
                .toList();
        Vec3 base = primary.isEmpty() ? k.forward().scale(baseVelocity.length()) : baseVelocity;
        TrajectoryMotion main =
            primary.isEmpty()
                ? TrajectoryMotion.ofPosition(
                    k.position().add(base.scale(deltaTicks)), base, base.length())
                : evaluatePrimary(
                    bullet, primary, runtime, anchor, base, baseProgress, deltaTicks, modules, k);
        return TrajectoryOffsetEvaluator.compose(
            bullet,
            offsets,
            runtime,
            k,
            main,
            !primary.isEmpty(),
            baseVelocity.length(),
            deltaTicks,
            modules);
    }

    private static TrajectoryMotion evaluatePrimary(
        IBulletObject bullet,
        List<TrajectorySpec> specs,
        Map<ResourceLocation, TrajectoryState> states,
        Vec3 anchor,
        Vec3 baseVelocity,
        double baseProgress,
        double deltaTicks,
        java.util.function.Function<ResourceLocation, TrajectoryModule<?>> modules,
        TrajectoryKinematics kinematics) {
        Map<ResourceLocation, TrajectoryState> runtimeStates = states;
        var active = new java.util.ArrayList<>(specs);
        active.sort(
            java.util.Comparator.comparingInt(
                    (TrajectorySpec spec) -> {
                        TrajectoryModule<?> module = modules.apply(spec.id());
                        return module == null ? Integer.MIN_VALUE : module.priority();
                    })
                .reversed()
                .thenComparing(spec -> spec.id().toString()));
        int blockingPriority = Integer.MAX_VALUE;
        for (TrajectorySpec spec : active) {
            TrajectoryModule<?> module = modules.apply(spec.id());
            if (module == null || !module.appliesTo(bullet)) continue;
            TrajectoryState state = runtimeStates.computeIfAbsent(
                spec.id(), id -> module.createState());
            if (module.blocksFollowingPrimary(state, spec.amplifier())) {
                blockingPriority = Math.min(blockingPriority, module.priority());
            }
        }
        final int finalBlockingPriority = blockingPriority;
        if (finalBlockingPriority != Integer.MAX_VALUE) {
            active.removeIf(
                spec -> {
                    TrajectoryModule<?> module = modules.apply(spec.id());
                    return module != null
                        && module.role() == TrajectoryModule.Role.PRIMARY
                        && module.priority() < finalBlockingPriority;
                });
        }
        Vec3 position =
            kinematics != null
                ? kinematics.position()
                : finite(bullet.getPosition()) ? bullet.getPosition() : Vec3.ZERO;
        Vec3 velocity =
            kinematics != null
                ? kinematics.velocity()
                : finite(bullet.getVelocity()) ? bullet.getVelocity() : Vec3.ZERO;
        baseVelocity = finite(baseVelocity) ? baseVelocity : velocity;
        Vec3 acceleration =
            kinematics != null
                ? kinematics.acceleration()
                : finite(bullet.getAcceleration()) ? bullet.getAcceleration() : Vec3.ZERO;
        double primaryClock =
            kinematics == null ? bullet.getTrajectoryRuntime().distance() : kinematics.clock();
        // The evaluator's base velocity is the actual per-step displacement
        // vector.  LaserProjectile exposes only a unit direction through
        // getVelocity(), so using that value here would move every segment by
        // one full block while its traveled counter advances by the configured
        // (usually smaller) segment step.
        Vec3 desiredPosition = position.add(baseVelocity.scale(deltaTicks));
        Vec3 framePosition = position;
        Vec3 carriedResidual =
            finite(bullet.getTrajectoryRuntime().compositionOffset())
                ? bullet.getTrajectoryRuntime().compositionOffset()
                : Vec3.ZERO;
        Vec3 relativeResidual = carriedResidual;
        Vec3 relativeVelocityResidual =
            finite(bullet.getTrajectoryRuntime().compositionVelocityOffset())
                ? bullet.getTrajectoryRuntime().compositionVelocityOffset()
                : Vec3.ZERO;
        // Raw velocity from the latest absolute controller, before the carried
        // relative velocity is added. Relative controllers use this value to
        // isolate their own contribution from the already-computed path.
        Vec3 absoluteVelocity = baseVelocity;
        boolean hasController = false;
        boolean hasAbsoluteController = false;
        double rangeCost = Double.NaN;
        boolean gravityApplied = false;
        List<Vec3> sampledPath = List.of();
        List<Double> sampledCosts = List.of();
        Vec3 sampledEndpoint = null;
        double progressRate =
            Double.isFinite(baseProgress) && baseProgress > 0.0D ? baseProgress : baseVelocity.length();
        if (active.isEmpty()) {
            return TrajectoryMotion.ofPosition(desiredPosition, velocity, progressRate);
        }
        TrajectoryFrame frame =
            new TrajectoryFrame(
                bullet,
                bullet.getTrajectoryRuntime().origin(),
                bullet.getTrajectoryRuntime().launchDirection());
        for (TrajectorySpec spec : active) {
            TrajectoryModule<?> trajectory = modules.apply(spec.id());
            if (trajectory == null) {
                if (WARNED_MISSING.add(spec.id())) {
                    IsaacDisaster.LOGGER.warn("Ignoring unknown trajectory {}", spec.id());
                }
                continue;
            }
            if (!trajectory.appliesTo(bullet)) continue;
            // Each ordered controller consumes the previous controller's absolute
            // intent, allowing a later trajectory to refine rather than overwrite
            // a stale position snapshot.
            TrajectoryState state = runtimeStates.computeIfAbsent(
                spec.id(), ignored -> trajectory.createState());
            TrajectoryContext context =
                new TrajectoryContext(
                    bullet,
                    new TrajectoryContext.Input(
                        framePosition,
                        velocity,
                        baseVelocity,
                        acceleration,
                        deltaTicks),
                    anchor,
                    spec.amplifier(),
                    state,
                    frame,
                    hasController);
            TrajectoryMotion result;
            try {
                result = trajectory.apply(context);
            } catch (RuntimeException error) {
                IsaacDisaster.LOGGER.warn("Trajectory {} failed; keeping base motion", spec.id(), error);
                continue;
            }
            if (result == null) continue;
            if (!result.path().isEmpty()) {
                sampledPath = result.path();
                sampledCosts = result.pathCosts();
                sampledEndpoint = result.desiredPosition();
            }
            if (Double.isFinite(result.rangeCost())) {
                if (trajectory.rangeCostPolicy() == TrajectoryModule.RangeCostPolicy.REPLACE) {
                    rangeCost = result.rangeCost();
                    gravityApplied = true;
                } else
                    rangeCost =
                        Double.isFinite(rangeCost)
                            ? Math.min(rangeCost, result.rangeCost())
                            : result.rangeCost();
            }
            Vec3 inputPosition = framePosition;
            if (result.desiredPosition() != null) {
                Vec3 resultPosition = result.desiredPosition();
                if (result.compositionMode() == TrajectoryMotion.CompositionMode.PRIMARY) {
                    resultPosition =
                        resultPosition.add(
                            hasController
                                ? framePosition.subtract(position.add(baseVelocity.scale(deltaTicks)))
                                : carriedResidual);
                    hasAbsoluteController = true;
                } else if (result.compositionMode() == TrajectoryMotion.CompositionMode.ABSOLUTE) {
                    double nextDistance = primaryClock + baseVelocity.length() * Math.max(0.0D, deltaTicks);
                    Vec3 compositionOffset =
                        !hasAbsoluteController && !hasController
                            ? carriedResidual
                            : framePosition.subtract(frame.baseline(nextDistance));
                    resultPosition = resultPosition.add(compositionOffset);
                    hasAbsoluteController = true;
                } else if (hasController) {
                    // Relative controllers are the only contribution that cannot
                    // be reconstructed from the immutable launch frame next tick.
                    // Their result includes the absolute controller's current
                    // step and the incoming relative state. Remove that raw
                    // absolute step, then carry the new relative displacement.
                    double dt = Math.max(0.0D, deltaTicks);
                    Vec3 previousStepVelocity = hasAbsoluteController ? absoluteVelocity : velocity;
                    if (hasAbsoluteController)
                        relativeResidual =
                            relativeResidual.add(
                                resultPosition
                                    .subtract(inputPosition)
                                    .subtract(previousStepVelocity.scale(dt)));
                    // Convert the relative controller's complete next-step
                    // result back to the absolute endpoint. The input frame
                    // already contains the previous absolute step, so keeping
                    // the raw absolute step here would advance it twice.
                    resultPosition = resultPosition.subtract(previousStepVelocity.scale(dt));
                }
                desiredPosition = resultPosition;
                framePosition = resultPosition;
            }
            if (result.desiredVelocity() != null) {
                Vec3 resultVelocity = result.desiredVelocity();
                if (result.compositionMode() != TrajectoryMotion.CompositionMode.RELATIVE) {
                    absoluteVelocity = resultVelocity;
                    resultVelocity = resultVelocity.add(relativeVelocityResidual);
                } else {
                    // The relative result contains the complete relative
                    // velocity after this controller. Keep it relative to the
                    // raw absolute path so the next tick can reapply it once.
                    relativeVelocityResidual = resultVelocity.subtract(absoluteVelocity);
                }
                velocity = finite(resultVelocity) ? resultVelocity : velocity;
            }
            acceleration = result.desiredAcceleration();
            if (result.progressRate() > 0.0D) progressRate = result.progressRate();
            hasController = true;
        }
        bullet
            .getTrajectoryRuntime()
            .compositionOffset(hasAbsoluteController ? relativeResidual : Vec3.ZERO);
        bullet
            .getTrajectoryRuntime()
            .compositionVelocityOffset(hasAbsoluteController ? relativeVelocityResidual : Vec3.ZERO);
        if (gravityApplied) {
            double remaining = Math.max(0, bullet.getRange() - bullet.getTraveled());
            if (rangeCost > remaining) {
                desiredPosition = position.lerp(desiredPosition, remaining / rangeCost);
                rangeCost = remaining;
            }
        }
        if (sampledEndpoint != null) {
            Vec3 correction = desiredPosition.subtract(sampledEndpoint);
            java.util.ArrayList<Vec3> combined = new java.util.ArrayList<>();
            for (int i = 0; i < sampledPath.size(); i++) {
                combined.add(sampledPath.get(i).add(correction.scale((i + 1.0) / sampledPath.size())));
            }
            sampledPath = combined;
        }
        return new TrajectoryMotion(
            desiredPosition,
            velocity,
            acceleration,
            progressRate,
            TrajectoryMotion.CompositionMode.ABSOLUTE,
            rangeCost,
            sampledPath,
            gravityApplied ? List.of() : sampledCosts);
    }

    private static boolean finite(Vec3 value) {
        return value != null
            && Double.isFinite(value.x)
            && Double.isFinite(value.y)
            && Double.isFinite(value.z);
    }
}
