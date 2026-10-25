package net.luojiuoscar.isaac_disaster.entity.familiar.impl.spider;

import net.luojiuoscar.isaac_disaster.entity.familiar.DisposableFamiliar;
import net.luojiuoscar.isaac_disaster.entity.familiar.SwarmFamiliar;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/** Blue spider damage policy; the model uses a neutral body texture with this RGB color. */
public final class BlueSpiderEntity extends AbstractSpiderFamiliarEntity implements DisposableFamiliar, SwarmFamiliar {
    public BlueSpiderEntity(EntityType<? extends BlueSpiderEntity> type,Level level) {super(type,level);}

    @Override public int getBodyColor() { return 0x4077E0; }

    @Override protected float getContactDamage(ServerPlayer owner,LivingEntity target) {
        return (float) (2.5 * owner.getAttributeValue(Attributes.ATTACK_DAMAGE) * Math.max(0, 1 + getEnhancement()));
    }
}
