package net.luojiuoscar.isaac_disaster.effect.custom;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.ActiveItemManager;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class NecronmiconShieldEffect extends MobEffect {
    public NecronmiconShieldEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public java.util.List<ItemStack> getCurativeItems() {
        return java.util.Collections.emptyList();
    }

    public static void onTriggered(LivingHurtEvent event){
        Entity attacker = event.getSource().getEntity();
        if (!(event.getEntity() instanceof Player player)) return;

        if (event.getAmount() <= Math.max(1.0f, StatManager.MAX_HEALTH.getBonus() * 0.25f)) return;
        // 伤害来源不能是拥有死灵庇护的玩家；否则不生效
        if (attacker instanceof Player attackerplayer &&
                attackerplayer.hasEffect(ModEffects.NECRONMICON_SHIELD.get())) return;

        // effect
        ActiveItemManager.getInstance().getItemFromId(ItemId.THE_NECRONMICON.getId()).onTriggeredEffect(player);
        // remove 1 amplifier
        EntityHelper.addAmplifier(player, ModEffects.NECRONMICON_SHIELD.get(), -1);
        // sounds
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.BLACK_HEART_ACTIVE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
