package net.luojiuoscar.isaac_disaster.item_ability.active_item.items;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.entity.custom.LemonEffectCloud;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.active_item.IActiveItem;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TheGamekid implements IActiveItem {

    @Override
    public int getItemId() {
        return ItemId.THE_GAMEKID.getId();
    }

    @Override
    public void onTriggeredEffect(Player player) {
        int duration = 200;
        MobEffectInstance invincible = new MobEffectInstance(ModEffects.INVINCIBLE.get(), duration,0);
        MobEffectInstance no_bullet = new MobEffectInstance(ModEffects.LACRIMAL_HYPOSECRETION.get(), duration,0);
        MobEffectInstance pac_man = new MobEffectInstance(ModEffects.PAC_MAN.get(), duration,0);

        player.addEffect(invincible);
        player.addEffect(no_bullet);
        player.addEffect(pac_man);
    }

    @Override
    public void onTriggeredEffectStronger(Player player){
        int duration = 400;
        MobEffectInstance invincible = new MobEffectInstance(ModEffects.INVINCIBLE.get(), duration,0);
        MobEffectInstance no_bullet = new MobEffectInstance(ModEffects.LACRIMAL_HYPOSECRETION.get(), duration,0);
        MobEffectInstance pac_man = new MobEffectInstance(ModEffects.PAC_MAN.get(), duration,0);

        player.addEffect(invincible);
        player.addEffect(no_bullet);
        player.addEffect(pac_man);
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.THE_GAMEKID.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.the_gamekid.lore.1")
        );
    }

    @Override
    public List<Component> getSynergyDescription() {
        List<Component> description = new ArrayList<>();

        if (ClientDataManager.getInstance().getCountFromId(ItemId.CAR_BATTERY.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.car_battery").append(": ")
                    .append(Component.translatable("item.isaac_disaster.synergy.description.increase_duration"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }

        return description;
    }

    @Override
    public List<Component> getExplain(){
        List<Component> description = new ArrayList<>();

        description.add(Component.translatable("effect.isaac_disaster.invincible").append(": ")
                .append(Component.translatable("effect.isaac_disaster.invincible.explain.1")));

        description.add(Component.translatable("effect.isaac_disaster.pac_man").append(": ")
                .append(Component.translatable("effect.isaac_disaster.pac_man.explain.1"))
                .append(Component.translatable("effect.isaac_disaster.pac_man.explain.2")));

        description.add(Component.translatable("effect.isaac_disaster.lacrimal_hyposecretion").append(": ")
                .append(Component.translatable("effect.isaac_disaster.lacrimal_hyposecretion.explain.1")));

        return description;
    }
}
