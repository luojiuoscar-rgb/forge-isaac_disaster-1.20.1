package net.luojiuoscar.isaac_disaster.effect;


import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class RampageEffect extends MobEffect {
    private static final UUID RAMPAGE_MODIFIER_UUID =
            UUID.nameUUIDFromBytes(("isaac_disaster:rampage_modifier_uuid").getBytes());

    protected RampageEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void addAttributeModifiers(@NotNull LivingEntity entity, @NotNull AttributeMap attributes, int amplifier) {
        super.addAttributeModifiers(entity, attributes, amplifier);

        if (entity instanceof ServerPlayer player) {
            AttributeInstance instance = player.getAttribute(ModAttributes.COLLISION_DAMAGE.get());
            if (instance != null) {
                instance.removeModifier(RAMPAGE_MODIFIER_UUID);
                instance.addTransientModifier(getRampageModifier(amplifier));
            }
        }
    }

    @Override
    public void removeAttributeModifiers(@NotNull LivingEntity entity, @NotNull AttributeMap attributes, int amplifier) {
        super.removeAttributeModifiers(entity, attributes, amplifier);

        if (entity instanceof Player player) {
            AttributeInstance instance = player.getAttribute(ModAttributes.COLLISION_DAMAGE.get());
            if (instance != null) {
                instance.removeModifier(RAMPAGE_MODIFIER_UUID);
            }
        }
    }


    private static AttributeModifier getRampageModifier(int amplifier) {
        return new AttributeModifier(
                RAMPAGE_MODIFIER_UUID,
                "rampage",
                StatManager.getDamageBonus() * (1 + amplifier),
                AttributeModifier.Operation.ADDITION
        );
    }

    @Override
    public java.util.List<ItemStack> getCurativeItems() {
        return java.util.Collections.emptyList();
    }
}
