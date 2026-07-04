package net.luojiuoscar.isaac_disaster.entity.custom;

import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Area-effect cloud used by lemon effects.
 *
 * <p>The visual and lifetime behavior still comes from vanilla {@link AreaEffectCloud}, but damage
 * and target filtering are handled here so the effect can skip allies and scale damage per use.</p>
 */
public class LemonEffectCloud extends AreaEffectCloud {
    private static final String DAMAGE_TAG = "IsaacLemonDamage";

    private float damage;

    public LemonEffectCloud(EntityType<? extends AreaEffectCloud> type, Level level) {
        super(type, level);
    }

    /**
     * Creates a lemon cloud with this mod's entity type and custom damage behavior.
     */
    public static LemonEffectCloud create(Level level, double x, double y, double z,
                                          @Nullable LivingEntity owner, float radius, int duration,
                                          float radiusPerTick, int waitTime, float damage) {
        LemonEffectCloud cloud = new LemonEffectCloud(ModEntities.SELECTIVE_EFFECT_CLOUD.get(), level);
        cloud.moveTo(x, y, z);
        cloud.setOwner(owner);
        cloud.setRadius(radius);
        cloud.setDuration(duration);
        cloud.setRadiusPerTick(radiusPerTick);
        cloud.setWaitTime(waitTime);
        cloud.setFixedColor(ColorManager.LEMON);
        cloud.damage = damage;
        return cloud;
    }

    /**
     * Applies lemon damage after vanilla cloud lifetime, radius, and particle state update.
     */
    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide || isRemoved()) return;

        List<LivingEntity> entities = this.level().getEntitiesOfClass(
                LivingEntity.class, this.getBoundingBox());

        for (LivingEntity entity : entities) {
            if (!shouldAffect(entity)) continue;

            LivingEntity owner = getOwner();
            if (owner != null) {
                entity.hurt(this.damageSources().indirectMagic(this, owner), this.damage);
            } else {
                entity.hurt(this.damageSources().magic(), this.damage);
            }
        }
    }

    /**
     * Returns whether the custom lemon damage should be applied to an entity this tick.
     */
    private boolean shouldAffect(LivingEntity entity) {
        LivingEntity owner = this.getOwner();
        if (entity == owner) return false;
        if (owner != null && EntityHelper.isFriendly(entity, owner)) return false;

        if (owner instanceof Player player && entity instanceof Player otherPlayer) {
            return player.canHarmPlayer(otherPlayer);
        }
        return true;
    }

    /**
     * Restores the custom damage value when this cloud is loaded from NBT.
     */
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.damage = tag.getFloat(DAMAGE_TAG);
    }

    /**
     * Saves the custom damage value that vanilla {@link AreaEffectCloud} does not know about.
     */
    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat(DAMAGE_TAG, this.damage);
    }

    /**
     * Uses Forge's spawning packet so clients receive this custom cloud entity type correctly.
     */
    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
