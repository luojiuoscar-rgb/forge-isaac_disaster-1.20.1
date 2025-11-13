package net.luojiuoscar.isaac_disaster.item_ability.pickup.cards;

import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPickup;
import net.luojiuoscar.isaac_disaster.manager.LootTableManager;
import net.luojiuoscar.isaac_disaster.manager.id.PickupId;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.ArrayList;
import java.util.List;

public class ReverseTarotsBack implements IPickup {
    @Override
    public int getItemId() {
        return PickupId.REVERSE_TAROTS_BACK.getId();
    }

    @Override
    public void onUseEffect(Player player, ItemStack stack, InteractionHand hand) {
        if (!(player.level() instanceof ServerLevel level)) return;
        LootParams params = new LootParams.Builder(level).create(LootContextParamSets.EMPTY);
        // 无源掉落
        LootHelper.spawnLootAtPos(level, player.position(), LootTableManager.RANDOM_TAROTS_R, params);
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(SoundEvents.BOOK_PAGE_TURN);
    }

    @Override
    public List<Component> getDescription() {
        List<Component> description = new ArrayList<>();

        description.add(Component.translatable("item.isaac_disaster.tarots_back.lore.1"));

        return description;
    }
}
