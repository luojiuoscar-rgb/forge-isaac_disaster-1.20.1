package net.luojiuoscar.isaac_disaster.event.custom.attack;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Cancelable
public class BeforeCreateShootEvent extends Event {

    private final LivingEntity shooter;
    private final AttackContext context;
    private Vec3 offset;
    private float xRot;
    private float yRot;

    private final List<ExtraShot> extraShots = new ArrayList<>();

    public BeforeCreateShootEvent(LivingEntity shooter, AttackContext context,
                            Vec3 offset, float xRot, float yRot) {
        this.shooter = Objects.requireNonNull(shooter);
        this.context = Objects.requireNonNull(context);
        this.offset = offset == null ? Vec3.ZERO : offset;
        this.xRot = xRot;
        this.yRot = yRot;
    }

    public LivingEntity getShooter() { return shooter; }
    public AttackContext getContext() { return context; }

    public Vec3 getOffset() { return offset; }
    public void setOffset(Vec3 offset) { this.offset = offset == null ? Vec3.ZERO : offset; }

    public float getXRot() { return xRot; }
    public void setXRot(float xRot) { this.xRot = xRot; }

    public float getYRot() { return yRot; }
    public void setYRot(float yRot) { this.yRot = yRot; }

    public record ExtraShot(Vec3 offset, float xRot, float yRot) {}

    public void addExtraShot(Vec3 offset, float xRot, float yRot) {
        extraShots.add(new ExtraShot(offset == null ? Vec3.ZERO : offset, xRot, yRot));
    }

    public List<ExtraShot> getExtraShots() { return extraShots; }
}
