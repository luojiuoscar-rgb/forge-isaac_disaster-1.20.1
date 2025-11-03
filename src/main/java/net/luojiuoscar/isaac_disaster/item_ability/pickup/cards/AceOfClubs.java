package net.luojiuoscar.isaac_disaster.item_ability.pickup.cards;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPickup;
import net.luojiuoscar.isaac_disaster.manager.LootTableNameManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.PickupId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class AceOfClubs implements IPickup {
    @Override
    public int getItemId() {
        return PickupId.ACE_OF_CLUBS.getId();
    }

    @Override
    public void onUseEffect(Player player, ItemStack stack, InteractionHand hand) {
        ServerLevel level = (ServerLevel) player.level();

        List<LivingEntity> entities = LevelHelper.selectBySphere(level, player.getX(), player.getY(), player.getZ(),
                StatManager.getNearbyRange());

        for (LivingEntity entity : entities){
            if (EntityHelper.isFriendlyToPlayer(entity, player)) continue;

            Vec3 pos = entity.blockPosition().getCenter();
            entity.discard();

            EntityHelper.spawnLootAtPos(player, pos, ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID,
                    LootTableNameManager.RANDOM_BOMBS));
        }
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(SoundEvents.BOOK_PAGE_TURN);
        player.playSound(ModSounds.ACE_OF_CLUBS.get());
    }

    @Override
    public List<Component> getDescription() {
        List<Component> description = new ArrayList<>();
        // 基础效果
        description.add(Component.translatable("item.isaac_disaster.ace_of_clubs.lore.1"));

        return description;
    }
}
