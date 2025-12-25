package net.luojiuoscar.isaac_disaster.effect.custom;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColor;
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

public class PowerOfBelialEffect extends MobEffect {
    private static final UUID MARTYR_DAMAGE_BOOST_UUID =
            UUID.nameUUIDFromBytes(("isaac_disaster:synergy_of_martyr").getBytes());
    private static final UUID POWER_OF_BELIAL_UUID =
            UUID.nameUUIDFromBytes(("isaac_disaster:power_of_belial_uuid").getBytes());

    public PowerOfBelialEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void addAttributeModifiers(@NotNull LivingEntity entity, @NotNull AttributeMap attributes, int amplifier) {
        super.addAttributeModifiers(entity, attributes, amplifier);
        if (!(entity instanceof ServerPlayer player)) {
            entity.removeEffect(this);
            return;
        }

        StatManager.addBulletColor(player, ModBulletColor.BLOOD_TEAR.getId(), 1);

        AttributeInstance attr = player.getAttribute(ModAttributes.BULLET_RANGE.get());
        if (attr != null) {
            attr.removeModifier(POWER_OF_BELIAL_UUID);
            double addValue = StatManager.DAMAGE.getBonus() * (amplifier + 1);
            AttributeModifier modifier = new AttributeModifier(
                    POWER_OF_BELIAL_UUID,
                    "power_of_belial.damage_bonus",
                    addValue,
                    AttributeModifier.Operation.ADDITION
            );
            attr.addPermanentModifier(modifier);
        }


        // 检查是否拥有 BLOOD_OF_THE_MARTYR
        if (PlayerHelper.hasItem(ItemId.BLOOD_OF_THE_MARTYR.getId(), player)) {
            AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attackDamage != null) {
                attackDamage.removeModifier(MARTYR_DAMAGE_BOOST_UUID);
                attackDamage.addTransientModifier(getBloodDamageBoost());
            }
        }
    }

    @Override
    public void removeAttributeModifiers(@NotNull LivingEntity entity, @NotNull AttributeMap attributes, int amplifier) {
        super.removeAttributeModifiers(entity, attributes, amplifier);

        if (entity instanceof ServerPlayer player) {
            StatManager.addBulletColor(player, ModBulletColor.BLOOD_TEAR.getId(), -1);

            AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attackDamage != null) {
                attackDamage.removeModifier(POWER_OF_BELIAL_UUID);
            }

            AttributeInstance attackDamage2 = player.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attackDamage2 != null) {
                attackDamage2.removeModifier(MARTYR_DAMAGE_BOOST_UUID);
            }
        }
    }

    private static AttributeModifier getBloodDamageBoost() {
        return new AttributeModifier(
                MARTYR_DAMAGE_BOOST_UUID,
                "Blood of the Martyr bonus",
                StatManager.DAMAGE.getBonus(),
                AttributeModifier.Operation.MULTIPLY_BASE
        );
    }


    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }
}
