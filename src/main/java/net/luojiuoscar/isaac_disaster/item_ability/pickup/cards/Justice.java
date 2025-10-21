package net.luojiuoscar.isaac_disaster.item_ability.pickup.cards;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.ITarot;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.LootTableNameManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.manager.id_managers.PickupId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class Justice implements ITarot {
    @Override
    public int getItemId() {
        return PickupId.JUSTICE.getId();
    }

    @Override
    public void onUseEffect(Player player, ItemStack stack, InteractionHand hand) {
        ServerLevel level = (ServerLevel) player.level();
        Vec3 pos = player.blockPosition().getCenter();

        LevelHelper.spawnLootAtPos(level, pos, ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, LootTableNameManager.RANDOM_COINS));
        LevelHelper.spawnLootAtPos(level, pos, ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, LootTableNameManager.RANDOM_HEARTS));
        LevelHelper.spawnLootAtPos(level, pos, ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, LootTableNameManager.RANDOM_BOMBS));
    }

    @Override
    public void onUseEffectStronger(Player player, ItemStack stack, InteractionHand hand) {
        onUseEffect(player, stack, hand);
        onUseEffect(player, stack, hand);
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(SoundEvents.BOOK_PAGE_TURN);
        player.playSound(ModSounds.JUSTICE.get());
    }

    @Override
    public List<Component> getDescription() {
        List<Component> description = new ArrayList<>();
        // 基础效果
        description.add(Component.translatable("item.isaac_disaster.justice.lore.1"));

        // 塔罗牌桌布
        if (ClientDataManager.getInstance().getCountFromId(ItemId.TAROT_CLOTH.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.tarot_cloth").append(": ")
                    .append(Component.translatable("item.isaac_disaster.synergy.description.double"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }

        return description;
    }
}
