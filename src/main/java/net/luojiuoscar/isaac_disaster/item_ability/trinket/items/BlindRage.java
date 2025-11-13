package net.luojiuoscar.isaac_disaster.item_ability.trinket.items;

import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.luojiuoscar.isaac_disaster.item_ability.trinket.IHurtTriggerTrinket;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.id.TrinketId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;

public class BlindRage implements IHurtTriggerTrinket {
    @Override
    public int getId() {
        return TrinketId.BLIND_RAGE.getId();
    }

    @Override
    public List<Component> getDescription() {
        return List.of(Component.translatable("item.isaac_disaster.blind_rage.lore.1"));
    }

    @Override
    public List<Component> getEnchantedDescription() {
        return List.of(Component.translatable("item.isaac_disaster.blind_rage.enchanted.lore.1")
                .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
    }

    @Override
    public void handleHurtEffect(Player player, Entity attacker, List<ItemStack> stackList, LivingHurtEvent event) {
        if (stackList.stream().anyMatch(Trinket::isEnchanted)){
            player.invulnerableTime = 50;
        }else{
            player.invulnerableTime = 25;
        }
    }

    @Override
    public boolean isPunishType() {
        return false;
    }

    @Override
    public double getTriggerChance(Player player, List<ItemStack> stackList) {
        return 1.0;
    }
}
