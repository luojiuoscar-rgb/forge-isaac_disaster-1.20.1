package net.luojiuoscar.isaac_disaster.item_ability.trinket.items;

import net.luojiuoscar.isaac_disaster.item_ability.trinket.ITrinket;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.TrinketId;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModule;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class Cartridge implements ITrinket {
    @Override
    public int getId() {
        return TrinketId.CARTRIDGE.getId();
    }

    @Override
    public void onFirstEquipped(LivingEntity entity, boolean isEnchanted) {

    }

    @Override
    public void onEquipped(LivingEntity entity, boolean isEnchanted) {
        if (isEnchanted){
            StatManager.addTriggerModule(entity, ModTriggerModule.CARTRIDGE.getId(), 2);
        }else{
            StatManager.addTriggerModule(entity, ModTriggerModule.CARTRIDGE.getId(), 1);
        }
    }

    @Override
    public void onUnequipped(LivingEntity entity, boolean isEnchanted) {
        if (isEnchanted){
            StatManager.addTriggerModule(entity, ModTriggerModule.CARTRIDGE.getId(), -2);
        }else{
            StatManager.addTriggerModule(entity, ModTriggerModule.CARTRIDGE.getId(), -1);
        }
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


}
