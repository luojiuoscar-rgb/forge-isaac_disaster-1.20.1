package net.luojiuoscar.isaac_disaster.registries.ability.active.impl;

import net.luojiuoscar.isaac_disaster.block.block_entity.misc.ItemDisplayContainerBlockEntity;
import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ActiveAbility;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.data.BlockData;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TheD6 extends ActiveAbility {
    public TheD6(int id, int level) {
        super(id, level);
    }

    @Override
    public void onFirstUse(ServerPlayer player, @Nullable ItemStack stack, @Nullable InteractionHand hand) {
    }

    @Override
    public void onTrigger(ServerPlayer player, ItemStack stack, @Nullable InteractionHand hand) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        Set<BlockPos> posList = BlockData.get(serverLevel).getAllItemBlocks();
        Vec3 playerPos = player.position();

        final double MAX_DISTANCE = 10.0;

        for (BlockPos pos : posList) {
            // distance
            Vec3 blockCenter = Vec3.atCenterOf(pos);
            double distanceSq = playerPos.distanceToSqr(blockCenter);
            if (distanceSq > MAX_DISTANCE * MAX_DISTANCE) continue;

            if (serverLevel.getBlockEntity(pos) instanceof ItemDisplayContainerBlockEntity be) {
                be.itemRollFromPlayer(player);
            }
        }

    }

    @Override
    public void onTriggerStronger(ServerPlayer player, ItemStack stack, @Nullable InteractionHand hand){
        onTrigger(player, stack, hand);
    }

    @Override
    public void triggerSFX(ServerPlayer player) {
        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                Component.translatable("item.isaac_disaster.the_d6.lore.1")
        );
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack) {
        List<Component> description = new ArrayList<>();

        if (ClientDataManager.getInstance().getCountFromId(ItemId.CAR_BATTERY.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.car_battery").append(": ")
                    .append(Component.translatable("item.isaac_disaster.synergy.description.no_effect"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }


        return description;
    }
}
