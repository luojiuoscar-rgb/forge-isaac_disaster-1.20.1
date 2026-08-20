package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.bomb;

import net.luojiuoscar.isaac_disaster.entity.tnt.BombData;
import net.luojiuoscar.isaac_disaster.entity.tnt.IsaacBomb;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ScatterBomb extends BombRelated {
    @Override
    protected boolean customEffect(ExecutableEffectContext context, ServerPlayer player, Level level, Vec3 pos, IsaacBomb bomb) {
        if (PlayerHelper.hasItem(ItemId.SCATTER_BOMB.getId(), player)) {
            spawnScatterBombs(player, bomb, pos, level);
        }
        return true;
    }

    private void spawnScatterBombs(ServerPlayer player, IsaacBomb source, Vec3 center, Level level) {
        if (!isValidOrigin(source)) return;

        int power = source.getPower() - 3;
        float scale = power == BombData.SMALL.power() ? BombData.SMALL.size() : BombData.NORMAL.size();

        for (int i = 0; i < 4; i++) {
            Vec3 randomVelocity = new Vec3(
                    Math.random() * 0.6 - 0.3,
                    Math.random() * 0.4,
                    Math.random() * 0.6 - 0.3
            );
            EntityHelper.spawnBomb(center, player, level, randomVelocity, 30, power, scale,
                    power != BombData.SMALL.power());
        }
    }
}
