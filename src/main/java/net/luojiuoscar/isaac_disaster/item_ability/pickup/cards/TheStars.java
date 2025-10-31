package net.luojiuoscar.isaac_disaster.item_ability.pickup.cards;

import net.luojiuoscar.isaac_disaster.block.ModBlocks;
import net.luojiuoscar.isaac_disaster.block.block_entity.PedestalBlockEntity;
import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.ITarot;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.data.PedestalData;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.manager.id_managers.PickupId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TheStars implements ITarot {
    @Override
    public int getItemId() {
        return PickupId.THE_STARS.getId();
    }

    @Override
    public void onUseEffect(Player player, ItemStack stack, InteractionHand hand) {
        teleportToPedestal(player, false);
    }

    @Override
    public void onUseEffectStronger(Player player, ItemStack stack, InteractionHand hand) {
        teleportToPedestal(player, true);
    }

    private void teleportToPedestal(Player player, boolean duplicate){
        Level level = player.level();
        if (level.isClientSide) return;

        PedestalData data = PedestalData.get((ServerLevel) level);
        Set<BlockPos> posSet = data.getAll();

        for (BlockPos pos : posSet){
            //非空、非装饰性
            if (level.getBlockEntity(pos) instanceof PedestalBlockEntity be &&
                    !be.isDecoration() && !be.getItem().isEmpty()){

                player.teleportTo(pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5); // 传送

                if (duplicate){
                    BlockEntity originalBE = level.getBlockEntity(pos);
                    if (!(originalBE instanceof PedestalBlockEntity originalPedestal)) break;
                    ServerLevel serverLevel = (ServerLevel) level;


                    outer:
                    for (int offsetX : List.of(-1, 0, 1)) {
                        for (int offsetZ : List.of(-1, 0, 1)) {
                            BlockPos newPos = pos.offset(offsetX, 0, offsetZ);
                            if (level.getBlockState(newPos).getCollisionShape(level, newPos).isEmpty()) {
                                // 设置方块
                                level.setBlock(newPos, ModBlocks.PEDESTAL_BLOCK.get().defaultBlockState(), 3);

                                // 获取新生成的方块实体
                                if (level.getBlockEntity(newPos) instanceof PedestalBlockEntity newPedestal) {
                                    // 克隆
                                    newPedestal.copyFromOriginal(originalPedestal);
                                    PedestalBlockEntity.linkPedestals(pos, newPos, serverLevel);
                                    break outer;
                                }
                            }
                        }
                    }
                }

                break;
            }
        }
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(SoundEvents.BOOK_PAGE_TURN);
        player.playSound(ModSounds.THE_STARS.get());
    }

    @Override
    public List<Component> getDescription() {
        List<Component> description = new ArrayList<>();
        // 基础效果
        description.add(Component.translatable("item.isaac_disaster.the_stars.lore.1"));

        // 塔罗牌桌布
        if (ClientDataManager.getInstance().getCountFromId(ItemId.TAROT_CLOTH.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.tarot_cloth").append(": ")
                    .append(Component.translatable("item.isaac_disaster.the_stars.tarot_cloth.lore.1"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }

        return description;
    }
}
