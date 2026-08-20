package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.bomb;

import net.luojiuoscar.isaac_disaster.entity.tnt.IsaacBomb;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BomberBoy extends BombRelated {
    @Override
    protected boolean customEffect(ExecutableEffectContext context, ServerPlayer player, Level level, Vec3 pos, IsaacBomb bomb) {
        if (PlayerHelper.hasItem(ItemId.BOMBER_BOY.getId(), player)) {
            spawnBomberBoyBombs(player, bomb, pos, level);
        }
        return true;
    }

    private void spawnBomberBoyBombs(ServerPlayer player, IsaacBomb source, Vec3 center, Level level) {
        if (!isValidOrigin(source)) return;

        int power = source.getPower();
        float offset = power + 1f;
        Vec3[] offsets = new Vec3[]{
                new Vec3(offset, 0, 0),
                new Vec3(-offset, 0, 0),
                new Vec3(0, 0, offset),
                new Vec3(0, 0, -offset)
        };

        for (Vec3 delta : offsets) {
            EntityHelper.spawnBomb(center.add(delta), player, level, Vec3.ZERO, 0, power, source.getScale(), false);
        }
    }
}
