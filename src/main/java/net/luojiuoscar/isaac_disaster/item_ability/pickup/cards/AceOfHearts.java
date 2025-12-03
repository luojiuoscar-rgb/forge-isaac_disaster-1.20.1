package net.luojiuoscar.isaac_disaster.item_ability.pickup.cards;

import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPickup;
import net.luojiuoscar.isaac_disaster.manager.LootTableManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.PickupId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class AceOfHearts implements IPickup {
    @Override
    public int getItemId() {
        return PickupId.ACE_OF_HEARTS.getId();
    }

    @Override
    public void onUseEffect(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        ServerLevel level = (ServerLevel) player.level();

        List<LivingEntity> entities = LevelHelper.selectBySphere(level, player.getX(), player.getY(), player.getZ(),
                StatManager.getNearbyRange());

        for (LivingEntity entity : entities){
            if (EntityHelper.isFriendly(entity, player)) continue;

            Vec3 pos = entity.blockPosition().getCenter();
            entity.discard();

            LootHelper.spawnLootAtPos(player, pos, LootTableManager.RANDOM_HEARTS);
        }
    }

    @Override
    public void onUseSound(Player player) {
        player.playNotifySound(SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.ACE_OF_HEARTS.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public List<Component> getDescription() {
        List<Component> description = new ArrayList<>();
        // 基础效果
        description.add(Component.translatable("item.isaac_disaster.ace_of_hearts.lore.1"));

        return description;
    }
}
