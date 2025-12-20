package net.luojiuoscar.isaac_disaster.registries.ability.pickup.impl.cards;

import net.luojiuoscar.isaac_disaster.block.block_entity.PedestalBlockEntity;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.data.BlockData;
import net.luojiuoscar.isaac_disaster.registries.ability.pickup.PickupAbility;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CreditCard extends PickupAbility {
    @Override
    public void onUseEffect(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        // 免费底座道具
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        Set<BlockPos> posList = BlockData.get(serverLevel).getAllItemBlocks();
        Vec3 playerPos = player.position();

        double radius = StatManager.getNearbyRange();

        for (BlockPos pos : posList) {
            // distance
            Vec3 blockCenter = Vec3.atCenterOf(pos);
            double distanceSq = playerPos.distanceToSqr(blockCenter);
            if (distanceSq > radius * radius) continue;

            if (serverLevel.getBlockEntity(pos) instanceof PedestalBlockEntity be) {
                be.setLifeCost(0);
                be.setMoneyCost(0);
                be.setChanged();
            }
        }

        // 转化村民
        List<Entity> villager = LevelHelper.selectBySphere(
                serverLevel, player.getX(), player.getY(), player.getZ(), radius, e -> e instanceof Merchant);

        for (Entity e : villager){
            if (!(e instanceof Merchant v)) continue;

            MerchantOffers offers = v.getOffers();
            if (offers.isEmpty()) continue;

            for (MerchantOffer offer : offers) {
                ItemStack result = offer.getResult();

                if (result.isEmpty()) continue;

                // 生成掉落物
                ItemEntity itemEntity = new ItemEntity(
                        serverLevel,
                        e.getX(),
                        e.getY() + 0.5,
                        e.getZ(),
                        result.copy()
                );
                itemEntity.setPickUpDelay(20);
                serverLevel.addFreshEntity(itemEntity);
            }

            e.discard();
        }

    }


    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.CREDIT_CARD.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public List<Component> getDesc() {
        List<Component> description = new ArrayList<>();

        description.add(Component.translatable("item.isaac_disaster.credit_card.lore.1"));
        description.add(Component.translatable("item.isaac_disaster.credit_card.lore.2"));

        return description;
    }
}

