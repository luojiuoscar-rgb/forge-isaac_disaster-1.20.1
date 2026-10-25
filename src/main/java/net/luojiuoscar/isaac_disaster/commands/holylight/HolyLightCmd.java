package net.luojiuoscar.isaac_disaster.commands.holylight;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.HolyLight;
import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.core.HolyLightTimeline;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

/** Operator diagnostics use the same public entry point as future item and mob integrations. */
public final class HolyLightCmd {
    public HolyLightCmd(CommandDispatcher<CommandSourceStack> dispatcher) {
        var skip = Commands.argument("skipWarning", BoolArgumentType.bool())
                .executes(context -> spawn(context, radius(context), skipWarning(context), 1))
                .then(Commands.argument("warningSpeed", DoubleArgumentType.doubleArg())
                        .executes(context -> spawn(context, radius(context), skipWarning(context),
                                DoubleArgumentType.getDouble(context, "warningSpeed"))));
        var radius = Commands.argument("radius", DoubleArgumentType.doubleArg())
                .executes(context -> spawn(context, radius(context), false, 1)).then(skip);
        var damage = Commands.argument("damage", FloatArgumentType.floatArg())
                .executes(context -> spawn(context, HolyLight.DEFAULT_RADIUS, false, 1)).then(radius);
        var position = Commands.argument("position", Vec3Argument.vec3()).then(damage);
        dispatcher.register(Commands.literal("isd").then(Commands.literal("holylight")
                .requires(source -> source.hasPermission(2)).then(position)));
    }

    private static double radius(CommandContext<CommandSourceStack> context) {
        return DoubleArgumentType.getDouble(context, "radius");
    }

    private static boolean skipWarning(CommandContext<CommandSourceStack> context) {
        return BoolArgumentType.getBool(context, "skipWarning");
    }

    private static int spawn(CommandContext<CommandSourceStack> context, double radius,
                             boolean skipWarning, double warningSpeed) {
        var source = context.getSource();
        if (!(source.getEntity() instanceof LivingEntity caster)) {
            source.sendFailure(Component.literal("Run as a living entity (for example: execute as @p at @s run isd holylight ...)."));
            return 0;
        }
        boolean created = HolyLight.strike(source.getLevel(), caster,
                Vec3Argument.getVec3(context, "position"), FloatArgumentType.getFloat(context, "damage"),
                radius, skipWarning, warningSpeed);
        if (!created) source.sendFailure(Component.literal("No holy light created: check position, loaded chunks, radius and damage."));
        else {
            int warningTicks = HolyLightTimeline.warningTicks(warningSpeed, skipWarning);
            source.sendSuccess(() -> Component.literal("Holy light created"
                    + (skipWarning ? " (immediate)." : " (" + warningTicks + " tick warning).")), false);
        }
        return created ? 1 : 0;
    }
}
