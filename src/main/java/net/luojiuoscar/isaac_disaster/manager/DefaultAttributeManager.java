package net.luojiuoscar.isaac_disaster.manager;

import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.function.DoubleSupplier;

/**
 * Applies configured player defaults as dedicated additive modifiers.
 * Stable UUIDs make repeated application replace, rather than stack, the old values.
 */
public final class DefaultAttributeManager {
    private static final int ADDITION_OPERATION = 0;

    private static final List<DefaultAttributeSpec> SPECS = List.of(
            new DefaultAttributeSpec(
                    "default_max_health",
                    Attributes.MAX_HEALTH,
                    20.0,
                    () -> Config.DEFAULT_MAX_HEALTH.get()
            ),
            new DefaultAttributeSpec(
                    "default_movement_speed",
                    Attributes.MOVEMENT_SPEED,
                    0.1,
                    () -> Config.DEFAULT_MOVEMENT_SPEED.get()
            ),
            new DefaultAttributeSpec(
                    "default_attack_damage",
                    Attributes.ATTACK_DAMAGE,
                    1.0,
                    () -> Config.DEFAULT_ATTACK_DAMAGE.get()
            ),
            new DefaultAttributeSpec(
                    "default_luck",
                    Attributes.LUCK,
                    0.0,
                    () -> Config.DEFAULT_LUCK.get()
            ),
            new DefaultAttributeSpec(
                    "default_scale",
                    ModAttributes.SCALE.get(),
                    1.0,
                    () -> Config.DEFAULT_SCALE.get()
            ),
            new DefaultAttributeSpec(
                    "default_bullet_range",
                    ModAttributes.BULLET_RANGE.get(),
                    18.0,
                    () -> Config.DEFAULT_BULLET_RANGE.get()
            ),
            new DefaultAttributeSpec(
                    "default_entity_reach",
                    ForgeMod.ENTITY_REACH.get(),
                    3.0,
                    () -> Config.DEFAULT_ENTITY_REACH.get()
            ),
            new DefaultAttributeSpec(
                    "default_block_reach",
                    ForgeMod.BLOCK_REACH.get(),
                    5.0,
                    () -> Config.DEFAULT_BLOCK_REACH.get()
            ),
            new DefaultAttributeSpec(
                    "default_tears",
                    ModAttributes.TEARS.get(),
                    0.0,
                    () -> Config.DEFAULT_TEARS.get()
            ),
            new DefaultAttributeSpec(
                    "default_tears_correction",
                    ModAttributes.TEARS_CORRECTION.get(),
                    0.0,
                    () -> Config.DEFAULT_TEARS_CORRECTION.get()
            ),
            new DefaultAttributeSpec(
                    "default_bullet_speed",
                    ModAttributes.BULLET_SPEED.get(),
                    1.0,
                    () -> Config.DEFAULT_BULLET_SPEED.get()
            ),
            new DefaultAttributeSpec(
                    "default_attack_speed",
                    Attributes.ATTACK_SPEED,
                    4.0,
                    () -> Config.DEFAULT_ATTACK_SPEED.get()
            ),
            new DefaultAttributeSpec(
                    "default_block_breaking_speed",
                    ModAttributes.BLOCK_BREAKING_SPEED.get(),
                    0.0,
                    () -> Config.DEFAULT_BLOCK_BREAKING_SPEED.get()
            ),
            new DefaultAttributeSpec(
                    "default_attack_knockback",
                    Attributes.ATTACK_KNOCKBACK,
                    0.0,
                    () -> Config.DEFAULT_ATTACK_KNOCKBACK.get()
            ),
            new DefaultAttributeSpec(
                    "default_bullet_scale",
                    ModAttributes.BULLET_SCALE.get(),
                    0.0,
                    () -> Config.DEFAULT_BULLET_SCALE.get()
            ),
            new DefaultAttributeSpec(
                    "default_bullet_count",
                    ModAttributes.BULLET_COUNT.get(),
                    1.0,
                    () -> Config.DEFAULT_BULLET_COUNT.get()
            ),
            new DefaultAttributeSpec(
                    "default_pill_quality",
                    ModAttributes.PILL_QUALITY.get(),
                    0.0,
                    () -> Config.DEFAULT_PILL_QUALITY.get()
            ),
            new DefaultAttributeSpec(
                    "default_fly_time",
                    ModAttributes.FLY_TIME.get(),
                    0.0,
                    () -> Config.DEFAULT_FLY_TIME.get()
            )
    );

    private DefaultAttributeManager() {
    }

    public static void apply(ServerPlayer player) {
        for (DefaultAttributeSpec spec : SPECS) {
            double configuredValue = spec.configuredValue().getAsDouble();
            double modifierAmount = configuredValue - spec.vanillaBaseValue();

            StatManager.setModifier(
                    player,
                    spec.uuid(),
                    spec.attribute(),
                    modifierAmount,
                    null,
                    null,
                    ADDITION_OPERATION
            );
        }

        player.setHealth(Math.min(player.getHealth(), player.getMaxHealth()));
    }

    public static void remove(ServerPlayer player) {
        for (DefaultAttributeSpec spec : SPECS) {
            StatManager.removeModifier(player, player.getAttribute(spec.attribute()), spec.uuid());
        }
    }

    private record DefaultAttributeSpec(
            UUID uuid,
            Attribute attribute,
            double vanillaBaseValue,
            DoubleSupplier configuredValue
    ) {
        private DefaultAttributeSpec(
                String id,
                Attribute attribute,
                double vanillaBaseValue,
                DoubleSupplier configuredValue
        ) {
            this(
                    UUID.nameUUIDFromBytes(("isaac_disaster:default_attribute/" + id)
                            .getBytes(StandardCharsets.UTF_8)),
                    attribute,
                    vanillaBaseValue,
                    configuredValue
            );
        }
    }
}
