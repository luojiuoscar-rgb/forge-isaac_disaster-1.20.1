package net.luojiuoscar.isaac_disaster.item_ability.active_item.items;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.item_ability.active_item.IActiveItem;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class Kamikaze implements IActiveItem {

    @Override
    public int getItemId() {
        return ItemId.KAMIKAZE.getId();
    }

    @Override
    public SoundEvent getSound() {
        return null;
    }

    @Override
    public void onTriggeredEffect(Player player) {
        explode(player, 4, 35);
    }

    @Override
    public void onTriggeredEffectStronger(Player player){
        explode(player, 7, 80);
    }

    private void explode(Player player, float power, float damage){
        player.level().explode(
                null,
                null,
                new ExplosionDamageCalculator() {},
                player.getX(),
                player.getY(),
                player.getZ(),
                power,
                false,
                Level.ExplosionInteraction.BLOCK
        );
        Vec3 pos = player.position();
        List<LivingEntity> livingEntities = LevelHelper.selectBySquare(player.level(), pos.x, pos.y, pos.z,
                power + 2);
        for (LivingEntity entity : livingEntities){
            entity.hurt(player.damageSources().explosion(player, null), damage);
        }
    }


    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.kamikaze.lore.1")
        );
    }

    @Override
    public List<Component> getSynergyDescription() {
        List<Component> description = new ArrayList<>();

        if (ClientDataManager.getInstance().getCountFromId(ItemId.CAR_BATTERY.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.car_battery").append(": ")
                    .append(Component.translatable("item.isaac_disaster.synergy.description.stronger"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }
        return description;
    }
}
