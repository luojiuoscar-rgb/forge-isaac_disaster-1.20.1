package net.luojiuoscar.isaac_disaster.item_ability.pickup.pill_effects;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPillEffect;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.PillEffectId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PillEffectManager;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.PillOnUseS2CPacket;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Objects;


public class FriendsTillTheEnd implements IPillEffect {
    @Override
    public int getPillEffectId() {
        return PillEffectId.FRIENDS_TILL_THE_END.getId();
    }

    @Override
    public void onUse(ServerPlayer player){
        if (PlayerHelper.getPillQuality(player) < 0){
            PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.HEALTH_DOWN.getId()).onUse(player);
            return;
        }

        onUseEffect(player);
        if (!player.level().isClientSide){
            ModMessages.sentToPlayer(new PillOnUseS2CPacket(getPillEffectId(), false), (ServerPlayer) player);
        }
    }

    @Override
    public void onUseH(ServerPlayer player){
        if (PlayerHelper.getPillQuality(player) < 0){
            PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.HEALTH_DOWN.getId()).onUseH(player);
            return;
        }

        onUseEffectH(player);
        if (!player.level().isClientSide){
            ModMessages.sentToPlayer(new PillOnUseS2CPacket(getPillEffectId(), true), (ServerPlayer) player);
        }
    }

    @Override
    public void onUseEffect(ServerPlayer player) {
        Level level = player.level();
        Wolf wolf = EntityType.WOLF.create(level);
        if (wolf != null) {
            wolf.tame(player); // 主人
            wolf.setOrderedToSit(false);
            wolf.setPos(player.blockPosition().getCenter());
            Objects.requireNonNull(wolf.getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(StatManager.DAMAGE.getBonus() * 5);
            Objects.requireNonNull(wolf.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(StatManager.MAX_HEALTH.getBonus() * 5);
            wolf.setHealth(wolf.getMaxHealth());

            level.addFreshEntity(wolf);
        }
    }

    @Override
    public void onUseEffectH(ServerPlayer player) {
        Level level = player.level();
        Wolf wolf = EntityType.WOLF.create(level);
        if (wolf != null) {
            wolf.tame(player); // 主人
            wolf.setOrderedToSit(false);
            wolf.setPos(player.blockPosition().getCenter());
            wolf.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(StatManager.DAMAGE.getBonus() * 12);
            wolf.getAttribute(Attributes.MAX_HEALTH).setBaseValue(StatManager.MAX_HEALTH.getBonus() * 12);
            wolf.setHealth(wolf.getMaxHealth());

            level.addFreshEntity(wolf);
        }
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(ModSounds.FRIENDS_TILL_THE_END.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.PILL_USE_GOOD.get(), 1.0f, 1.0f);
    }

    @Override
    public void onUseSoundH(Player player) {
        player.playSound(ModSounds.FRIENDS_TILL_THE_END_H.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.PILL_USE_GOOD_H.get(), 1.0f, 1.0f);
    }

    @Override
    public String getDescriptionId(){
        if (ClientDataManager.getInstance().getPillQuality() < 0){
            return "pill.isaac_disaster.effect.health_down";
        }
        return "pill.isaac_disaster.effect.friends_till_the_end";
    }

}
