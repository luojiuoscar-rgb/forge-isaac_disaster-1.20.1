package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.trajectory.ModAttackTrajectory;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class MyReflection extends PassiveAbility {
    private static final UUID MY_REFLECTION_BULLET_SPEED =
            UUID.nameUUIDFromBytes(("isaac_disaster:my_reflection_bullet_speed").getBytes(StandardCharsets.UTF_8));
    private static final UUID MY_REFLECTION_RANGE =
            UUID.nameUUIDFromBytes(("isaac_disaster:my_reflection_range").getBytes(StandardCharsets.UTF_8));

    public MyReflection(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.RANGE.apply(player, 1.5);
        StatManager.DAMAGE.apply(player, 1.5);
        StatManager.LUCK.apply(player, -1);
        StatManager.addTrajectory(player, ModAttackTrajectory.MY_REFLECTION.getId(), 1);

        AttributeInstance range = player.getAttribute(ModAttributes.BULLET_RANGE.get());
        if (range != null && range.getModifier(MY_REFLECTION_RANGE) == null){
            range.addPermanentModifier(new AttributeModifier(
                    MY_REFLECTION_RANGE,
                    "",
                    1,
                    AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }

        AttributeInstance bullet_speed = player.getAttribute(ModAttributes.BULLET_SPEED.get());
        if (bullet_speed != null && bullet_speed.getModifier(MY_REFLECTION_BULLET_SPEED) == null){
            bullet_speed.addPermanentModifier(new AttributeModifier(
                    MY_REFLECTION_BULLET_SPEED,
                    "",
                    0.6,
                    AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }

    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        if (!PlayerHelper.hasSet(ItemId.MY_REFLECTION.getId(), (ServerPlayer) player)){
            AttributeInstance range = player.getAttribute(ModAttributes.BULLET_RANGE.get());
            AttributeInstance bullet_speed = player.getAttribute(ModAttributes.BULLET_SPEED.get());
            if (range != null) range.removeModifier(MY_REFLECTION_RANGE);
            if (bullet_speed != null) bullet_speed.removeModifier(MY_REFLECTION_BULLET_SPEED);
        }

        StatManager.RANGE.apply(player, -1.5);
        StatManager.DAMAGE.apply(player, -1.5);
        StatManager.LUCK.apply(player, 1);
        StatManager.addTrajectory(player, ModAttackTrajectory.MY_REFLECTION.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                Component.translatable("item.isaac_disaster.my_reflection.lore.1"),
                Component.translatable("item.isaac_disaster.my_reflection.lore.2"),
                StatManager.RANGE.description(1.5),
                StatManager.DAMAGE.description(1.5),
                StatManager.LUCK.description(-1)
        );
    }

}
