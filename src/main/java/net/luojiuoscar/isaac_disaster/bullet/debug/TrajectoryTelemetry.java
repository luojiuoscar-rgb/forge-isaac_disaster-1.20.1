package net.luojiuoscar.isaac_disaster.bullet.debug;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.LaserAttack;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryMotion;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectorySpec;
import net.minecraft.world.phys.Vec3;

/**
 * Gated server-side JSONL recorder for long trajectory runs. It is deliberately disabled by default
 * so the normal tick path performs no file I/O.
 */
public final class TrajectoryTelemetry {
    private static final AtomicLong LASER_IDS = new AtomicLong();
    private static final Map<LaserAttack.LaserProjectile, Long> LASER_IDENTITIES =
        new WeakHashMap<>();
    private static BufferedWriter writer;
    private static String runName;
    private static int maxSamples;
    private static int samples;
    private static volatile boolean active;

    private TrajectoryTelemetry() {
    }

    /** Starts a new recorder under the server game directory's logs/trajectory folder. */
    public static synchronized Path start(String requestedName, int sampleLimit) throws IOException {
        stop();
        String safeName =
            sanitize(requestedName == null || requestedName.isBlank() ? "trajectory" : requestedName);
        Path directory = Path.of("logs", "trajectory");
        Files.createDirectories(directory);
        Path file = directory.resolve(safeName + "-" + Instant.now().toEpochMilli() + ".jsonl");
        writer =
            Files.newBufferedWriter(
                file, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        runName = safeName;
        maxSamples = Math.max(1, sampleLimit);
        samples = 0;
        return file.toAbsolutePath();
    }

    /** Stops recording and closes the current file. */
    public static synchronized void stop() {
        if (writer == null) return;
        try {
            writer.flush();
            writer.close();
        } catch (IOException error) {
            IsaacDisaster.LOGGER.warn("Failed closing trajectory telemetry", error);
        } finally {
            writer = null;
            runName = null;
            samples = 0;
        }
    }

    /** Returns whether a telemetry run is currently active. */
    public static boolean enabled() {
        return active;
    }

    /** Records a lightweight bullet tick, including the post-collision position. */
    public static void recordBullet(
        BulletState bullet, Vec3 plannedEnd, TrajectoryMotion motion, boolean steeringActive) {
        if (!enabled() || bullet == null || bullet.getTrajectorySpecs().isEmpty()) return;
        record(
            "bullet",
            bullet,
            bullet.slot() + ":" + bullet.generation(),
            bullet.previousPosition(),
            bullet.position(),
            plannedEnd,
            motion,
            steeringActive,
            bullet.getTrajectoryRuntime().distance(),
            bullet.traveled(),
            bullet.age(),
            bullet.getTrajectoryRuntime().origin(),
            bullet.getTrajectoryRuntime().launchDirection(),
            bullet.getTrajectoryRuntime().anchor(),
            bullet.getTrajectorySpecs());
    }

    /** Records one laser segment after its path and collision handling. */
    public static void recordLaser(
        LaserAttack.LaserProjectile laser,
        Vec3 plannedEnd,
        Vec3 start,
        TrajectoryMotion motion,
        boolean steeringActive) {
        if (!enabled() || laser == null || laser.getTrajectorySpecs().isEmpty()) return;
        record(
            "laser",
            laser,
            "laser-" + laserIdentity(laser),
            start,
            laser.getPosition(),
            plannedEnd,
            motion,
            steeringActive,
            laser.getTrajectoryRuntime().distance(),
            laser.getTraveled(),
            laser.getTrajectoryAge(),
            laser.getTrajectoryRuntime().origin(),
            laser.getTrajectoryRuntime().launchDirection(),
            laser.getTrajectoryRuntime().anchor(),
            laser.getTrajectorySpecs());
    }

    private static synchronized long laserIdentity(LaserAttack.LaserProjectile laser) {
        return LASER_IDENTITIES.computeIfAbsent(laser, ignored -> LASER_IDS.incrementAndGet());
    }

    private static synchronized void record(
        String kind,
        IBulletObject bullet,
        String identity,
        Vec3 start,
        Vec3 actualPosition,
        Vec3 plannedEnd,
        TrajectoryMotion motion,
        boolean steeringActive,
        double trajectoryDistance,
        double traveled,
        int age,
        Vec3 origin,
        Vec3 mainAxis,
        Vec3 anchor,
        List<TrajectorySpec> specs) {
        if (writer == null || samples >= maxSamples) return;
        try {
            StringBuilder json = new StringBuilder(768);
            json.append('{');
            field(json, "run", runName).append(',');
            field(json, "kind", kind).append(',');
            field(json, "identity", identity).append(',');
            field(json, "sample", samples++).append(',');
            field(json, "age", age).append(',');
            field(json, "steering", steeringActive).append(',');
            field(json, "trajectoryDistance", trajectoryDistance).append(',');
            if (bullet instanceof BulletState state) {
                field(json, "trajectoryInputDistance", state.lastTrajectoryInputDistance()).append(',');
                field(json, "trajectoryEvaluationCount", state.trajectoryEvaluationCount()).append(',');
            }
            rawField(
                    json,
                    "motionProgressRate",
                    motion == null ? null : Double.toString(motion.progressRate()))
                .append(',');
            rawField(
                    json,
                    "estimatedInputDistance",
                    motion == null
                        ? null
                        : Double.toString(Math.max(0.0D, trajectoryDistance - motion.progressRate())))
                .append(',');
            field(json, "traveled", traveled).append(',');
            field(json, "start", vector(start)).append(',');
            rawField(json, "position", vector(actualPosition)).append(',');
            rawField(json, "plannedEnd", vector(plannedEnd)).append(',');
            rawField(json, "velocity", vector(bullet.getVelocity())).append(',');
            rawField(json, "motionVelocity", vector(motion == null ? null : motion.desiredVelocity()))
                .append(',');
            rawField(json, "motionPosition", vector(motion == null ? null : motion.desiredPosition()))
                .append(',');
            rawField(json, "origin", vector(origin)).append(',');
            rawField(json, "mainAxis", vector(mainAxis)).append(',');
            rawField(json, "anchor", vector(anchor)).append(',');
            field(json, "source", bullet.getRootTypeId().toString()).append(',');
            if (bullet.getTrajectoryRuntime().hasKinematics()) {
                var primary = bullet.getTrajectoryRuntime().kinematics();
                rawField(json, "primaryPosition", vector(primary.position())).append(',');
                rawField(json, "primaryVelocity", vector(primary.velocity())).append(',');
                field(json, "primaryDistance", primary.distance()).append(',');
                rawField(json, "trajectoryOffset", vector(primary.offset())).append(',');
            }
            json.append("\"modulePhases\":{");
            boolean firstPhase = true;
            var registry = net.minecraftforge.registries.RegistryManager.ACTIVE.getRegistry(
                net.luojiuoscar.isaac_disaster.registries.trajectory.ModTrajectoryModules.TRAJECTORY_MODULE_KEY);
            for (var entry : bullet.getTrajectoryRuntime().states().entrySet()) {
                if (!firstPhase) json.append(',');
                var module = registry == null ? null : registry.getValue(entry.getKey());
                field(json, entry.getKey().toString(), module == null ? 0 : module.telemetryPhase(entry.getValue()));
                firstPhase = false;
            }
            json.append("},");
            field(json, "trajectories", specs.toString());
            json.append('}').append('\n');
            writer.write(json.toString());
            if (samples % 32 == 0) writer.flush();
        } catch (IOException error) {
            IsaacDisaster.LOGGER.warn("Disabling trajectory telemetry after write failure", error);
            stop();
        }
    }

    private static StringBuilder field(StringBuilder out, String name, Object value) {
        out.append('"').append(name).append("\":");
        if (value instanceof String string) {
            out.append('"').append(escape(string)).append('"');
        } else out.append(value == null ? "null" : value);
        return out;
    }

    private static StringBuilder rawField(StringBuilder out, String name, String value) {
        out.append('\"').append(name).append("\":").append(value == null ? "null" : value);
        return out;
    }

    private static String vector(Vec3 value) {
        if (value == null) return "null";
        return String.format(Locale.ROOT, "[%.12f,%.12f,%.12f]", value.x, value.y, value.z);
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String sanitize(String value) {
        return value.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
