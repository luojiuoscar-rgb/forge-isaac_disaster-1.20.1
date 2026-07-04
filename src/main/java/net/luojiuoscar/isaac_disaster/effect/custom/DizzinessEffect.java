package net.luojiuoscar.isaac_disaster.effect.custom;


import net.luojiuoscar.isaac_disaster.helper.FlightHelper;
import net.luojiuoscar.isaac_disaster.manager.TagManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class DizzinessEffect extends MobEffect {
    private static final UUID DIZZINESS_MOVE_UUID =
            UUID.nameUUIDFromBytes(("isaac_disaster:dizziness_move").getBytes());
    private static final UUID DIZZINESS_JUMP_UUID =
            UUID.nameUUIDFromBytes(("isaac_disaster:dizziness_jump").getBytes());

    public DizzinessEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity pLivingEntity, int pAmplifier){
        if (pLivingEntity.getType().is(TagManager.BOSSES)) {
            pLivingEntity.removeEffect(this);
            return;
        }

        //禁止飞行
        if(pLivingEntity instanceof ServerPlayer player){
            FlightHelper.stopIsaacFlying(player);
        }
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier){
        return true;
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }

    @Override
    public void addAttributeModifiers(@NotNull LivingEntity entity,
                                      @NotNull AttributeMap attributes,
                                      int amplifier) {
        super.addAttributeModifiers(entity, attributes, amplifier);

        // 移动速度
        AttributeInstance moveAttr = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (moveAttr != null) {
            moveAttr.removeModifier(DIZZINESS_MOVE_UUID);
            double value = -0.10D * (amplifier + 1); // 每级降低 10%
            AttributeModifier modifier = new AttributeModifier(
                    DIZZINESS_MOVE_UUID,
                    "dizziness.move_slow",
                    value,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            );
            moveAttr.addPermanentModifier(modifier);
        }

        // 跳跃力
        AttributeInstance jumpAttr = entity.getAttribute(Attributes.JUMP_STRENGTH);
        if (jumpAttr != null) {
            jumpAttr.removeModifier(DIZZINESS_JUMP_UUID);
            double value = -0.10D * (amplifier + 1); // 每级降低 10%
            AttributeModifier modifier = new AttributeModifier(
                    DIZZINESS_JUMP_UUID,
                    "dizziness.jump_reduce",
                    value,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            );
            jumpAttr.addPermanentModifier(modifier);
        }
    }

    @Override
    public void removeAttributeModifiers(@NotNull LivingEntity entity,
                                         @NotNull AttributeMap attributes,
                                         int amplifier) {
        super.removeAttributeModifiers(entity, attributes, amplifier);

        AttributeInstance moveAttr = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (moveAttr != null) {
            moveAttr.removeModifier(DIZZINESS_MOVE_UUID);
        }

        AttributeInstance jumpAttr = entity.getAttribute(Attributes.JUMP_STRENGTH);
        if (jumpAttr != null) {
            jumpAttr.removeModifier(DIZZINESS_JUMP_UUID);
        }
    }
}
