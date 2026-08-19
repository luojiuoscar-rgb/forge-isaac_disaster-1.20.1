package net.luojiuoscar.isaac_disaster.effect.custom;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.RefreshScaleS2CPacket;
import net.luojiuoscar.isaac_disaster.system.ScaleUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class InnerChildEffect extends MobEffect {
    private static final double SCALE_UNITS_PER_LEVEL = -5.0D;
    private static final double SPEED_UNITS_PER_LEVEL = 1.0D;
    private static final double BLOCK_BREAKING_UNITS_PER_LEVEL = 1.0D;

    private static final UUID INNER_CHILD_SCALE_UUID =
            UUID.nameUUIDFromBytes((IsaacDisaster.MOD_ID + ":inner_child_scale").getBytes(StandardCharsets.UTF_8));
    private static final UUID INNER_CHILD_SPEED_UUID =
            UUID.nameUUIDFromBytes((IsaacDisaster.MOD_ID + ":inner_child_speed").getBytes(StandardCharsets.UTF_8));
    private static final UUID INNER_CHILD_BLOCK_BREAKING_UUID =
            UUID.nameUUIDFromBytes((IsaacDisaster.MOD_ID + ":inner_child_block_breaking").getBytes(StandardCharsets.UTF_8));

    public InnerChildEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        if (!(entity instanceof Player player)) {
            entity.removeEffect(this);
            return;
        }

        super.addAttributeModifiers(entity, attributeMap, amplifier);

        int level = amplifier + 1;

        AttributeInstance scale = player.getAttribute(ModAttributes.SCALE.get());
        if (scale != null) {
            scale.removeModifier(INNER_CHILD_SCALE_UUID);
            scale.addTransientModifier(new AttributeModifier(
                    INNER_CHILD_SCALE_UUID,
                    "inner_child.scale",
                    StatManager.SCALE.getBonus() * SCALE_UNITS_PER_LEVEL * level,
                    AttributeModifier.Operation.ADDITION
            ));
        }

        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(INNER_CHILD_SPEED_UUID);
            speed.addTransientModifier(new AttributeModifier(
                    INNER_CHILD_SPEED_UUID,
                    "inner_child.speed",
                    StatManager.MOVEMENT_SPEED.getBonus() * SPEED_UNITS_PER_LEVEL * level,
                    AttributeModifier.Operation.ADDITION
            ));
        }

        AttributeInstance blockBreaking = player.getAttribute(ModAttributes.BLOCK_BREAKING_SPEED.get());
        if (blockBreaking != null) {
            blockBreaking.removeModifier(INNER_CHILD_BLOCK_BREAKING_UUID);
            blockBreaking.addTransientModifier(new AttributeModifier(
                    INNER_CHILD_BLOCK_BREAKING_UUID,
                    "inner_child.block_breaking_speed",
                    StatManager.BLOCK_BREAKING.getBonus() * BLOCK_BREAKING_UNITS_PER_LEVEL * level,
                    AttributeModifier.Operation.ADDITION
            ));
        }

        ScaleUtils.refreshScale(player);
        if (player instanceof ServerPlayer serverPlayer) {
            ModMessages.sentToPlayer(new RefreshScaleS2CPacket(), serverPlayer);
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);

        AttributeInstance scale = entity.getAttribute(ModAttributes.SCALE.get());
        if (scale != null) {
            scale.removeModifier(INNER_CHILD_SCALE_UUID);
        }

        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(INNER_CHILD_SPEED_UUID);
        }

        AttributeInstance blockBreaking = entity.getAttribute(ModAttributes.BLOCK_BREAKING_SPEED.get());
        if (blockBreaking != null) {
            blockBreaking.removeModifier(INNER_CHILD_BLOCK_BREAKING_UUID);
        }

        ScaleUtils.refreshScale(entity);
        if (entity instanceof ServerPlayer serverPlayer) {
            ModMessages.sentToPlayer(new RefreshScaleS2CPacket(), serverPlayer);
        }
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }
}
