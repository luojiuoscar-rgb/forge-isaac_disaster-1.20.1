package net.luojiuoscar.isaac_disaster.item_ability.pickup.pill_effects;

import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPillEffect;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id.PillEffectId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.Potion;


public class SomethingsWrong implements IPillEffect {
    @Override
    public int getPillEffectId() {
        return PillEffectId.SOMETHINGS_WRONG.getId();
    }


    @Override
    public void onUseEffect(Player player) {
        AreaEffectCloud cloud = new AreaEffectCloud(player.level(), player.getX(), player.getY(), player.getZ());
        cloud.setFixedColor(0x000000);
        cloud.setPotion(new Potion(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 400, 0)));
        cloud.setDuration(600);
        cloud.setOwner(player);
        cloud.setRadius((float) StatManager.RANGE.getBonus() * 2.5f);
        player.level().addFreshEntity(cloud);
    }

    @Override
    public void onUseEffectH(Player player) {
        AreaEffectCloud cloud = new AreaEffectCloud(player.level(), player.getX(), player.getY(), player.getZ());
        cloud.setFixedColor(0x000000);
        cloud.setPotion(new Potion(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 600, 2)));
        cloud.setDuration(1500);
        cloud.setOwner(player);
        cloud.setRadius((float) StatManager.RANGE.getBonus() * 4f);
        player.level().addFreshEntity(cloud);
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(ModSounds.SOMETHINGS_WRONG.get(), 1.0f, 1.0f);
    }

    @Override
    public void onUseSoundH(Player player) {
        player.playSound(ModSounds.SOMETHINGS_WRONG_H.get(), 1.0f, 1.0f);
    }

    @Override
    public String getDescriptionId(){
        return "pill.isaac_disaster.effect.somethings_wrong";
    }

}
