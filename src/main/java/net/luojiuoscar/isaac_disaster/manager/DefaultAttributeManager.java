package net.luojiuoscar.isaac_disaster.manager;

import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
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
                    () -> Config.DEFAULT_MAX_HEALTH.get()
            ),
            new DefaultAttributeSpec(
                    "default_movement_speed",
                    Attributes.MOVEMENT_SPEED,
                    () -> Config.DEFAULT_MOVEMENT_SPEED.get()
            ),
            new DefaultAttributeSpec(
                    "default_attack_damage",
                    Attributes.ATTACK_DAMAGE,
                    () -> Config.DEFAULT_ATTACK_DAMAGE.get()
            ),
            new DefaultAttributeSpec(
                    "default_luck",
                    Attributes.LUCK,
                    () -> Config.DEFAULT_LUCK.get()
            ),
            new DefaultAttributeSpec(
                    "default_scale",
                    ModAttributes.SCALE.get(),
                    () -> Config.DEFAULT_SCALE.get()
            ),
            new DefaultAttributeSpec(
                    "default_bullet_range",
                    ModAttributes.BULLET_RANGE.get(),
                    () -> Config.DEFAULT_BULLET_RANGE.get()
            ),
            new DefaultAttributeSpec(
                    "default_entity_reach",
                    ForgeMod.ENTITY_REACH.get(),
                    () -> Config.DEFAULT_ENTITY_REACH.get()
            ),
            new DefaultAttributeSpec(
                    "default_block_reach",
                    ForgeMod.BLOCK_REACH.get(),
                    () -> Config.DEFAULT_BLOCK_REACH.get()
            ),
            new DefaultAttributeSpec(
                    "default_tears",
                    ModAttributes.TEARS.get(),
                    () -> Config.DEFAULT_TEARS.get()
            ),
            new DefaultAttributeSpec(
                    "default_fire_rate_addition",
                    ModAttributes.FIRE_RATE.get(),
                    () -> Config.DEFAULT_FIRE_RATE_ADDITION.get()
            ),
            new DefaultAttributeSpec(
                    "default_bullet_speed",
                    ModAttributes.BULLET_SPEED.get(),
                    () -> Config.DEFAULT_BULLET_SPEED.get()
            ),
            new DefaultAttributeSpec(
                    "default_attack_speed",
                    Attributes.ATTACK_SPEED,
                    () -> Config.DEFAULT_ATTACK_SPEED.get()
            ),
            new DefaultAttributeSpec(
                    "default_block_breaking_speed",
                    ModAttributes.BLOCK_BREAKING_SPEED.get(),
                    () -> Config.DEFAULT_BLOCK_BREAKING_SPEED.get()
            ),
            new DefaultAttributeSpec(
                    "default_attack_knockback",
                    Attributes.ATTACK_KNOCKBACK,
                    () -> Config.DEFAULT_ATTACK_KNOCKBACK.get()
            ),
            new DefaultAttributeSpec(
                    "default_bullet_scale",
                    ModAttributes.BULLET_SCALE.get(),
                    () -> Config.DEFAULT_BULLET_SCALE.get()
            ),
            new DefaultAttributeSpec(
                    "default_bullet_count",
                    ModAttributes.BULLET_COUNT.get(),
                    () -> Config.DEFAULT_BULLET_COUNT.get()
            ),
            new DefaultAttributeSpec(
                    "default_pill_quality",
                    ModAttributes.PILL_QUALITY.get(),
                    () -> Config.DEFAULT_PILL_QUALITY.get()
            ),
            new DefaultAttributeSpec(
                    "default_fly_time",
                    ModAttributes.FLY_TIME.get(),
                    () -> Config.DEFAULT_FLY_TIME.get()
            )
    );

    private DefaultAttributeManager() {
    }

    public static void apply(ServerPlayer player) {
        // Use the same supplier as player construction, including player-specific vanilla bases.
        var defaults = DefaultAttributes.getSupplier(EntityType.PLAYER);
        for (DefaultAttributeSpec spec : SPECS) {
            double configuredValue = spec.configuredValue().getAsDouble();
            double modifierAmount = configuredValue - defaults.getBaseValue(spec.attribute());

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
            DoubleSupplier configuredValue
    ) {
        private DefaultAttributeSpec(
                String id,
                Attribute attribute,
                DoubleSupplier configuredValue
        ) {
            this(
                    UUID.nameUUIDFromBytes(("isaac_disaster:default_attribute/" + id)
                            .getBytes(StandardCharsets.UTF_8)),
                    attribute,
                    configuredValue
            );
        }
    }
}
