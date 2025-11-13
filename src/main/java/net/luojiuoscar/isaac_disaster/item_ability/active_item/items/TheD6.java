package net.luojiuoscar.isaac_disaster.item_ability.active_item.items;

import net.luojiuoscar.isaac_disaster.block.block_entity.misc.ItemDisplayContainerBlockEntity;
import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.item_ability.active_item.IActiveItem;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.data.BlockData;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TheD6 implements IActiveItem {

    @Override
    public int getItemId() {
        return ItemId.THE_D6.getId();
    }

    @Override
    public SoundEvent getSound() {
        return SoundEvents.PLAYER_LEVELUP;
    }

    @Override
    public void onTriggeredEffect(Player player) {
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
    public void onTriggeredEffectStronger(Player player){
        onTriggeredEffect(player);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.the_d6.lore.1")
        );
    }

    @Override
    public List<Component> getSynergyDescription() {
        List<Component> description = new ArrayList<>();

        if (ClientDataManager.getInstance().getCountFromId(ItemId.CAR_BATTERY.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.car_battery").append(": ")
                    .append(Component.translatable("item.isaac_disaster.synergy.description.no_effect"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }


        return description;
    }
}
