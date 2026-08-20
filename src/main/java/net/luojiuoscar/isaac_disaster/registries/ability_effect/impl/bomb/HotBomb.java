package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.bomb;

import net.luojiuoscar.isaac_disaster.entity.fireball.TimedFireball;
import net.luojiuoscar.isaac_disaster.entity.tnt.IsaacBomb;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class HotBomb extends BombRelated {
    @Override
    protected boolean customEffect(ExecutableEffectContext context, ServerPlayer player, Level level, Vec3 pos, IsaacBomb bomb) {
        if (PlayerHelper.hasItem(ItemId.HOT_BOMB.getId(), player)) {
            spawnHotBombFireballs(player, bomb, pos, level);
        }
        return true;
    }

    private void spawnHotBombFireballs(ServerPlayer player, IsaacBomb source, Vec3 pos, Level level) {
        if (!isValidOrigin(source)) return;

        int count = 0;
        if (source.getPower() > 4) {
            count = 3;
        } else if (source.getPower() > 1) {
            count = 5;
        }

        for (int i = 0; i < count; i++) {
            double vx = (level.random.nextDouble() - 0.5) * 0.5;
            double vy = (level.random.nextDouble() - 0.5) * 0.5;
            double vz = (level.random.nextDouble() - 0.5) * 0.5;
            TimedFireball fireball = new TimedFireball(level, player, vx, vy, vz, count);
            fireball.setPos(pos);
            fireball.setDeltaMovement(new Vec3(vx, vy, vz));
            level.addFreshEntity(fireball);
        }
    }
}
