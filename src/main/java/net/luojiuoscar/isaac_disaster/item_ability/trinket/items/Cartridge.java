package net.luojiuoscar.isaac_disaster.item_ability.trinket.items;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item_ability.trinket.IHurtTriggerTrinket;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.id.TrinketId;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;

public class Cartridge implements IHurtTriggerTrinket {
    @Override
    public int getId() {
        return TrinketId.CARTRIDGE.getId();
    }

    @Override
    public void onFirstEquipped(LivingEntity entity, boolean isEnchanted) {

    }

    @Override
    public void onEquipped(LivingEntity entity, boolean isEnchanted) {

    }

    @Override
    public void onUnequipped(LivingEntity entity, boolean isEnchanted) {

    }

    @Override
    public List<Component> getDescription() {
        return List.of(Component.translatable("item.isaac_disaster.cartridge.lore.1"));
    }

    @Override
    public List<Component> getEnchantedDescription() {
        return List.of(Component.translatable("item.isaac_disaster.synergy.description.no_effect")
                .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
    }

    @Override
    public void handleHurtEffect(Player player, Entity attacker, List<ItemStack> stackList, LivingHurtEvent event) {
        player.addEffect(new MobEffectInstance(ModEffects.INVINCIBLE.get(), 120));
    }

    @Override
    public boolean isPunishType() {
        return false;
    }

    @Override
    public double getTriggerChance(Player player, List<ItemStack> stackList) {
        double count = 1;
        if (player instanceof ServerPlayer serverPlayer){
            count = PlayerHelper.getTrinketCount(TrinketId.CARTRIDGE.getId(), serverPlayer);
        }
        return count / Math.max(count, 20 - getPlayerLuck(player) * 0.5);
    }
}
