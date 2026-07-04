package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.event.custom.attack.GetAttackContextEvent;
import net.luojiuoscar.isaac_disaster.event.custom.misc.GetShotDelayEvent;
import net.luojiuoscar.isaac_disaster.event.custom.misc.IsaacGetBulletCountEvent;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
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
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public abstract class AttackType {
    private final double priority;

    public AttackType(double priority){
        this.priority = priority;
    }

    public double getPriority() {
        return priority;
    }

    public abstract ResourceLocation getId();

    public List<AttackContext> getAttackContextsWithEvent(ServerPlayer player, int bulletCount){
        List<AttackContext> contexts = getAttackContexts(player, bulletCount);

        GetAttackContextEvent event =
                new GetAttackContextEvent(player, contexts, this, true);
        MinecraftForge.EVENT_BUS.post(event);

        return event.getContexts();
    }

    public abstract List<AttackContext> getAttackContexts(ServerPlayer player, int bulletCount);
    public abstract void performAttack(List<AttackContext> ctxList);
    public abstract void makeSound(LivingEntity entity);
    public abstract void shoot(AttackContext ctx);
    public void onTick(ServerPlayer player){}

    @Nullable
    public AttackContext getOneAttackContext(ServerPlayer player, Entity shooter) {
        return player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY)
                .map(playerAbility -> {
                    ResourceLocation colorRl = playerAbility.getBestBulletColor();
                    Map<ResourceLocation, Integer> trajectories = playerAbility.getTrajectories();
                    Vec3 eyePos = player.getEyePosition().add(0, player.getBbHeight() * -0.15, 0);

                    return new AttackContext(
                            player,
                            shooter,
                            colorRl,
                            new CompositeTrigger(),
                            trajectories,
                            eyePos,
                            player.getXRot(),
                            player.getYRot());
                })
                .orElse(new AttackContext());
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
        int[] count = {bulletCount == null ? 1 : (int) bulletCount.getValue()};

        player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).ifPresent(passive -> {
            int innerEye = passive.getItemCountFromAll(player, ItemId.THE_INNER_EYE.getId());
            int mutantSpider = passive.getItemCountFromAll(player, ItemId.MUTANT_SPIDER.getId());
            int perfectVision = passive.getItemCountFromAll(player, ItemId.PERFECT_VISION.getId());

            if (perfectVision >= 1){
                if (innerEye + mutantSpider == 0){
                    count[0] += 1;
                } else {
                    count[0] += perfectVision - 1;
                }
            }

            if (innerEye + mutantSpider > 0){
                count[0] += innerEye + 2 * mutantSpider + 1;
            }
        });

        IsaacGetBulletCountEvent event = new IsaacGetBulletCountEvent(player, count[0]);
        MinecraftForge.EVENT_BUS.post(event);

        return Math.min(event.getCount(), 17);
    }

    protected float getDamage(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        return attr != null ? (float) attr.getValue() : 1f;
    }

    protected double getBulletSpeed(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(ModAttributes.BULLET_SPEED.get());
        return attr != null ? Math.max(attr.getValue(), 0.1) : 1.0;
    }

    protected double getRange(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(ModAttributes.BULLET_RANGE.get());
        return attr != null ? Math.max(Math.min(attr.getValue(), 64), 1) : 18.0;
    }

    protected float getBulletScale(LivingEntity entity, double damage) {
        AttributeInstance extraAttr = entity.getAttribute(ModAttributes.BULLET_SCALE.get());
        float extra = extraAttr != null ? (float) extraAttr.getValue() : 0f;

        float scale = 1.0f;
        if (damage <= 198) {
            scale *= (float) Math.log10(9 + damage / 2);
        } else {
            scale *= Math.min(4f, (float) (2 + Math.log10((damage - 198) / 20)));
        }
        return scale + extra;
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

    /** 获取攻击方式列表中第n个具有更低优先级的攻击方式 */
    protected AttackType pickLowerAttackType(Map<ResourceLocation, Integer> attackType, int index) {
        IForgeRegistry<AttackType> registry = RegistryManager.ACTIVE.getRegistry(ModAttackType.ATTACK_TYPE_KEY);

        AttackType defaultAttack = ModAttackType.BULLET.get();
        if (registry == null) return defaultAttack;

        // 收集优先级低于自己的攻击方式
        List<AttackType> lowerList = new ArrayList<>();
        double selfPriority = this.getPriority();

        for (ResourceLocation id : attackType.keySet()) {
            AttackType at = registry.getValue(id);
            if (at == null) continue;

            if (at.getPriority() < selfPriority) {
                lowerList.add(at);
            }
        }

        // 按优先级降序排列（从高到低）
        lowerList.sort(Comparator.comparingDouble(AttackType::getPriority).reversed());

        // 返回第 n 个攻击方式，如果越界则返回默认值
        if (index >= 0 && index < lowerList.size()) {
            return lowerList.get(index);
        } else {
            return defaultAttack;
        }
    }


    public static Vec3 rotateAroundAxis(Vec3 v, Vec3 axis, double radians) {
        axis = axis.normalize();
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        Vec3 term1 = v.scale(cos);
        Vec3 term2 = axis.cross(v).scale(sin);
        Vec3 term3 = axis.scale(axis.dot(v) * (1 - cos));

        return term1.add(term2).add(term3);
    }



}
