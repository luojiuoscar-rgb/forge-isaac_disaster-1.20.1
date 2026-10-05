package net.luojiuoscar.isaac_disaster.commands.bullet;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.debug.TrajectoryTelemetry;
import net.luojiuoscar.isaac_disaster.bullet.server.BulletRuntime;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.BrimstoneAttack;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.LaserAttack;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.trajectory.ModTrajectoryModules;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Operator-only command used to produce long, deterministic trajectory samples. */
public final class BulletTrajectoryTraceCmd {
    public BulletTrajectoryTraceCmd(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> start = Commands.literal("start")
                                                .then(Commands.argument("target", EntityArgument.entity())
                        // A quoted comma-separated string is accepted so runtime
                        // validation can exercise the ordered evaluator chain.
                        .then(Commands.argument("trajectory", StringArgumentType.string())
                                .then(Commands.argument("kind", StringArgumentType.word())
                                        .then(Commands.argument("amplifier", IntegerArgumentType.integer(0, 64))
                                                .then(Commands.argument("speed", DoubleArgumentType.doubleArg(0.1, 8.0))
                                                        .then(Commands.argument("range", DoubleArgumentType.doubleArg(1.0, 64.0))
                                                                .then(Commands.argument("samples", IntegerArgumentType.integer(1, 100000))
                                                                        .executes(this::start))))))));
        LiteralArgumentBuilder<CommandSourceStack> stop = Commands.literal("stop")
                .executes(context -> {
                    TrajectoryTelemetry.stop();
                    context.getSource().sendSuccess(() -> Component.literal("Trajectory telemetry stopped"), false);
                    return 1;
                });
        dispatcher.register(Commands.literal("isd")
                .then(Commands.literal("bullet").requires(source -> source.hasPermission(2))
                        .then(Commands.literal("trace").then(start).then(stop))));
    }

    private int start(CommandContext<CommandSourceStack> command) {
        LivingEntity player;
        try {
            Entity entity = EntityArgument.getEntity(command, "target");
            if (!(entity instanceof LivingEntity living)) throw new IllegalArgumentException("target is not living");
            player = living;
        } catch (Exception error) {
            command.getSource().sendFailure(Component.literal("A living entity target is required"));
            return 0;
        }
        if (!(player.level() instanceof ServerLevel level)) return 0;
        String trajectoryArgument = StringArgumentType.getString(command, "trajectory");
        String kind = StringArgumentType.getString(command, "kind").toLowerCase(java.util.Locale.ROOT);
        int amplifier = IntegerArgumentType.getInteger(command, "amplifier");
        double speed = DoubleArgumentType.getDouble(command, "speed");
        double range = DoubleArgumentType.getDouble(command, "range");
        int samples = IntegerArgumentType.getInteger(command, "samples");
        IForgeRegistry<?> registry = RegistryManager.ACTIVE.getRegistry(ModTrajectoryModules.TRAJECTORY_MODULE_KEY);
        Map<ResourceLocation, Integer> trajectoryConfig = new LinkedHashMap<>();
        for (String rawId : trajectoryArgument.split("[+,]")) {
            ResourceLocation trajectory = ResourceLocation.tryParse(rawId.trim());
            if (trajectory == null || registry == null || registry.getValue(trajectory) == null) {
                command.getSource().sendFailure(Component.literal("Unknown trajectory: " + rawId));
                return 0;
            }
            trajectoryConfig.put(trajectory, amplifier + 1);
        }
        if (trajectoryConfig.isEmpty()) {
            command.getSource().sendFailure(Component.literal("At least one trajectory is required"));
            return 0;
        }
        if (!(kind.equals("tear") || kind.equals("laser") || kind.equals("brimstone") || kind.equals("fetus"))) {
            command.getSource().sendFailure(Component.literal("kind must be tear, fetus, laser, or brimstone"));
            return 0;
        }
        try {
            java.nio.file.Path file = TrajectoryTelemetry.start(kind + "-" + trajectoryArgument, samples);
            var attackType = switch (kind) {
                case "fetus" -> ModAttackTypes.C_SECTION.get();
                case "brimstone" -> ModAttackTypes.BRIMSTONE.get();
                case "laser" -> ModAttackTypes.LASER.get();
                default -> ModAttackTypes.BULLET.get();
            };
            AttackContext context = AttackContext.builder(player, player).attackType(attackType)
                    .position(player.getEyePosition().add(player.getLookAngle().scale(0.75D)))
                    .direction(player.getLookAngle()).damage(1.0D).speed(speed).range(range)
                    .color(ModBulletColors.BASE.getId())
                    .useFixedLaunchTransform().build();
            trajectoryConfig.forEach(context::addTrajectoryModule);
            context.freeze();
            if (kind.equals("tear") || kind.equals("fetus")) {
                BulletState.Builder builder = BulletState.from(context).lifetime(200).spectral(true).piercing(true);
                BulletRuntime.INSTANCE.spawn(level, builder.build());
            } else if (kind.equals("brimstone")) {
                ((BrimstoneAttack) attackType).shootSingle(context, 1);
            } else {
                attackType.shoot(context);
            }
            command.getSource().sendSuccess(() -> Component.literal("Trajectory trace started: " + file), true);
            return 1;
        } catch (IOException | RuntimeException error) {
            TrajectoryTelemetry.stop();
            command.getSource().sendFailure(Component.literal("Could not start trajectory trace: " + error.getMessage()));
            return 0;
        }
    }
}
