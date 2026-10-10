package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.helper.GeometryHelper;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.event.custom.misc.GetShotDelayEvent;
import net.luojiuoscar.isaac_disaster.event.custom.misc.IsaacGetBulletCountEvent;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.attack_type.tags.IChargeableAttack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AttackType {
    private final int priorityTier;
    private final double priority;

    public AttackType(int priorityTier, double priority){
        this.priorityTier = priorityTier;
        this.priority = priority;
    }

    public AttackType(double priority){
        this(0, priority);
    }

    public int getPriorityTier() {
        return priorityTier;
    }

    public double getPriority() {
        return priority;
    }

    public abstract ResourceLocation getId();

    /** Root behavior family. A new root defaults to its own registered ID. */
    public ResourceLocation getRootId() { return getId(); }

    public abstract List<AttackContext> getAttackContexts(ServerPlayer player, int bulletCount);
    /** Starts this type's complete attack; delegating types intentionally do nothing. */
    public abstract void performAttack(List<AttackContext> ctxList);
    public abstract void makeSound(LivingEntity entity);
    public abstract void shoot(AttackContext ctx);
    public void onTick(ServerPlayer player){}

    public final void tickAttack(ServerPlayer player) {
        if (this instanceof IChargeableAttack charge && !charge.isChargeEligible(player)) {
            charge.clearCharge(player);
            return;
        }
        onTick(player);
        if (this instanceof IChargeableAttack charge) charge.syncCharge(player);
    }

    public final void handleChargeInput(ServerPlayer player, boolean pressed) {
        if (!(this instanceof IChargeableAttack charge)) return;
        if (!charge.isChargeEligible(player)) {
            charge.clearCharge(player);
            return;
        }
        if (pressed) charge.onPressed(player);
        else charge.onReleased(player);
        charge.syncCharge(player);
    }

    /**
     * Returns whether this attack type should participate in current attack selection.
     *
     * <p>The default is active. Future disabling effects can override this method without adding
     * special cases to the selector.</p>
     */
    public boolean isActive(AttackSelectionContext context) {
        return true;
    }

    @Nullable
    public AttackContext createAttackContext(ServerPlayer player, Entity shooter) {
        return player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY)
                .map(playerAbility -> {
                    ResourceLocation colorRl = playerAbility.getBestBulletColor();
                    Vec3 eyePos = player.getEyePosition().add(0, player.getBbHeight() * -0.15, 0);

                    return AttackContext.builder(player, shooter)
                            .color(colorRl).visuals(playerAbility.getBulletVisuals())
                            .trigger(new CompositeTrigger())
                            .position(eyePos).attackType(this).mainAxis(GeometryHelper.mainAxisFromRotation(player.getXRot(), player.getYRot()))
                            .range(getRange(player)).speed(getBulletSpeed(player)).build();
                })
                .orElse(null);
    }

    // ============ 属性相关 =============
    protected boolean isSpectral(LivingEntity entity){
        if (entity instanceof Player player){
            return player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY)
                    .map(a -> a.getSpectral() > 0)
                    .orElse(false);
        }
        return false;
    }

    protected boolean isHoming(LivingEntity entity){
        if (entity instanceof Player player){
            return player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY)
                    .map(a -> a.getHoming() > 0)
                    .orElse(false);
        }
        return false;
    }

    protected boolean isPiercing(LivingEntity entity){
        if (entity instanceof Player player){
            return player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY)
                    .map(a -> a.getPiercing() > 0)
                    .orElse(false);
        }
        return false;
    }

    protected boolean isControllable(LivingEntity entity){
        if (entity instanceof Player player){
            return player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY)
                    .map(a -> a.getControllable() > 0)
                    .orElse(false);
        }
        return false;
    }

    public int getBulletCount(Player player){
        AttributeInstance bulletCount = player.getAttribute(ModAttributes.BULLET_COUNT.get());
        int count = bulletCount == null ? 1 : (int) bulletCount.getValue();

        IsaacGetBulletCountEvent event = new IsaacGetBulletCountEvent(player, count);
        MinecraftForge.EVENT_BUS.post(event);

        return Math.min(event.getCount(), 17);
    }

    protected float getDamage(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        return attr != null ? (float) attr.getValue() : 1f;
    }

    public double getBulletSpeed(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(ModAttributes.BULLET_SPEED.get());
        return attr != null ? Math.max(attr.getValue(), 0.1) : 1.0;
    }

    public double getRange(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(ModAttributes.BULLET_RANGE.get());
        return attr != null ? Math.max(Math.min(attr.getValue(), 64), 1) : 18.0;
    }

    protected double getTears(Player player) {
        AttributeInstance instance = player.getAttribute(ModAttributes.TEARS.get());
        if (instance == null) return 0.0;

        return  Math.max(instance.getValue(),-7);
    }

    protected double getTearsCorrection(Player player) {
        AttributeInstance instance = player.getAttribute(ModAttributes.TEARS_CORRECTION.get());
        MobEffectInstance effect = player.getEffect(MobEffects.DIG_SPEED);

        double value = 0;
        value += effect != null ? effect.getAmplifier() + 1 : 0;
        value += instance != null ? instance.getValue() : 0;

        return  value;
    }

    protected double getShotDelay(Player player) {
        double tears = getTears(player);
        double delay;

        if (tears < -(10.0/13.0)){
            delay = (11 - 4*tears);
        }else if(tears >= -(10.0/13.0) && tears < 0){
            delay = (11 - 4*Math.sqrt(1.3*tears+1) - 4*tears);
        }else if(tears >= 0 && tears < (165.0/104.0)){
            delay = (11 - 4*Math.sqrt(1.3*tears+1));
        }else{
            delay =  4;
        }
        delay -= getTearsCorrection(player);

        GetShotDelayEvent event = new GetShotDelayEvent(player, delay);
        MinecraftForge.EVENT_BUS.post(event);

        if (!event.isCanceled()){
            delay = event.getDelay();
        }

        return Math.max(delay, 0);
    }


}
