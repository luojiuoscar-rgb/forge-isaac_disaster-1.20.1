package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.bomb;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.entity.tnt.IsaacBomb;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.profile.PotionProfile;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class ButtBomb extends BombRelated {
    @Override
    protected boolean customEffect(ExecutableEffectContext context, ServerPlayer player, Level level, Vec3 pos, IsaacBomb bomb) {
        boolean huge = bomb.getPower() > 4;
        double radius = StatManager.getNearbyRange() * (huge ? 1.5 : 1);
        double damage = StatManager.DAMAGE.getBonus() * (huge ? 20 : 10);
        int duration = huge ? 160 : 80;
        for (var target : LevelHelper.selectBySphere(level, pos, radius)) {
            if (EntityHelper.isFriendly(player, target)) continue;
            target.hurt(level.damageSources().playerAttack(player), (float) damage);
            var potion = new ExecutableEffectContext(target);
            potion.set(ContextKeys.POTIONS, List.of(new PotionProfile(ModEffects.PANIC.get(), duration, 0)));
            ModExecutableEffects.POTIONS.get().apply(potion);
        }
        level.playSound(null, pos.x, pos.y, pos.z, (huge ? ModSounds.FART_HUGE : ModSounds.FART_NORMAL).get(), SoundSource.PLAYERS, 1, 1);
        return true;
    }
}
