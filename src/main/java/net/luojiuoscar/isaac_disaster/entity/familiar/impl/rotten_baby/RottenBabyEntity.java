package net.luojiuoscar.isaac_disaster.entity.familiar.impl.rotten_baby;

import net.luojiuoscar.isaac_disaster.entity.familiar.base.baby.AbstractBabyFamiliarEntity;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.luojiuoscar.isaac_disaster.registries.familiar.ModFamiliarEntities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class RottenBabyEntity extends AbstractBabyFamiliarEntity {
    public RottenBabyEntity(EntityType<?> type, Level level) { super(type, level); }

    @Override
    protected int attackIntervalTicks() { return 20; }

    @Override
    protected boolean performAttack(ServerPlayer owner, Vec3 direction) {
        var data = owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElse(null);
        if (data == null || data.getCount(ModFamiliarEntities.BLUE_FLY.getId(), ModFamiliarEntities.ROTTEN_BABY.getId())
                >= data.getEffectiveCount(ModFamiliarEntities.ROTTEN_BABY.getId())) return false;
        return !FamiliarHelper.spawnFamiliars(owner, ModFamiliarEntities.BLUE_FLY.getId(),
                ModFamiliarEntities.ROTTEN_BABY.getId(), attackOrigin()).isEmpty();
    }
}
