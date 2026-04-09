package net.luojiuoscar.isaac_disaster.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IExecutableEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.PillEffect;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class TriggerPillEffectCommand {

    public TriggerPillEffectCommand(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
                Commands.literal("isd")
                        .then(
                                Commands.literal("pill")
                                        .then(
                                                Commands.literal("use")
                                                        .then(
                                                                Commands.argument("id", ResourceLocationArgument.id())
                                                                        // ==== TAB 补全 ====
                                                                        .suggests((ctx, builder) -> {
                                                                            List<String> ids = ModExecutableEffects.EXECUTABLE_EFFECT_REGISTRY.getEntries()
                                                                                    .stream()
                                                                                    .map(RegistryObject::getId)
                                                                                    .map(ResourceLocation::toString)
                                                                                    // 只保留 PillEffect 类型
                                                                                    .filter(idStr -> {
                                                                                        RegistryObject<?> ro = ModExecutableEffects.EXECUTABLE_EFFECT_REGISTRY.getEntries()
                                                                                                .stream()
                                                                                                .filter(r -> r.getId().toString().equals(idStr))
                                                                                                .findFirst()
                                                                                                .orElse(null);
                                                                                        return ro != null && ro.get() instanceof PillEffect;
                                                                                    })
                                                                                    .toList();
                                                                            return SharedSuggestionProvider.suggest(ids, builder);
                                                                        })
                                                                        // 默认 isHorse = false
                                                                        .executes(ctx -> {
                                                                            ResourceLocation id = ResourceLocationArgument.getId(ctx, "id");
                                                                            return execute(ctx.getSource(), id, false);
                                                                        })
                                                                        // 可选 isHorse 参数
                                                                        .then(
                                                                                Commands.argument("isHorse", BoolArgumentType.bool())
                                                                                        .executes(ctx -> {
                                                                                            ResourceLocation id = ResourceLocationArgument.getId(ctx, "id");
                                                                                            boolean isHorse = BoolArgumentType.getBool(ctx, "isHorse");
                                                                                            return execute(ctx.getSource(), id, isHorse);
                                                                                        })
                                                                        )
                                                        )
                                        )
                        )
        );
    }

    /** 指令执行逻辑 */
    private int execute(CommandSourceStack source, ResourceLocation id, boolean isHorse) {

        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("§c该命令必须由玩家执行"));
            return 0;
        }

        // === 从 Registry 中查找对应的 PillEffect ===
        RegistryObject<IExecutableEffect> obj = ModExecutableEffects.EXECUTABLE_EFFECT_REGISTRY.getEntries()
                .stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (obj == null || !(obj.get() instanceof PillEffect effect)) {
            source.sendFailure(Component.literal("§c找不到药丸效果: §e" + id));
            return 0;
        }

        // 输出信息
        source.sendSuccess(() -> Component.literal(
                "§a读取成功: §e" + id + " §7| 大药丸=" + isHorse), false);

        // 执行 PillEffect
        ExecutableEffectContext context = new ExecutableEffectContext(player);
        context.set(ContextKeys.BOOLEAN, List.of(isHorse));
        effect.apply(context);

        return 1;
    }
}